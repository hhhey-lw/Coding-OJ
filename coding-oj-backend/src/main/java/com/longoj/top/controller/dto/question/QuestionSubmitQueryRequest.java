package com.longoj.top.controller.dto.question;

import com.longoj.top.controller.dto.PageRequest;
import io.swagger.annotations.ApiModel;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Data
@ApiModel("题目查询实体")
@EqualsAndHashCode(callSuper = true)
public class QuestionSubmitQueryRequest extends PageRequest implements Serializable {
    /**
     * 编程语言
     */
    private String language;

    /**
     * 提交状态
     */
    private Integer status;

    /**
     * 题目 ID
     */
    private Integer questionId;

    /**
     * 用户 ID
     */
    private Long userId;

    @Serial
    private static final long serialVersionUID = 1L;

}
