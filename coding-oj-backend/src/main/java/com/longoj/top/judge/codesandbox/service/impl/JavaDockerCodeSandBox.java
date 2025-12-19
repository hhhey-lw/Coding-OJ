package com.longoj.top.judge.codesandbox.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.FoundWord;
import cn.hutool.dfa.WordTree;
import cn.hutool.json.JSONUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.longoj.top.judge.codesandbox.service.CodeSandBox;
import com.longoj.top.judge.codesandbox.model.ExecuteCodeRequest;
import com.longoj.top.judge.codesandbox.model.ExecuteCodeResponse;
import com.longoj.top.model.dto.questionsubmit.JudgeInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
@ConditionalOnProperty(name = "codesandbox.type", havingValue = "docker")
public class JavaDockerCodeSandBox implements CodeSandBox {

    private static final String LOCAL_FILE_PATH = "/home/admin/app";

    private static final String CONTAINER_FILE_PATH = "/tmp/data";

    private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";

    private static final String GLOBAL_INPUT_DATA_NAME = "input.txt";

    private static final String JAVA_CONTAINER_IMAGE_NAME = "swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/library/openjdk:17-jdk-slim";

    // private static final String CPP_CONTAINER_IMAGE_NAME = "swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/library/gcc:11-alpine";

    private Boolean IS_FIRST_PULL = true;


    @Resource
    private DockerClient dockerClient;

    // 1. 编译检查
    // 2. 输入优化
    // 3. 输出优化
    // 4. 判题系统
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();
        List<String> inputList = executeCodeRequest.getInputList();
        ExecuteCodeResponse codeResponse = new ExecuteCodeResponse();

        // 1.1 用户Java文件 - 代码检查
        // 1. 检查Code信息
        if (checkCodeFile(code)) {
            codeResponse.setStatus(3);
            codeResponse.setMessage("代码有违规词！");
            return codeResponse;
        }

        // 1.2 用户Java文件 - 编译
        // 2. 编译Code
        File codeFile = compileCode(code);

        // 1. 将inputList 整理好， 存储在文件中
        // 3. 整理输入
        File inputData = collectInputData(inputList, codeFile);


        // 2.0 传输Class文件到服务器端
        // TODO 先手动上传 - 运行在服务器，不需要传输

        // 2.1 拉取镜像
        if (IS_FIRST_PULL) {
            try {
                PullImageCmd pullImageCmd = dockerClient.pullImageCmd(JAVA_CONTAINER_IMAGE_NAME);
                pullImageCmd.exec(new PullImageResultCallback() {
                    @Override
                    public void onNext(PullResponseItem item) {
                        System.out.println("下载镜像：" + item.getId());
                        super.onNext(item);
                    }
                }).awaitCompletion();

            } catch (InterruptedException e) {
                System.out.println("拉取镜像异常");
                throw new RuntimeException(e);
            }
            System.out.println("拉取镜像完成");
            IS_FIRST_PULL = false;
        }

        // 2.2 创建容器
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
        System.out.println("容器创建成功：" + containerResponse.getId());

        // 2.3 启动容器
        dockerClient.startContainerCmd(containerResponse.getId()).exec();
        System.out.println("容器启动成功！");

        // 2.4 执行Java文件
        // docker exec keen_blackwell java -cp /app Main 1 3

        List<String> ouputList = new ArrayList<>();
        Long[] maxMemory = new Long[]{0L};
        Long maxTime = 0L;

        try (ResultCallback.Adapter<Statistics> exec = dockerClient.statsCmd(containerResponse.getId()).exec(new ResultCallback.Adapter<Statistics>() {
            @Override
            public void onNext(Statistics stats) {
                Long maxUsage = stats.getMemoryStats().getMaxUsage();
                maxMemory[0] = Math.max(maxUsage, maxMemory[0]);
                System.out.printf("峰值内存: %.2fMB\n", maxUsage / 1024.0 / 1024.0);
            }
        })) {

            String[] cmd = new String[]{"sh", "-c", "java -cp /tmp/data/" + codeFile.getParentFile().getName() +
                    " Main < /tmp/data/" + codeFile.getParentFile().getName() +  "/input.txt"};

            long startTime = System.currentTimeMillis();
            ExecCreateCmdResponse response = dockerClient.execCreateCmd(containerResponse.getId())
                    .withCmd(cmd)
                    .withAttachStdin(true)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();

            System.out.println("容器执行Java代码");
            long endTime = System.currentTimeMillis();
            maxTime = Math.max(endTime-startTime, maxTime);

            String execId = response.getId();
            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            ByteArrayOutputStream stderr = new ByteArrayOutputStream();

            try {
                dockerClient.execStartCmd(execId)
                        .exec(new ExecStartResultCallback(stdout, stderr))
                        .awaitCompletion(5, TimeUnit.SECONDS); // 阻塞直到命令执行完成
            } catch (InterruptedException e) {
                System.out.println("等待Docker执行完毕失败！");
                throw new RuntimeException(e);
            }

            // 4. 获取标准输出和错误
            String output = stdout.toString(StandardCharsets.UTF_8);
            String error = stderr.toString(StandardCharsets.UTF_8);

            if (StrUtil.isNotBlank(output)) {
                ouputList = Arrays.stream(output.split("\n"))
                        .filter(s -> !s.isEmpty())  // 过滤掉空字符串（如末尾的\n产生的空值）
                        .collect(Collectors.toList());
            }

            System.out.println("标准输出: " + output);
            if (!error.isEmpty()) {
                System.out.println("错误输出: " + error);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            // 删除容器
            System.out.println("删除容器");
            dockerClient.stopContainerCmd(containerResponse.getId()).exec();
            dockerClient.removeContainerCmd(containerResponse.getId()).exec();

            // 5. 清理文件
            if (codeFile.getParentFile() != null) {
                boolean del = FileUtil.del(codeFile.getParentFile().getAbsolutePath());
                System.out.println("删除" + (del ? "成功" : "失败"));
            }
        }

        codeResponse.setMessage("执行成功！");
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setTime(maxTime);
        judgeInfo.setMemory(maxMemory[0]);
        judgeInfo.setMessage("执行成功！");
        codeResponse.setJudgeInfo(judgeInfo);
        codeResponse.setOutputList(ouputList);
        System.out.println(JSONUtil.toJsonStr(codeResponse));
        // 3 收集运行信息
        return codeResponse;
    }


    private File collectInputData(List<String> inputList, File codeFile) {
        // 1.0.
        String inputData = StrUtil.join("\n", inputList);

        // 1.1. 隔离用户文件
        String userCodeParentPath = codeFile.getParentFile().getAbsolutePath();
        String userDataPath = userCodeParentPath + File.separator + GLOBAL_INPUT_DATA_NAME;
        return FileUtil.writeString(inputData, userDataPath, StandardCharsets.UTF_8);
    }


    private File compileCode(String code) {
        // 1. 保存code为文件
        String localFilePath = LOCAL_FILE_PATH;
        String userCodeParentPath = localFilePath + File.separator + UUID.randomUUID();
        if (!FileUtil.exist(userCodeParentPath)) {
            FileUtil.mkdir(userCodeParentPath);
        }
        // 1.1. 隔离用户文件
        // String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
        String userCodePath = userCodeParentPath + File.separator + GLOBAL_JAVA_CLASS_NAME;
        File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);

        // 2. 编译 得到 class 文件
        try {
            String compileCmd = String.format("/www/server/java/jdk-17.0.8/bin/javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());;
            Process process = Runtime.getRuntime().exec(compileCmd);

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("编译成功！");
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("编译失败！");
            return null;
        }
        return userCodeFile;
    }


    private static WordTree WORD_TREE;
    static {
        String[] privacyAccessOps = {
                "/etc/passwd",       // Linux用户账户
                "/etc/shadow",       // Linux密码哈希
                "/etc/ssh/sshd_config",
                "C:\\Windows\\System32\\config\\SAM",  // Windows密码文件
                "~/.ssh/id_rsa",     // SSH私钥
                "web.config",        // Web服务器配置
                "application.properties",
                ".env",              // 环境变量文件
                "keyStore.jks",      // Java密钥库
                "wp-config.php"      // WordPress配置
        };
        String[] destructiveOps = {
                "rm -rf /",          // Linux删除根目录
                "del /F /S /Q C:\\*",
                "format C: /y",
                "dd if=/dev/zero of=/dev/sda",
                "chmod 777 / -R",    // 权限滥用
                "iptables --flush",  // 清除防火墙规则
                "echo 1 > /proc/sys/kernel/sysrq",  // 启用SysRq
                "mv /home/user /dev/null",
                "useradd attacker",  // 创建后门账户
                "netcat -l -p 4444 -e /bin/bash"  // 反弹shell
        };

        WORD_TREE = new WordTree();
        WORD_TREE.addWords("Files", "exec");
        WORD_TREE.addWords(privacyAccessOps);
        WORD_TREE.addWords(destructiveOps);
    }
    private boolean checkCodeFile(String code) {
        // 0. 检查代码内部字符
        FoundWord foundWord = WORD_TREE.matchWord(code);
        if (foundWord != null) {
            System.out.println("包含禁止词：" + foundWord.getFoundWord());
            return true;
        }
        return false;
    }
}
