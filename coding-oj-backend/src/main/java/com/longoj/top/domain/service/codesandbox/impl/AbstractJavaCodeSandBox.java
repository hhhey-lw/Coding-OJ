package com.longoj.top.domain.service.codesandbox.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.longoj.top.domain.entity.dto.ExecuteCodeRequest;
import com.longoj.top.domain.entity.dto.ExecuteCodeResponse;
import com.longoj.top.domain.entity.dto.JudgeInfo;
import com.longoj.top.domain.service.codesandbox.CodeSandBox;
import com.longoj.top.infrastructure.utils.KeyWordDetectUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Java代码沙箱抽象模板类
 * 定义代码执行的通用流程，子类实现具体的执行方式
 */
@Slf4j
public abstract class AbstractJavaCodeSandBox implements CodeSandBox {

    protected static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";
    protected static final String GLOBAL_INPUT_DATA_NAME = "input.txt";

    /**
     * 模板方法：执行代码的通用流程
     */
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest request) {
        String code = request.getCode();
        List<String> inputList = request.getInputList();

        // 1. 代码安全检查
        ExecuteCodeResponse securityCheckResult = checkCodeSecurity(code);
        if (securityCheckResult != null) {
            return securityCheckResult;
        }

        // 2. 保存代码文件
        File codeFile = saveCodeFile(code);
        if (codeFile == null) {
            return buildErrorResponse("代码文件保存失败！");
        }

        try {
            // 3. 保存输入数据
            saveInputData(inputList, codeFile.getParentFile().getAbsolutePath());

            // 4. 执行前准备（钩子方法，子类可选实现）
            beforeExecute();

            // 5. 编译代码（钩子方法，子类可覆盖实现编译策略）
            boolean compiled = doCompile(codeFile);
            if (!compiled) {
                return buildErrorResponse("代码编译失败！");
            }

            // 6. 执行代码（抽象方法，子类必须实现）
            ExecutionResult result = doExecute(codeFile);

            // 7. 构建响应
            return buildResponse(result);

        } catch (Exception e) {
            log.error("代码执行异常", e);
            return buildErrorResponse("运行失败！");
        } finally {
            // 8. 执行后处理（钩子方法，子类可选实现）
            try {
                afterExecute();
            } finally {
                // 9. 清理资源
                cleanup(codeFile);
            }
        }
    }

    // ==================== 抽象方法（子类必须实现） ====================

    /**
     * 执行编译后的代码
     * @param codeFile 编译后的代码文件
     * @return 执行结果
     */
    protected abstract ExecutionResult doExecute(File codeFile);

    /**
     * 获取代码存放的根路径
     */
    protected abstract String getCodeRootPath();

    /**
     * 获取编译命令
     */
    protected abstract String[] getCompileCommand(File codeFile);

    // ==================== 钩子方法（子类可选实现） ====================

    /**
     * 执行前准备
     */
    protected void beforeExecute() {
        // 默认空实现
    }

    /**
     * 编译代码（默认在宿主机编译，Docker等实现可覆盖为跳过或自定义编译）
     */
    protected boolean doCompile(File codeFile) {
        try {
            String[] compileCmd = getCompileCommand(codeFile);
            Process process = Runtime.getRuntime().exec(compileCmd);
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                log.info("代码编译成功");
                return true;
            } else {
                log.error("代码编译失败，退出码: {}", exitCode);
                return false;
            }
        } catch (Exception e) {
            log.error("编译过程异常", e);
            return false;
        }
    }

    /**
     * 执行后处理
     */
    protected void afterExecute() {
        // 默认空实现
    }

    /**
     * 清理资源
     */
    protected void cleanup(File codeFile) {
        if (codeFile != null && codeFile.getParentFile() != null) {
            boolean deleted = FileUtil.del(codeFile.getParentFile().getAbsolutePath());
            log.info("临时文件清理{}", deleted ? "成功" : "失败");
        }
    }

    // ==================== 通用方法 ====================

    /**
     * 代码安全检查
     */
    private ExecuteCodeResponse checkCodeSecurity(String code) {
        if (KeyWordDetectUtil.checkCodeFile(code)) {
            ExecuteCodeResponse response = new ExecuteCodeResponse();
            response.setStatus(3);
            response.setMessage("代码有违规词！");
            return response;
        }
        return null;
    }

    /**
     * 编译Java代码
     */
    protected File saveCodeFile(String code) {
        // 1. 创建代码存放目录
        String codeRootPath = getCodeRootPath();
        if (!FileUtil.exist(codeRootPath)) {
            FileUtil.mkdir(codeRootPath);
        }

        // 2. 为用户代码创建隔离目录
        String userCodeParentPath = codeRootPath + File.separator + UUID.randomUUID();
        FileUtil.mkdir(userCodeParentPath);

        String userCodePath = userCodeParentPath + File.separator + GLOBAL_JAVA_CLASS_NAME;
        return FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);
    }

    /**
     * 保存输入数据
     */
    protected void saveInputData(List<String> inputList, String parentPath) {
        String inputData = StrUtil.join("\n", inputList);
        String inputDataPath = parentPath + File.separator + GLOBAL_INPUT_DATA_NAME;
        FileUtil.writeString(inputData, inputDataPath, StandardCharsets.UTF_8);
    }

    /**
     * 构建成功/失败响应
     */
    protected ExecuteCodeResponse buildResponse(ExecutionResult result) {
        ExecuteCodeResponse response = new ExecuteCodeResponse();
        response.setMessage(result.success() ? "执行成功！" : "执行失败！");
        response.setOutputList(parseOutputList(result.output()));

        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setTime(result.executionTime());
        judgeInfo.setMemory(result.maxMemory());
        judgeInfo.setMessage(result.success() ? "执行成功！" : result.error());
        response.setJudgeInfo(judgeInfo);

        if (!result.success()) {
            response.setStatus(3);
        }

        log.info("代码执行信息: {}", JSONUtil.toJsonStr(response));
        return response;
    }

    /**
     * 构建错误响应
     */
    protected ExecuteCodeResponse buildErrorResponse(String message) {
        ExecuteCodeResponse response = new ExecuteCodeResponse();
        response.setStatus(3);
        response.setMessage(message);
        return response;
    }

    /**
     * 解析输出列表
     */
    protected List<String> parseOutputList(String output) {
        if (StrUtil.isBlank(output)) {
            return new ArrayList<>();
        }
        return Arrays.stream(output.split("\n"))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    // ==================== 执行结果记录类 ====================

    /**
     * 执行结果
     */
    protected record ExecutionResult(
            boolean success,
            String output,
            String error,
            long executionTime,
            long maxMemory
    ) {
        /** 创建成功结果 */
        public static ExecutionResult success(String output, long executionTime, long maxMemory) {
            return new ExecutionResult(true, output, "", executionTime, maxMemory);
        }

        /** 创建失败结果 */
        public static ExecutionResult failure(String error, long executionTime, long maxMemory) {
            return new ExecutionResult(false, "", error, executionTime, maxMemory);
        }

        /** 创建简单失败结果 */
        public static ExecutionResult failure(String error) {
            return new ExecutionResult(false, "", error, 0L, 0L);
        }
    }
}

