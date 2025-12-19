package com.longoj.top.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum QuestionSubmitStatusEnum {

    WAITING(0, "等待中"),
    RUNNING(1, "判题中"),
    SUCCESS(2, "执行成功"),
    ERROR(3, "执行失败");

    private final Integer status;

    private final String value;

    public static String getValue(Integer status) {
        switch (status) {
            case 0: return WAITING.value;
            case 1: return RUNNING.value;
            case 2: return SUCCESS.value;
            default: return ERROR.value;
        }
    }

}
