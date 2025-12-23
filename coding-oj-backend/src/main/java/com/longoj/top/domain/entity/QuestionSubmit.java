package com.longoj.top.domain.entity;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.longoj.top.controller.dto.question.QuestionSubmitAddRequest;
import com.longoj.top.domain.entity.dto.JudgeInfo;
import com.longoj.top.domain.entity.enums.QuestionPassStatusEnum;
import com.longoj.top.domain.entity.enums.QuestionSubmitStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 题目提交记录
 *
 * @TableName question_submit
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value ="question_submit")
public class QuestionSubmit implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 编程语言
     */
    private String language;

    /**
     * 用户代码
     */
    private String code;

    /**
     * 判题信息（json 对象）
     */
    private String judgeInfo;

    /**
     * 判题状态（0 - 待判题、1 - 判题中、2 - 成功、3 - 失败）
     */
    private Integer status;

    /**
     * 通过状态
     */
    private String passStatus;

    /**
     * 题目 id
     */
    private Long questionId;

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

    /**
     * 是否删除
     */
    private Integer isDelete;

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 转换为实体
     */
    public static QuestionSubmit toEntity(QuestionSubmitAddRequest questionSubmitAddRequest, Question question, User loginUser) {
        return QuestionSubmit.builder()
                .questionId(question.getId())
                .code(questionSubmitAddRequest.getCode())
                .language(questionSubmitAddRequest.getLanguage())
                .judgeInfo(JSONUtil.toJsonStr(JudgeInfo.builder()
                        .memory(null)
                        .message(QuestionSubmitStatusEnum.WAITING.name())
                        .time(null)
                        .build()))
                .userId(loginUser.getId())
                .status(QuestionSubmitStatusEnum.WAITING.getCode())
                .build();
    }

}