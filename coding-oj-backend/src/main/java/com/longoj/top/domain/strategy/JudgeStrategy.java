package com.longoj.top.domain.strategy;

import com.longoj.top.domain.entity.dto.JudgeContext;
import com.longoj.top.domain.entity.dto.JudgeInfo;

public interface JudgeStrategy {

    JudgeInfo doJudge(JudgeContext judgeContext);

}
