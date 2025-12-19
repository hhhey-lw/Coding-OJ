package com.longoj.top.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


@Data
public class UserCheckInVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private String yearMonth;

    private Integer bitmap;

    private List<UserSubmitSummaryVO> userSubmitSummaryVO;
}
