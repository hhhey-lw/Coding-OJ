package com.longoj.top.domain.strategy;

import cn.hutool.json.JSONUtil;
import com.longoj.top.domain.entity.dto.JudgeContext;
import com.longoj.top.domain.entity.dto.JudgeCase;
import com.longoj.top.domain.entity.dto.JudgeConfig;
import com.longoj.top.domain.entity.dto.JudgeInfo;
import com.longoj.top.domain.entity.enums.JudgeInfoMessageEnum;

import java.util.List;
import java.util.stream.Collectors;

public class DefaultJudgeStrategy implements JudgeStrategy {
    @Override
    public JudgeInfo doJudge(JudgeContext judgeContext) {
        // 1. 先获取信息
        List<String> outputList = judgeContext.getOutputList();
        List<String> correctAns = judgeContext.getJudgeCaseList().stream().map(JudgeCase::getOutput).collect(Collectors.toList());
        JudgeInfo judgeInfo = judgeContext.getJudgeInfo();
        JudgeConfig judgeConfig = JSONUtil.toBean(judgeContext.getQuestion().getJudgeConfig(), JudgeConfig.class);

        JudgeInfo judgeInfoResponse = new JudgeInfo();
        judgeInfoResponse.setMemory(judgeInfo.getMemory());
        judgeInfoResponse.setTime(judgeInfo.getTime());

        // 2. 判断
        // 2.1 答案是否正确
        if (outputList.size() != correctAns.size()) {
            judgeInfoResponse.setMessage(JudgeInfoMessageEnum.WRONG_ANSWER.getValue());
            return judgeInfoResponse;
        }

        for (int i = 0; i < outputList.size(); i++) {
            String outputAnswer = outputList.get(i).trim().replace("\n", "");
            String correctAnswer = correctAns.get(i).trim().replace("\n", "");
            if (!outputAnswer.equals(correctAnswer)) {
                judgeInfoResponse.setMessage(JudgeInfoMessageEnum.WRONG_ANSWER.getValue());
                return judgeInfoResponse;}
        }

        // 2.2 题目限制
        Long runMemory = judgeInfo.getMemory();
        if (runMemory != null && judgeConfig.getMemoryLimit() < runMemory / 1024 / 1024) {
            judgeInfoResponse.setMessage(JudgeInfoMessageEnum.MEMORY_LIMIT_EXCEEDED.getValue());
            return judgeInfoResponse;
        }
        if (judgeInfo.getTime() != null && judgeConfig.getTimeLimit() < judgeInfo.getTime()) {
            judgeInfoResponse.setMessage(JudgeInfoMessageEnum.TIME_LIMIT_EXCEEDED.getValue());
            return judgeInfoResponse;
        }
        judgeInfoResponse.setMessage(JudgeInfoMessageEnum.ACCEPTED.getValue());
        return judgeInfoResponse;
    }
}
