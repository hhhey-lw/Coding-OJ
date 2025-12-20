package com.longoj.top.domain.entity.dto;

import lombok.Data;

@Data
public class UserPassedCountDTO {
    /**
     * 用户Id
     */
    private Long userId;
    /**
     * 通过题目数
     */
    private Integer passedCount;
    /**
     * 提交题目数
     */
    private Integer submitCount;
}
