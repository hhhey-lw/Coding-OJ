package com.longoj.top.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


@Data
@TableName(value = "user_check_in")
public class UserCheckIn implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private String yearMonth;

    private Integer bitmap;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
