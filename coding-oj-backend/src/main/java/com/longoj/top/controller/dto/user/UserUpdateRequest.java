package com.longoj.top.controller.dto.user;

import java.io.Serial;
import java.io.Serializable;

import com.longoj.top.domain.entity.User;
import lombok.Data;

/**
 * 用户更新请求
 *
 */
@Data
public class UserUpdateRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 将 UserUpdateRequest 转换为 User 实体类
     *
     * @param req 用户更新请求
     * @return User 实体类
     */
    public static User toUser(UserUpdateRequest req) {
        if (req == null) {
            return null;
        }
        User user = new User();
        user.setId(req.getId());
        user.setUserName(req.getUserName());
        user.setUserAvatar(req.getUserAvatar());
        user.setUserProfile(req.getUserProfile());
        user.setUserRole(req.getUserRole());
        return user;
    }

}