package com.longoj.top.judge.codesandbox.strategy;

import com.longoj.top.judge.codesandbox.model.JudgeContext;
import com.longoj.top.model.dto.questionsubmit.JudgeInfo;

public interface JudgeStrategy {

    JudgeInfo doJudge(JudgeContext judgeContext);

}
