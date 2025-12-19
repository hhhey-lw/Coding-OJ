package com.longoj.top.judge.codesandbox.service.impl;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.FoundWord;
import cn.hutool.dfa.WordTree;
import cn.hutool.json.JSONUtil;
import com.longoj.top.judge.codesandbox.service.CodeSandBox;
import com.longoj.top.judge.codesandbox.model.ExecuteCodeRequest;
import com.longoj.top.judge.codesandbox.model.ExecuteCodeResponse;
import com.longoj.top.model.dto.questionsubmit.JudgeInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class JavaNativeCodeSandBox implements CodeSandBox {
    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";

    private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";

    private static final String GLOBAL_INPUT_DATA_NAME = "input.txt";

    private static final long TIME_OUT = 5000L;


    @Value("${cmdOS.type:windows}")
    private String cmdOSType;

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
                "application.properties",              // 环境变量文件
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

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();
        List<String> inputList = executeCodeRequest.getInputList();

        ExecuteCodeResponse codeResponse = new ExecuteCodeResponse();

        // 1. 检查Code信息
        if (checkCodeFile(code)) {
            codeResponse.setStatus(3);
            codeResponse.setMessage("代码有违规词！");
            return codeResponse;
        }

        // 2. 编译Code
        File compileCode = compileCode(code);

        // 3. 整理输入
        File inputData = collectInputData(inputList, compileCode);

        // 3. 运行得到结果
        List<String> outputList = new ArrayList<>();
        StopWatch stopWatch = new StopWatch();
        Long maxTime = 0L;

        try {
            stopWatch.start();
            // String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main < %s", compileCode.getParentFile().getAbsolutePath(), inputData.getAbsolutePath());
            // window os
            String[] cmd;
            if (cmdOSType.equals("windows")) {
                cmd = new String[]{
                        "cmd.exe", "/c",
                        String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main < %s",
                                compileCode.getParentFile().getAbsolutePath(),
                                inputData.getAbsolutePath())
                };
            } else {
                // linux or mac
                cmd = new String[]{
                        "/bin/sh", "-c",
                        String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main < %s",
                                compileCode.getParentFile().getAbsolutePath(),
                                inputData.getAbsolutePath())
                };
            }
            Process process = Runtime.getRuntime().exec(cmd);

            new Thread(() -> {
                try {
                    Thread.sleep(TIME_OUT);
                    System.out.println("定时任务，兜底中断执行程序");
                    process.destroy();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.err.println("执行成功！");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String outputLine;
                while ((outputLine = bufferedReader.readLine()) != null) {
                    outputList.add(outputLine);
                }
                bufferedReader.close();
            } else {
                System.err.println("执行失败！");
                // 分批获取进程的错误输出
                BufferedReader errorBufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                StringBuilder errorOutputStringBuilder = new StringBuilder();

                // 逐行读取
                String errorCompileOutputLine;
                while ((errorCompileOutputLine = errorBufferedReader.readLine()) != null) {
                    errorOutputStringBuilder.append(errorCompileOutputLine);
                }
                System.err.println(errorOutputStringBuilder.toString());
                errorBufferedReader.close();
            }

            stopWatch.stop();
            maxTime = Math.max(maxTime, stopWatch.getLastTaskTimeMillis());

        } catch (IOException | InterruptedException e) {
            codeResponse.setStatus(3);
            codeResponse.setMessage("运行失败！");
            return codeResponse;
        }


        // 4. 收集结果
        System.out.println(JSONUtil.toJsonStr(outputList));
        codeResponse.setOutputList(outputList);
        codeResponse.setMessage("执行成功！");
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setTime(maxTime);
        judgeInfo.setMessage("执行成功！");
        codeResponse.setJudgeInfo(judgeInfo);

        // 5. 清理文件
        if (compileCode.getParentFile() != null) {
            boolean del = FileUtil.del(compileCode.getParentFile().getAbsolutePath());
            System.out.println("删除" + (del ? "成功" : "失败"));
        }

        return codeResponse;
    }

    private File collectInputData(List<String> inputList, File compileCodeFile) {
        // 1.0.
        String inputData = StrUtil.join("\n", inputList);

        // 1.1. 隔离用户文件
        String userCodeParentPath = compileCodeFile.getParentFile().getAbsolutePath();
        String userDataPath = userCodeParentPath + File.separator + GLOBAL_INPUT_DATA_NAME;
        return FileUtil.writeString(inputData, userDataPath, StandardCharsets.UTF_8);
    }

    private File compileCode(String code) {
        // 1. 保存code为文件
        String userDir = System.getProperty("user.dir");
        String globalCodePathName = userDir + File.separator + GLOBAL_CODE_DIR_NAME;
        if (!FileUtil.exist(globalCodePathName)) {
            FileUtil.mkdir(globalCodePathName);
        }
        // 1.1. 隔离用户文件
        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
        String userCodePath = userCodeParentPath + File.separator + GLOBAL_JAVA_CLASS_NAME;
        File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);

        // 2. 编译 得到 class 文件
        try {
            String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());;
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
