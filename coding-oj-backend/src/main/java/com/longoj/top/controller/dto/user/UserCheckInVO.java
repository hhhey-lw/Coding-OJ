package com.longoj.top.controller.dto.user;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;


@Data
public class UserCheckInVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 年月 格式yyyy-MM
     */
    private String yearMonth;

    /**
     * 签到位图 一个月
     */
    private Integer bitmap;

    /**
     * 每日提交统计
     */
    private List<UserSubmitSummaryVO> userSubmitSummaryVO;
}
