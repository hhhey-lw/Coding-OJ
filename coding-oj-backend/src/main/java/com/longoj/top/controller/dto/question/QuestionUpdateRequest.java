package com.longoj.top.controller.dto.question;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.longoj.top.domain.entity.Question;
import com.longoj.top.domain.entity.dto.JudgeCase;
import com.longoj.top.domain.entity.dto.JudgeConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 题目
 * @TableName question
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionUpdateRequest implements Serializable {
    /**
     * id
     */
    private Long id;

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

    /**
     * 题目难度
     */
    private Integer difficulty;

    /**
     * 题目答案
     */
    private String answer;

    /**
     * 题目提交数
     */
    private Integer submitNum;

    /**
     * 题目通过数
     */
    private Integer acceptedNum;

    /**
     * 题目判题用例 (json 数组)
     */
    private List<JudgeCase> judgeCase;

    /**
     * 判题配置 (json 对象)
     */
    private JudgeConfig judgeConfig;

    /**
     * 点赞数
     */
    private Integer thumbNum;

    /**
     * 收藏数
     */
    private Integer favourNum;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;


    private String sourceCode;

    /**
     * 是否删除
     */
    private Integer isDelete;

    @Serial
    private static final long serialVersionUID = 1L;

    public static Question toEntity(QuestionUpdateRequest questionUpdateRequest) {
        if (questionUpdateRequest == null) {
            return null;
        }
        Question question = new Question();
        question.setId(questionUpdateRequest.getId());
        question.setTitle(questionUpdateRequest.getTitle());
        question.setContent(questionUpdateRequest.getContent());
        question.setDifficulty(questionUpdateRequest.getDifficulty());
        question.setAnswer(questionUpdateRequest.getAnswer());
        question.setSubmitNum(questionUpdateRequest.getSubmitNum());
        question.setAcceptedNum(questionUpdateRequest.getAcceptedNum());
        question.setThumbNum(questionUpdateRequest.getThumbNum());
        question.setFavourNum(questionUpdateRequest.getFavourNum());
        question.setUserId(questionUpdateRequest.getUserId());
        question.setCreateTime(questionUpdateRequest.getCreateTime());
        question.setUpdateTime(questionUpdateRequest.getUpdateTime());
        question.setSourceCode(questionUpdateRequest.getSourceCode());
        question.setIsDelete(questionUpdateRequest.getIsDelete());

        // 标签列表
        if (CollectionUtil.isNotEmpty(questionUpdateRequest.getTags())) {
            question.setTags(JSONUtil.toJsonStr(questionUpdateRequest.getTags()));
        }
        // 判题用例
        List<JudgeCase> judgeCase = questionUpdateRequest.getJudgeCase();
        if (judgeCase != null) {
            question.setJudgeCase(JSONUtil.toJsonStr(judgeCase));
        }
        // 判题配置
        JudgeConfig judgeConfig = questionUpdateRequest.getJudgeConfig();
        if (judgeConfig != null) {
            question.setJudgeConfig(JSONUtil.toJsonStr(judgeConfig));
        }

        return question;
    }

}