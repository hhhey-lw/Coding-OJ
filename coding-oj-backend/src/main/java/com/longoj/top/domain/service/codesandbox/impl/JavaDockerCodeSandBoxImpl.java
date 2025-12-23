package com.longoj.top.domain.service.codesandbox.impl;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.AccessMode;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Capability;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.longoj.top.infrastructure.exception.BusinessException;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Docker容器代码沙箱实现
 * 使用Docker容器隔离环境执行Java代码，提供更安全的执行环境
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "codesandbox.type", havingValue = "docker")
public class JavaDockerCodeSandBoxImpl extends AbstractJavaCodeSandBox {

    /** 宿主机代码存放路径 */
    private static final String LOCAL_FILE_PATH = "/home/admin/app";

    /** 容器内代码挂载路径 */
    private static final String CONTAINER_FILE_PATH = "/tmp/data";

    /** 执行超时时间（秒） */
    private static final int EXECUTION_TIMEOUT_SECONDS = 5;

    /** JDK路径 */
    private static final String JDK_PATH = "/www/server/java/jdk-17.0.8/bin/javac";

    private static final String JAVA_CONTAINER_IMAGE_NAME =
            "swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/library/openjdk:17-jdk-slim";

    private final AtomicBoolean needPullImage = new AtomicBoolean(true);

    /** 当前执行的容器ID（用于清理） */
    private String currentContainerId;

    @Resource
    private DockerClient dockerClient;

    @Override
    protected String getCodeRootPath() {
        return LOCAL_FILE_PATH;
    }

    @Override
    protected String getCompileCommand(File codeFile) {
        return String.format("%s -encoding utf-8 %s", JDK_PATH, codeFile.getAbsolutePath());
    }

    @Override
    protected void beforeExecute() {
        // 确保Docker镜像存在
        pullDockerImage();
    }

    @Override
    protected ExecutionResult doExecute(File codeFile) {
        // 1. 创建并启动容器
        CreateContainerResponse containerResponse = createDockerContainer();
        currentContainerId = containerResponse.getId();
        dockerClient.startContainerCmd(currentContainerId).exec();
        log.info("容器启动成功: {}", currentContainerId);

        // 2. 在容器中执行代码
        return executeInContainer(currentContainerId, codeFile);
    }

    @Override
    protected void afterExecute() {
        // 停止并删除容器
        if (currentContainerId != null) {
            try {
                dockerClient.stopContainerCmd(currentContainerId).exec();
                dockerClient.removeContainerCmd(currentContainerId).exec();
                log.info("容器已删除: {}", currentContainerId);
            } catch (Exception e) {
                log.error("清理容器失败: {}", currentContainerId, e);
            } finally {
                currentContainerId = null;
            }
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
                        CONTAINER_FILE_PATH, codeDirName,
                        CONTAINER_FILE_PATH, codeDirName, GLOBAL_INPUT_DATA_NAME)};
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
                .withBinds(new Bind(LOCAL_FILE_PATH, new Volume(CONTAINER_FILE_PATH), AccessMode.ro))
                .withReadonlyRootfs(true)
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
