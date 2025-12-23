package com.longoj.top.domain.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum QuestionSubmitStatusEnum {

    WAITING(0, "等待中"),
    RUNNING(1, "判题中"),
    SUCCESS(2, "执行成功"),
    ERROR(3, "执行失败");

    private final Integer code;

    private final String Description;

    public static QuestionSubmitStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (QuestionSubmitStatusEnum statusEnum : QuestionSubmitStatusEnum.values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum;
            }
        }
        return null;
    }

}
