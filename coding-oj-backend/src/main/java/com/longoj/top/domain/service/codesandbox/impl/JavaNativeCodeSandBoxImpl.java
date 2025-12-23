package com.longoj.top.domain.service.codesandbox.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Java本地代码沙箱实现
 * 在本地环境中编译和执行Java代码
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "codesandbox.type", havingValue = "native", matchIfMissing = true)
public class JavaNativeCodeSandBoxImpl extends AbstractJavaCodeSandBox {

    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";
    private static final long TIME_OUT_MS = 5000L;


    @Value("${cmdOS.type:windows}")
    private String cmdOSType;

    @Override
    protected String getCodeRootPath() {
        return System.getProperty("user.dir") + File.separator + GLOBAL_CODE_DIR_NAME;
    }

    @Override
    protected String getCompileCommand(File codeFile) {
        return String.format("javac -encoding utf-8 %s", codeFile.getAbsolutePath());
    }

    @Override
    protected ExecutionResult doExecute(File codeFile) {
        File inputDataFile = new File(codeFile.getParentFile(), GLOBAL_INPUT_DATA_NAME);

        try {
            long startTime = System.currentTimeMillis();

            String[] cmd = buildExecutionCommand(codeFile, inputDataFile);
            Process process = Runtime.getRuntime().exec(cmd);

            // 使用带超时的waitFor
            boolean finished = process.waitFor(TIME_OUT_MS, TimeUnit.MILLISECONDS);
            long executionTime = System.currentTimeMillis() - startTime;

            if (!finished) {
                process.destroyForcibly();
                log.warn("代码执行超时，已强制终止");
                return ExecutionResult.failure("代码执行超时", executionTime, 0L);
            }

            int exitCode = process.exitValue();
            if (exitCode == 0) {
                log.info("代码执行成功");
                String output = readProcessOutput(process);
                return ExecutionResult.success(output, executionTime, 0L);
            } else {
                log.warn("代码执行失败，退出码: {}", exitCode);
                String error = readProcessError(process);
                log.error("错误信息: {}", error);
                return ExecutionResult.failure(error, executionTime, 0L);
            }

        } catch (IOException | InterruptedException e) {
            log.error("执行过程异常", e);
            return ExecutionResult.failure("执行过程异常: " + e.getMessage());
        }
    }

    /**
     * 构建执行命令
     */
    private String[] buildExecutionCommand(File codeFile, File inputDataFile) {
        String javaCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main < %s",
                codeFile.getParentFile().getAbsolutePath(),
                inputDataFile.getAbsolutePath());

        if ("windows".equals(cmdOSType)) {
            return new String[]{"cmd.exe", "/c", javaCmd};
        } else {
            return new String[]{"/bin/sh", "-c", javaCmd};
        }
    }

    /**
     * 读取进程标准输出
     */
    private String readProcessOutput(Process process) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return String.join("\n", lines);
    }

    /**
     * 读取进程错误输出
     */
    private String readProcessError(Process process) throws IOException {
        StringBuilder errorOutput = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                errorOutput.append(line).append("\n");
            }
        }
        return errorOutput.toString().trim();
    }


}
