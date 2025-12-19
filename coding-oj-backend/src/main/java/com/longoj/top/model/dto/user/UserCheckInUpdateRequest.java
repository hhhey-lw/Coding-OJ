package com.longoj.top.model.dto.user;

import lombok.Data;

import java.io.Serializable;


@Data
public class UserCheckInUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private String yearMonth;

    private Integer day;
}
