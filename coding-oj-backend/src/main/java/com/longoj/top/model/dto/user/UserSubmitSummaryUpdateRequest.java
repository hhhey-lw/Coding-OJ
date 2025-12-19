package com.longoj.top.model.dto.user;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


@Data
public class UserSubmitSummaryUpdateRequest {


    private Long userId;

    private String yearMonthDay;

    private Integer submitCount;
    private Integer acceptCount;
}
