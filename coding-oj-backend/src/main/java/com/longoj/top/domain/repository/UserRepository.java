package com.longoj.top.domain.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.longoj.top.domain.entity.User;

public interface UserRepository {
    /**
     * 用户登录
     */
    User userLogin(String userAccount, String encryptPassword);

    /**
     * 通过用户账号获取用户
     */
    User getByAccount(String userAccount);

    /**
     * 添加用户
     */
    boolean addUser(User entity);

    /**
     * 更新用户密码
     */
    boolean updatePassword(Long id, String newPasswordSALT);

    /**
     * 分页查询用户
     */
    Page<User> page(String userName, String userRole, int current, int pageSize);
}
