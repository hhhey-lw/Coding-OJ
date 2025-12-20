package com.longoj.top.controller.dto.user;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

import com.longoj.top.domain.entity.User;
import lombok.Data;

/**
 * 用户创建请求
 *
 */
@Data
public class UserAddRequest implements Serializable {

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户角色: user, admin
     */
    private String userRole;

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 将 UserAddRequest 请求体转换为 User 实体类
     *
     * @param request 用户创建请求体
     * @return 用户实体类
     */
    public static User toEntity(UserAddRequest request) {
        if (request == null) {
            return null;
        }
        User user = new User();
        user.setUserName(request.getUserName());
        user.setUserAccount(request.getUserAccount());
        user.setUserAvatar(request.getUserAvatar());
        user.setUserRole(request.getUserRole());
        user.setUserPassword(request.getUserPassword());
        return user;
    }

}