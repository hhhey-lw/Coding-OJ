package com.longoj.top.model.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


@Data
public class UserSubmitSummaryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private String yearMonthDay;

    private Integer submitCount;
    private Integer acceptCount;

    /**
     * 更新时间
     */
    private Date updateTime;
}
