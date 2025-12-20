package com.longoj.top.controller.dto.question;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionSubmitAddRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String language;

    private String code;

    private Long questionId;

}
