package com.longoj.top.controller.dto.question;

import com.longoj.top.controller.dto.PageRequest;
import com.longoj.top.domain.entity.enums.QuestionSubmitLanguageEnum;
import com.longoj.top.domain.entity.enums.QuestionSubmitStatusEnum;
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
    private QuestionSubmitLanguageEnum language;

    /**
     * 提交状态
     */
    private QuestionSubmitStatusEnum status;

    /**
     * 题目 ID
     */
    private Integer questionId;

    @Serial
    private static final long serialVersionUID = 1L;

}
