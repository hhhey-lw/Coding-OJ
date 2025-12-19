package com.longoj.top.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


@Data
@TableName(value = "user_submit_summary")
public class UserSubmitSummary implements Serializable {

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
