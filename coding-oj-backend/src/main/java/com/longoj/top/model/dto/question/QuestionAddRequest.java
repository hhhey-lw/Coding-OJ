package com.longoj.top.model.dto.question;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 题目
 * @TableName question
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionAddRequest implements Serializable {
    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 标签列表（json 数组）
     */
    private List<String> tags;

    private Integer difficulty;

    /**
     * 题目答案
     */
    private String answer;

    private String sourceCode;

    /**
     * 题目判题用例 (json 数组)
     */
    private List<JudgeCase> judgeCase;

    /**
     * 判题配置 (json 对象)
     */
    private JudgeConfig judgeConfig;

    private static final long serialVersionUID = 1L;
}