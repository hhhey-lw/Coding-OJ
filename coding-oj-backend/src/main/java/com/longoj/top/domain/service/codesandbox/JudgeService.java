package com.longoj.top.domain.service.codesandbox;

import com.longoj.top.domain.entity.dto.JudgeInfo;
import com.longoj.top.domain.entity.QuestionSubmit;

public interface JudgeService {

    /**
     * 判题
     *
     * @param questionSubmit 提交的题目
     * @return 判题信息
     */
    JudgeInfo doJudge(QuestionSubmit questionSubmit);

    /**
     * 设置判题信息为失败
     *
     * @param id 提交ID
     */
    void setJudgeInfoFailed(Long id);
}
