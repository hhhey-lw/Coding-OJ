package com.longoj.top.domain.service.codesandbox.impl;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.exception.NotModifiedException;
import com.github.dockerjava.api.model.Capability;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.longoj.top.infrastructure.exception.BusinessException;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

/**
 * Docker容器代码沙箱实现
 * 使用Docker容器隔离环境执行Java代码，提供更安全的执行环境
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "codesandbox.type", havingValue = "docker")
public class JavaDockerCodeSandBoxImpl extends AbstractJavaCodeSandBox {

    /** 容器内代码存放路径 */
    private static final String CONTAINER_CODE_PATH = "/app";

    private static final String CONTAINER_CLASSES_PATH = "/tmp/classes";

    /** 执行超时时间（秒） */
    private static final int EXECUTION_TIMEOUT_SECONDS = 5;

    private static final String JAVA_CONTAINER_IMAGE_NAME =
            "swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/library/openjdk:17-jdk-slim";

    private final AtomicBoolean needPullImage = new AtomicBoolean(true);

    private final ThreadLocal<String> containerIdHolder = new ThreadLocal<>();


    @Resource
    private DockerClient dockerClient;

    @Override
    protected String getCodeRootPath() {
        return System.getProperty("user.dir") + File.separator + "tmpCode";
    }

    @Override
    protected String[] getCompileCommand(File codeFile) {
        return new String[]{
                "javac",
                "-encoding",
                "utf-8",
                codeFile.getAbsolutePath()
        };
    }

    @Override
    protected void beforeExecute() {
        // 确保Docker镜像存在
        pullDockerImage();
    }

    @Override
    protected boolean doCompile(File codeFile) {
        String containerId = null;
        try {
            // 1. 创建容器
            CreateContainerResponse containerResponse = createDockerContainer();
            containerId = containerResponse.getId();
            containerIdHolder.set(containerId);
            log.info("容器创建成功: {}", containerId);

            // 2. 启动容器
            dockerClient.startContainerCmd(containerId).exec();
            log.info("容器启动成功: {}", containerId);

            // 3. 确保容器内目录存在（避免copy时报 /app 不存在）
            ensureContainerDir(containerId, CONTAINER_CODE_PATH);

            // 4. 上传代码到容器
            uploadCodeToContainer(containerId, codeFile.getParentFile());
            log.info("代码上传成功");

            // 5. 容器内编译
            ExecutionResult compileResult = compileInContainer(containerId, codeFile);
            return compileResult.success();
        } catch (Exception e) {
            log.error("Docker编译流程异常", e);
            return false;
        }
    }

    @Override
    protected ExecutionResult doExecute(File codeFile) {
        String containerId = containerIdHolder.get();
        if (containerId == null) {
            return ExecutionResult.failure("容器未初始化，无法执行");
        }
        return executeInContainer(containerId, codeFile);
    }

    @Override
    protected void afterExecute() {
        String containerId = containerIdHolder.get();
        try {
            cleanupContainer(containerId);
        } finally {
            containerIdHolder.remove();
        }
    }

    /**
     * 清理容器资源
     */
    private void cleanupContainer(String containerId) {
        if (containerId != null) {
            try {
                try {
                    dockerClient.stopContainerCmd(containerId).exec();
                } catch (NotModifiedException e) {
                    // 容器已停止等情况会返回 304，忽略即可
                    log.debug("容器无需停止: {}", containerId);
                }
            } catch (Exception e) {
                log.error("停止容器失败: {}", containerId, e);
            }

            try {
                dockerClient.removeContainerCmd(containerId).exec();
                log.info("容器已删除: {}", containerId);
            } catch (Exception e) {
                log.error("删除容器失败: {}", containerId, e);
            }
        }
    }

    private void ensureContainerDir(String containerId, String dir) {
        try {
            ExecCreateCmdResponse execResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd("sh", "-c", "mkdir -p " + dir)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();

            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            ByteArrayOutputStream stderr = new ByteArrayOutputStream();

            dockerClient.execStartCmd(execResponse.getId())
                    .exec(new ExecStartResultCallback(stdout, stderr))
                    .awaitCompletion(EXECUTION_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            String error = stderr.toString(StandardCharsets.UTF_8);
            if (StrUtil.isNotBlank(error)) {
                log.warn("创建容器目录警告: {}", error);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("创建容器目录被中断");
        }
    }

    // ==================== Docker相关私有方法 ====================

    /**
     * 在容器中执行代码
     */
    private ExecutionResult executeInContainer(String containerId, File codeFile) {
        final long[] maxMemory = {0L};

        try (ResultCallback.Adapter<Statistics> ignored = startMemoryMonitor(containerId, maxMemory)) {
            return doContainerExecute(containerId, codeFile, maxMemory);
        } catch (InterruptedException e) {
            log.error("等待Docker执行完成时被中断", e);
            Thread.currentThread().interrupt();
            return ExecutionResult.failure("执行超时或被中断");
        } catch (IOException e) {
            log.error("Docker执行异常", e);
            return ExecutionResult.failure("执行异常: " + e.getMessage());
        }
    }

    /**
     * 启动内存监控
     */
    private ResultCallback.Adapter<Statistics> startMemoryMonitor(String containerId, long[] maxMemory) {
        return dockerClient.statsCmd(containerId)
                .exec(new ResultCallback.Adapter<>() {
                    @Override
                    public void onNext(Statistics stats) {
                        Long usage = stats.getMemoryStats().getMaxUsage();
                        if (usage != null) {
                            maxMemory[0] = Math.max(usage, maxMemory[0]);
                            log.debug("峰值内存: {}MB", String.format("%.2f", usage / 1024.0 / 1024.0));
                        }
                    }
                });
    }

    /**
     * 执行容器内代码
     */
    private ExecutionResult doContainerExecute(String containerId, File codeFile, long[] maxMemory)
            throws InterruptedException {
        String[] cmd = buildContainerCommand(codeFile);
        long startTime = System.currentTimeMillis();

        // 创建执行命令
        ExecCreateCmdResponse execResponse = dockerClient.execCreateCmd(containerId)
                .withCmd(cmd)
                .withAttachStdin(true)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .exec();

        // 执行并捕获输出
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        dockerClient.execStartCmd(execResponse.getId())
                .exec(new ExecStartResultCallback(stdout, stderr))
                .awaitCompletion(EXECUTION_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        long executionTime = System.currentTimeMillis() - startTime;

        // 解析输出
        String output = stdout.toString(StandardCharsets.UTF_8);
        String error = stderr.toString(StandardCharsets.UTF_8);

        logExecutionOutput(output, error);

        if (StrUtil.isBlank(error)) {
            return ExecutionResult.success(output, executionTime, maxMemory[0]);
        } else {
            return ExecutionResult.failure(error, executionTime, maxMemory[0]);
        }
    }

    /**
     * 构建容器内执行命令
     */
    private String[] buildContainerCommand(File codeFile) {
        String codeDirName = codeFile.getParentFile().getName();
        return new String[]{"sh", "-c",
                String.format("java -cp %s/%s Main < %s/%s/%s",
                        CONTAINER_CLASSES_PATH, codeDirName,
                        CONTAINER_CODE_PATH, codeDirName, GLOBAL_INPUT_DATA_NAME)};
    }

    private ExecutionResult compileInContainer(String containerId, File codeFile) {
        String codeDirName = codeFile.getParentFile().getName();
        String compileCmdStr = String.format(
                "mkdir -p %s/%s && javac -encoding utf-8 -d %s/%s %s/%s/%s",
                CONTAINER_CLASSES_PATH, codeDirName,
                CONTAINER_CLASSES_PATH, codeDirName,
                CONTAINER_CODE_PATH, codeDirName, GLOBAL_JAVA_CLASS_NAME);

        long startTime = System.currentTimeMillis();

        ExecCreateCmdResponse execResponse = dockerClient.execCreateCmd(containerId)
                .withCmd("sh", "-c", compileCmdStr)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .exec();

        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        try {
            dockerClient.execStartCmd(execResponse.getId())
                    .exec(new ExecStartResultCallback(stdout, stderr))
                    .awaitCompletion(EXECUTION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ExecutionResult.failure("编译超时或被中断");
        }

        long executionTime = System.currentTimeMillis() - startTime;
        String output = stdout.toString(StandardCharsets.UTF_8);
        String error = stderr.toString(StandardCharsets.UTF_8);

        logExecutionOutput(output, error);

        if (StrUtil.isBlank(error)) {
            return ExecutionResult.success(output, executionTime, 0L);
        }
        return ExecutionResult.failure(error, executionTime, 0L);
    }

    /**
     * 记录执行输出日志
     */
    private void logExecutionOutput(String output, String error) {
        log.info("标准输出: {}", output);
        if (StrUtil.isNotBlank(error)) {
            log.warn("错误输出: {}", error);
        }
    }

    /**
     * 创建Docker容器
     */
    private CreateContainerResponse createDockerContainer() {
        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(JAVA_CONTAINER_IMAGE_NAME);
        CreateContainerResponse containerResponse = containerCmd
                .withNetworkDisabled(true)
                .withPrivileged(false)
                .withCapDrop(Capability.ALL)
                .withAttachStdin(true)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withTty(true)
                .exec();
        log.info("容器创建成功: {}", containerResponse.getId());
        return containerResponse;
    }

    /**
     * 上传代码目录到容器
     */
    private void uploadCodeToContainer(String containerId, File codeDir) {
        try {
            // 将代码目录打包成tar
            ByteArrayOutputStream tarStream = createTarArchive(codeDir);
            
            // 上传到容器
            dockerClient.copyArchiveToContainerCmd(containerId)
                    .withTarInputStream(new ByteArrayInputStream(tarStream.toByteArray()))
                    .withRemotePath(CONTAINER_CODE_PATH)
                    .exec();
            
            log.info("代码目录已上传到容器: {} -> {}", codeDir.getName(), CONTAINER_CODE_PATH);
        } catch (IOException e) {
            log.error("上传代码到容器失败", e);
            throw new BusinessException("上传代码失败: " + e.getMessage());
        }
    }

    /**
     * 将目录打包成tar格式
     */
    private ByteArrayOutputStream createTarArchive(File directory) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        try (TarArchiveOutputStream tarOutput = new TarArchiveOutputStream(outputStream)) {
            tarOutput.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
            addDirectoryToTar(tarOutput, directory, directory.getName());
            tarOutput.finish();
        }
        
        return outputStream;
    }

    /**
     * 递归添加目录到tar包
     */
    private void addDirectoryToTar(TarArchiveOutputStream tarOutput, File file, String entryName) 
            throws IOException {
        if (file.isDirectory()) {
            // 添加目录条目
            TarArchiveEntry dirEntry = new TarArchiveEntry(file, entryName + "/");
            tarOutput.putArchiveEntry(dirEntry);
            tarOutput.closeArchiveEntry();
            
            // 递归添加子文件
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    addDirectoryToTar(tarOutput, child, entryName + "/" + child.getName());
                }
            }
        } else {
            // 添加文件条目
            TarArchiveEntry fileEntry = new TarArchiveEntry(file, entryName);
            tarOutput.putArchiveEntry(fileEntry);
            
            // 写入文件内容
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    tarOutput.write(buffer, 0, bytesRead);
                }
            }
            
            tarOutput.closeArchiveEntry();
        }
    }

    /**
     * 拉取Docker镜像（仅首次执行时拉取）
     */
    private void pullDockerImage() {
        if (!needPullImage.get()) {
            return;
        }
        try {
            PullImageCmd pullImageCmd = dockerClient.pullImageCmd(JAVA_CONTAINER_IMAGE_NAME);
            pullImageCmd.exec(new PullImageResultCallback() {
                @Override
                public void onNext(PullResponseItem item) {
                    log.info("下载镜像: {}", item.getId());
                    super.onNext(item);
                }
            }).awaitCompletion();

            log.info("镜像拉取完成");
            needPullImage.set(false);
        } catch (InterruptedException e) {
            log.error("拉取镜像被中断", e);
            Thread.currentThread().interrupt();
            throw new BusinessException("拉取镜像失败");
        }
    }

}
