package com.longoj.top.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserUpdatePwdRequest implements Serializable {
    private String oldPwd;
    private String newPwd;

    private static final long serialVersionUID = 1L;
}
