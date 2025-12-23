package com.longoj.top.controller.dto.user;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;


@Data
public class UserSubmitSummaryVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 日期，格式yyyy-MM-dd
     */
    private String yearMonthDay;

    /**
     * 提交题目数量
     */
    private Integer submitCount;

    /**
     * 通过题目数量
     */
    private Integer acceptCount;

}
