package com.longoj.top.controller.dto.user;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


/**
 * 用户签到信息更新请求
 */
@Data
public class UserCheckInUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 签到年月，格式：yyyy-MM-dd
     */
    private String dateTime;

}
