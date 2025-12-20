package com.longoj.top.controller.dto.user;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UserUpdatePwdRequest implements Serializable {
    /**
     *  用户旧密码
     */
    private String oldPwd;
    /**
     * 用户新密码
     */
    private String newPwd;

    @Serial
    private static final long serialVersionUID = 1L;
}
