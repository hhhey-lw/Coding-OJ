package com.longoj.top.controller.dto.question;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionSubmitAddRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 提交代码的编程语言
     */
    private String language;

    /**
     * 提交的代码
     */
    private String code;

    /**
     * 题目 id
     */
    private Long questionId;

}
