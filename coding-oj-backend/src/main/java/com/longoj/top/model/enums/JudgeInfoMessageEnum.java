package com.longoj.top.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum JudgeInfoMessageEnum {
    ACCEPTED("Accepted", "答案正确"),
    WRONG_ANSWER("Wrong Answer", "答案错误"),
    COMPILE_ERROR("Compile Error", "编译错误"),
    MEMORY_LIMIT_EXCEEDED("Memory Limit Exceeded", "内存溢出"),
    TIME_LIMIT_EXCEEDED("Time Limit Exceeded", "超时"),
    PRESENTATION_ERROR("Presentation Error", "展示错误（多空格之类的）"),
    OUTPUT_LIMIT_EXCEEDED("Output Limit Exceeded", "输出溢出"),
    WAITING("Waiting", "等待中"),
    DANGEROUS_OPERATION("Dangerous Operation", "危险操作"),
    RUNTIME_ERROR("Runtime Error", "运行错误（用户程序运行出现问题）"),
    SYSTEM_ERROR("System Error", "系统错误（做系统的人的问题）");

    private final String Text;
    private final String value;

    public static String getValue(String text) {
        for (JudgeInfoMessageEnum judgeInfoMessageEnum : values()) {
            if (judgeInfoMessageEnum.Text.equals(text))
                return judgeInfoMessageEnum.value;
        }
        return WRONG_ANSWER.value;
    }
}
