package com.longoj.top.domain.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum QuestionPassStatusEnum {
    /**
     * 未通过
     */
    FAILED(0, "FAILED"),
    /**
     * 通过
     */
    ACCEPTED(1, "ACCEPTED"),
    ;
    private final Integer code;
    private final String enCode;
}
