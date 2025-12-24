package com.longoj.top.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

import com.longoj.top.controller.dto.user.LoginUserVO;
import com.longoj.top.controller.dto.user.UserVO;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * 用户
 */
@Data
@TableName(value = "user")
public class User implements Serializable {

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 用户密码
     */
    private String userPassword;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 转换为已登录用户视图
     *
     * @param user 用户实体
     * @param token 登录令牌
     * @return 已登录用户视图
     */
    public static LoginUserVO toVO(User user, String token) {
        LoginUserVO vo = new LoginUserVO();
        vo.setId(user.getId());
        vo.setUserName(user.getUserName());
        vo.setUserAvatar(user.getUserAvatar());
        vo.setUserProfile(user.getUserProfile());
        vo.setUserRole(user.getUserRole());
        vo.setCreateTime(user.getCreateTime());
        vo.setUpdateTime(user.getUpdateTime());
        vo.setToken(token);
        return vo;
    }

    /**
     * 转换为已登录用户视图
     *
     * @param user 用户实体
     * @return 已登录用户视图
     */
    public static UserVO toVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUserName(user.getUserName());
        vo.setUserAvatar(user.getUserAvatar());
        vo.setUserProfile(user.getUserProfile());
        vo.setUserRole(user.getUserRole());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }

    /**
     * 获取用户实体
     *
     * @param userAccount    用户账号
     * @param encryptPassword 加密后的密码
     * @return 用户实体
     */
    public static User buildEntity(String userAccount, String encryptPassword,
                                   String userName, String userAvatar, String userProfile) {
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName(userName);
        user.setUserAvatar(userAvatar);
        user.setUserProfile(userProfile);
        return user;
    }

    /**
     * 获取用户实体
     */
    public static User buildEntity(Long id, String userName, String userAvatar, String userProfile) {
        User user = new User();
        user.setId(id);
        if (StringUtils.isNotBlank(userName)) {
            user.setUserName(userName);
        }
        if (StringUtils.isNotBlank(userAvatar)) {
            user.setUserAvatar(userAvatar);
        }
        if (StringUtils.isNotBlank(userProfile)) {
            user.setUserProfile(userProfile);
        }
        return user;
    }
}