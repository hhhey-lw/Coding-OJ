package com.longoj.top.domain.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.longoj.top.controller.dto.user.UserAddRequest;
import com.longoj.top.domain.entity.User;
import com.longoj.top.controller.dto.user.LoginUserVO;
import com.longoj.top.controller.dto.user.UserVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户服务
 */
public interface UserService extends IService<User> {

    /**
     * 添加用户
     */
    User save(UserAddRequest userAddRequest);

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 更新用户密码
     */
    boolean updateUserPwd(String oldPwd, String newPwd, String confirmNewPwd);

    /**
     * 更新用户信息
     */
    Boolean updateUserInfo(String userName, String userAvatar, String userProfile);

    /**
     * 根据用户 id 获取用户视图
     */
    UserVO getUserVOByUserId(Long userId);

    /**
     * 分页查询用户
     */
    Page<User> page(String userName, String userRole, int current, int pageSize);

}
