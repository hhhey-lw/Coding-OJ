package com.longoj.top.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户每日提交统计
 **/
@Data
@TableName(value = "user_submit_summary")
public class UserSubmitSummary implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 日期，格式yyyy-MM-dd
     */
    private String yearMonthDay;

    /**
     * 提交次数
     */
    private Integer submitCount;

    /**
     * 通过次数
     */
    private Integer acceptCount;

    /**
     * 更新时间
     */
    private Date updateTime;
}
