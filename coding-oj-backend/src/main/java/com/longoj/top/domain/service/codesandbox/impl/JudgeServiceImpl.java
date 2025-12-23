package com.longoj.top.domain.service.codesandbox.impl;

import cn.hutool.json.JSONUtil;
import com.longoj.top.domain.entity.enums.QuestionPassStatusEnum;
import com.longoj.top.infrastructure.exception.ErrorCode;
import com.longoj.top.infrastructure.utils.RedisKeyUtil;
import com.longoj.top.infrastructure.exception.BusinessException;
import com.longoj.top.domain.service.codesandbox.CodeSandBox;
import com.longoj.top.domain.entity.dto.ExecuteCodeRequest;
import com.longoj.top.domain.entity.dto.ExecuteCodeResponse;
import com.longoj.top.domain.entity.dto.JudgeContext;
import com.longoj.top.domain.service.codesandbox.JudgeService;
import com.longoj.top.infrastructure.mapper.UserSubmitSummaryMapper;
import com.longoj.top.domain.entity.dto.JudgeCase;
import com.longoj.top.domain.entity.dto.JudgeInfo;
import com.longoj.top.domain.entity.Question;
import com.longoj.top.domain.entity.QuestionSubmit;
import com.longoj.top.domain.entity.enums.JudgeInfoMessageEnum;
import com.longoj.top.domain.entity.enums.QuestionSubmitStatusEnum;
import com.longoj.top.domain.service.QuestionService;
import com.longoj.top.domain.service.QuestionSubmitService;
import com.longoj.top.domain.strategy.DefaultJudgeStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

//
@Slf4j
@Service
public class JudgeServiceImpl implements JudgeService {

    @Lazy
    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private QuestionService questionService;

    @Resource
    private UserSubmitSummaryMapper userSubmitSummaryMapper;

    private final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Value("${codesandbox.type:example}")
    private String CODE_SANDBOX_TYPE;

    @Override
    @Transactional
    public JudgeInfo doJudge(QuestionSubmit questionSubmit) {
        // 1. 信息检查
        if (questionSubmit == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "提交不存在");
        }

        // 1.1. 判断提交题目是否存在
        Question question = questionService.getById(questionSubmit.getQuestionId());
        if (question == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "题目不存在");
        }
        // 1.2. 判断提交题目状态是否可执行
        if (!questionSubmit.getStatus().equals(QuestionSubmitStatusEnum.WAITING.getCode())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "题目正在判题中");
        }

        // =============
        // 2. 更新提交状态
        QuestionSubmit updateObj = new QuestionSubmit();
        updateObj.setId(questionSubmit.getId());
        updateObj.setStatus(QuestionSubmitStatusEnum.RUNNING.getCode());
        boolean update = questionSubmitService.updateById(updateObj);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "提交状态更新失败");
        }
        // =============

        // 3. 调用代码沙箱执行结果
        CodeSandBox codeSandBox = getCodeSandBox(CODE_SANDBOX_TYPE);
        // 3.1 设置执行代码参数和执行
        String judgeCaseStr = question.getJudgeCase();
        List<JudgeCase> judgeCases = JSONUtil.toList(judgeCaseStr, JudgeCase.class);
        List<String> judgeCasesInput = judgeCases.stream().map(JudgeCase::getInput).collect(Collectors.toList());
        ExecuteCodeRequest codeRequest = ExecuteCodeRequest.builder()
                .code(questionSubmit.getCode())
                .language(questionSubmit.getLanguage())
                .inputList(judgeCasesInput)
                .build();
        ExecuteCodeResponse executeCodeResponse = codeSandBox.executeCode(codeRequest);

        // 4  整理代码响应和判断
        JudgeContext judgeContext = JudgeContext.builder()
                .judgeInfo(executeCodeResponse.getJudgeInfo())
                .inputList(judgeCasesInput)
                .outputList(executeCodeResponse.getOutputList())
                .judgeCaseList(judgeCases)
                .question(question)
                .questionSubmit(questionSubmit)
                .build();

        // 判断结果
        JudgeInfo judgeInfo = new DefaultJudgeStrategy().doJudge(judgeContext);
        log.debug(JSONUtil.toJsonStr(judgeInfo));
        // 5. 修改数据库中的判题结果
        updateObj = QuestionSubmit.builder()
                .id(questionSubmit.getId())
                .status(QuestionSubmitStatusEnum.SUCCESS.getCode())
                .judgeInfo(JSONUtil.toJsonStr(judgeInfo))
                .build();
        boolean updated = questionSubmitService.updateById(updateObj);
        if (!updated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "提交状态更新失败");
        }

        // 6. 修改每日提交汇总，通过数
        try {
            Date submitCreateTime = questionSubmit.getCreateTime();
            String format = submitCreateTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().format(DATE_TIME_FORMATTER);
            if (judgeInfo.getMessage().equals(JudgeInfoMessageEnum.ACCEPTED.getValue())) {
                // 更新每日提交汇总
                userSubmitSummaryMapper.updateSubmitSummary(questionSubmit.getUserId(), format, 0, 1);
                // 更新题目通过数
                questionService.updateQuestionAcceptedNum(question.getId());
                // 更新用户通过的题目
                questionSubmitService.updatePassStatus(questionSubmit.getId(), QuestionPassStatusEnum.ACCEPTED);
                // 删除缓存
                stringRedisTemplate.delete(RedisKeyUtil.getUserPassedQuestionKey(questionSubmit.getUserId()));
            } else {
                // 标记提交的通过状态
                questionSubmitService.updatePassStatus(questionSubmit.getId(), QuestionPassStatusEnum.FAILED);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.debug("修改每日提交汇总，通过数 失败");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "修改每日提交汇总，通过数 失败");
        }
        return judgeInfo;
    }

    @Override
    public void setJudgeInfoFailed(Long id) {
        // 1. 信息检查
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "提交不存在");
        }

        // 2. 更新提交状态
        QuestionSubmit updateObj = new QuestionSubmit();
        updateObj.setId(id);
        updateObj.setStatus(QuestionSubmitStatusEnum.ERROR.getCode());
        updateObj.setJudgeInfo(JSONUtil.toJsonStr(JudgeInfo.builder()
                .message(QuestionSubmitStatusEnum.ERROR.getDescription())
                .build()));

        boolean update = questionSubmitService.updateById(updateObj);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "提交状态更新失败");
        }
    }

    @Autowired
    private ApplicationContext applicationContext;

    private CodeSandBox getCodeSandBox(String type) {
        switch (type) {
            case "native":
                return applicationContext.getBean(JavaNativeCodeSandBoxImpl.class);
            case "docker":
                return applicationContext.getBean(JavaDockerCodeSandBoxImpl.class);
            default:
                throw new IllegalArgumentException("无效的代码沙箱类型: " + type);
        }
    }

}
