package com.longoj.top.controller.dto.question;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.longoj.top.domain.entity.Question;
import com.longoj.top.domain.entity.User;
import com.longoj.top.domain.entity.dto.JudgeCase;
import com.longoj.top.domain.entity.dto.JudgeConfig;
import com.longoj.top.infrastructure.utils.UserContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 题目
 *
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

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 构建 Question 实体
     */
    public static Question buildEntity(QuestionAddRequest questionAddRequest) {
        Question question = new Question();
        question.setTitle(questionAddRequest.getTitle());
        question.setContent(questionAddRequest.getContent());
        question.setDifficulty(questionAddRequest.getDifficulty());
        question.setAnswer(questionAddRequest.getAnswer());
        question.setSourceCode(questionAddRequest.getSourceCode());

        if (CollectionUtil.isNotEmpty(questionAddRequest.getTags())) {
            question.setTags(JSONUtil.toJsonStr(questionAddRequest.getTags()));
        }
        if (CollectionUtil.isNotEmpty(questionAddRequest.getJudgeCase())) {
            question.setJudgeCase(JSONUtil.toJsonStr(questionAddRequest.getJudgeCase()));
        }
        if (Objects.nonNull(questionAddRequest.getJudgeConfig())) {
            question.setJudgeConfig(JSONUtil.toJsonStr(questionAddRequest.getJudgeConfig()));
        }
        question.setUserId(UserContext.getUser().getId());
        question.setFavourNum(0);
        question.setThumbNum(0);

        return question;
    }

}