package com.longoj.top.model.dto.questionsubmit;

import com.longoj.top.common.PageRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// @ApiModel("题目查询实体")
public class QuestionSubmitQueryRequest extends PageRequest implements Serializable {

    private String language;

    private Integer status;

    private Integer questionId;

    private Long userId;

    private static final long serialVersionUID = 1L;

}
