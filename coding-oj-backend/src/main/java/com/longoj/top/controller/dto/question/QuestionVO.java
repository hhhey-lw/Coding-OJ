package com.longoj.top.controller.dto.question;

import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.longoj.top.controller.dto.user.UserVO;
import com.longoj.top.domain.entity.dto.JudgeConfig;
import com.longoj.top.domain.entity.Question;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

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
public class QuestionVO implements Serializable {
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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String answer;

    /**
     * 题目源码
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String sourceCode;

    /**
     * 题目提交数
     */
    private Integer submitNum;

    /**
     * 题目通过数
     */
    private Integer acceptedNum;

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
    private UserVO userVO;

    /**
     * 是否通过
     */
    private boolean isPassed;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 对象转包装类
     */
    public static QuestionVO convertToVo(Question question) {
        if (question == null) {
            return null;
        }
        QuestionVO questionVO = new QuestionVO();

        questionVO.setId(question.getId());
        questionVO.setTitle(question.getTitle());
        questionVO.setContent(question.getContent());
        questionVO.setDifficulty(question.getDifficulty());
        questionVO.setAnswer(question.getAnswer());
        questionVO.setSourceCode(question.getSourceCode());
        questionVO.setSubmitNum(question.getSubmitNum());
        questionVO.setAcceptedNum(question.getAcceptedNum());
        questionVO.setThumbNum(question.getThumbNum());
        questionVO.setFavourNum(question.getFavourNum());
        questionVO.setCreateTime(question.getCreateTime());
        questionVO.setUpdateTime(question.getUpdateTime());

        questionVO.setTags(JSONUtil.toList(question.getTags(), String.class));
        questionVO.setJudgeConfig(JSONUtil.toBean(question.getJudgeConfig(), JudgeConfig.class));
        return questionVO;
    }

}