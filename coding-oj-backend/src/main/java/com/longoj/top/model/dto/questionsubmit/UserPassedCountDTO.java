package com.longoj.top.model.dto.questionsubmit;

import lombok.Data;

@Data
public class UserPassedCountDTO {
    private Long userId;
    private Integer passedCount;
    private Integer submitCount;
}
