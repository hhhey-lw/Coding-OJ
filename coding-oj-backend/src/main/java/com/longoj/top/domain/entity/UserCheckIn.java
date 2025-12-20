package com.longoj.top.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;


/**
 * 用户签到表
 */
@Data
@TableName(value = "user_check_in")
public class UserCheckIn implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户Id
     */
    private Long userId;

    /**
     * 签到年月，格式yyyy--MM
     */
    private String yearMonth;

    /**
     * 签到位图
     */
    private Integer bitmap;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建空的签到记录对象
     */
    public static UserCheckIn createEmpty(Long userId, String yearMonth) {
        UserCheckIn userCheckIn = new UserCheckIn();
        userCheckIn.setUserId(userId);
        userCheckIn.setYearMonth(yearMonth);
        userCheckIn.setBitmap(0);
        return userCheckIn;
    }
}
