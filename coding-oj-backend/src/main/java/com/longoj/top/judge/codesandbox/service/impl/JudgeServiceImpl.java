package com.longoj.top.judge.codesandbox.service.impl;

import cn.hutool.json.JSONUtil;
import com.longoj.top.common.ErrorCode;
import com.longoj.top.common.RedisKeyUtil;
import com.longoj.top.exception.BusinessException;
import com.longoj.top.judge.codesandbox.JudgeManager;
import com.longoj.top.judge.codesandbox.service.CodeSandBox;
import com.longoj.top.judge.codesandbox.CodeSandboxFactory;
import com.longoj.top.judge.codesandbox.model.ExecuteCodeRequest;
import com.longoj.top.judge.codesandbox.model.ExecuteCodeResponse;
import com.longoj.top.judge.codesandbox.model.JudgeContext;
import com.longoj.top.judge.codesandbox.service.JudgeService;
import com.longoj.top.mapper.UserSubmitSummaryMapper;
import com.longoj.top.model.dto.question.JudgeCase;
import com.longoj.top.model.dto.questionsubmit.JudgeInfo;
import com.longoj.top.model.entity.Question;
import com.longoj.top.model.entity.QuestionSubmit;
import com.longoj.top.model.enums.JudgeInfoMessageEnum;
import com.longoj.top.model.enums.QuestionSubmitStatusEnum;
import com.longoj.top.service.QuestionPassedService;
import com.longoj.top.service.QuestionService;
import com.longoj.top.service.QuestionSubmitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    private QuestionPassedService questionPassedService;

    @Resource
    private JudgeManager judgeManager;

    @Resource
    private CodeSandboxFactory codeSandboxFactory;

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
        if (!questionSubmit.getStatus().equals(QuestionSubmitStatusEnum.WAITING.getStatus())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "题目正在判题中");
        }

        // =============
        // 2. 更新提交状态
        QuestionSubmit updateObj = new QuestionSubmit();
        updateObj.setId(questionSubmit.getId());
        updateObj.setStatus(QuestionSubmitStatusEnum.RUNNING.getStatus());
        boolean update = questionSubmitService.updateById(updateObj);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "提交状态更新失败");
        }
        // =============

        // 3. 调用代码沙箱执行结果
        CodeSandBox codeSandBox = codeSandboxFactory.getCodeSandBox(CODE_SANDBOX_TYPE);
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
        JudgeInfo judgeInfo = judgeManager.doJudge(judgeContext);
        log.debug(JSONUtil.toJsonStr(judgeInfo));
        // 5. 修改数据库中的判题结果
        updateObj = QuestionSubmit.builder()
                .id(questionSubmit.getId())
                .status(QuestionSubmitStatusEnum.SUCCESS.getStatus())
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
                questionPassedService.addUserPassedQuestion(question.getId(), questionSubmit.getUserId());
                // 删除缓存
                stringRedisTemplate.delete(RedisKeyUtil.getUserPassedQuestionKey(questionSubmit.getUserId()));
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
        updateObj.setStatus(QuestionSubmitStatusEnum.ERROR.getStatus());
        updateObj.setJudgeInfo(JSONUtil.toJsonStr(JudgeInfo.builder()
                .message(QuestionSubmitStatusEnum.ERROR.getValue())
                .build()));

        boolean update = questionSubmitService.updateById(updateObj);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "提交状态更新失败");
        }
    }
}
