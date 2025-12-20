package com.longoj.top.domain.entity.dto;

import com.longoj.top.domain.entity.Question;
import com.longoj.top.domain.entity.QuestionSubmit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 上下文（用于定义在策略中传递的参数）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JudgeContext {

    /**
     * 判题信息
     */
    private JudgeInfo judgeInfo;

    /**
     * 输入用例列表
     */
    private List<String> inputList;

    /**
     * 输出用例列表
     */
    private List<String> outputList;

    /**
     * 题目用例列表
     */
    private List<JudgeCase> judgeCaseList;

    /**
     * 题目信息
     */
    private Question question;

    /**
     * 提交信息
     */
    private QuestionSubmit questionSubmit;

}
