package com.longoj.top.judge.codesandbox.service;

import com.longoj.top.model.dto.questionsubmit.JudgeInfo;
import com.longoj.top.model.entity.QuestionSubmit;

public interface JudgeService {

    JudgeInfo doJudge(QuestionSubmit questionSubmit);

    void setJudgeInfoFailed(Long id);
}
