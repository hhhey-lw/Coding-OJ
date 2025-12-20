package com.longoj.top.domain.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.longoj.top.domain.entity.User;
import com.longoj.top.domain.repository.UserRepository;
import com.longoj.top.infrastructure.mapper.UserMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class UserRepositoryImpl implements UserRepository {

    @Resource
    private UserMapper userMapper;

    @Override
    public User userLogin(String userAccount, String encryptPassword) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserAccount, userAccount);
        queryWrapper.eq(User::getUserPassword, encryptPassword);
        queryWrapper.eq(User::getIsDelete, Boolean.FALSE);
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    public User getByAccount(String userAccount) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserAccount, userAccount);
        queryWrapper.eq(User::getIsDelete, Boolean.FALSE);
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    public boolean addUser(User entity) {
        return userMapper.insert(entity) == 1;
    }

    @Override
    public boolean updatePassword(Long id, String newPasswordSALT) {
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(User::getUserPassword, newPasswordSALT);
        updateWrapper.eq(User::getId, id);
        updateWrapper.eq(User::getIsDelete, Boolean.FALSE);
        return userMapper.update(null, updateWrapper) == 1;
    }

    @Override
    public Page<User> page(String userName, String userRole, int current, int pageSize) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotBlank(userName), User::getUserName, userName);
        queryWrapper.eq(StringUtils.isNotBlank(userRole), User::getUserRole, userRole);
        queryWrapper.eq(User::getIsDelete, Boolean.FALSE);
        return userMapper.selectPage(new Page<>(current, pageSize), queryWrapper);
    }
}
