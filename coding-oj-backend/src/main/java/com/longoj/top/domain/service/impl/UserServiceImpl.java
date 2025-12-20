package com.longoj.top.domain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.longoj.top.controller.dto.user.UserAddRequest;
import com.longoj.top.domain.repository.UserRepository;
import com.longoj.top.infrastructure.exception.ErrorCode;
import com.longoj.top.infrastructure.exception.BusinessException;
import com.longoj.top.infrastructure.mapper.UserMapper;
import com.longoj.top.domain.entity.User;
import com.longoj.top.controller.dto.user.LoginUserVO;
import com.longoj.top.controller.dto.user.UserVO;
import com.longoj.top.domain.service.UserService;
import com.longoj.top.infrastructure.utils.JwtTokenUtil;

import java.util.Random;
import java.util.UUID;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.longoj.top.infrastructure.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
 * 用户服务实现
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 盐值，混淆密码
     */
    public static final String SALT = "Coding-OJ";
    public static final String USER_NAME_PREFIX = "用户";
    public static final String USER_DEFAULT_AVATAR = "http://longcoding.top:8101/api/images/avatar-%d.jpg";
    public static final String USER_DEFAULT_PROFILE = "这个人很神秘，什么都没有留下";

    @Resource
    private UserRepository userRepository;

    @Override
    public User save(UserAddRequest userAddRequest) {
        User user = UserAddRequest.toEntity(userAddRequest);
        userRepository.addUser(user);
        return user;
    }

    @Override
    public long userRegister(String userAccount, String userPassword) {
        // 1. 校验账号是否重复
        User user = userRepository.getByAccount(userAccount);
        if (user != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }

        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        // 3. 插入数据
        User entity = User.buildEntity(userAccount, encryptPassword,
                USER_NAME_PREFIX + UUID.randomUUID().toString().substring(0, 8),
                String.format(USER_DEFAULT_AVATAR, new Random().nextInt(5) + 1),
                USER_DEFAULT_PROFILE);
        boolean saveResult = userRepository.addUser(entity);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }
        return entity.getId();
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword) {
        // 1. 校验
        if (userAccount.length() < 6) {
            throw new BusinessException("账号不能少于6位");
        }
        if (userPassword.length() < 6) {
            throw new BusinessException("密码不能少于6位");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        // 3. 登陆
        User user = userRepository.userLogin(userAccount, encryptPassword);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }

        // 4. 生成登陆令牌
        String token = JwtTokenUtil.generateToken(user.getId());

        // 5. 返回结果
        return User.toVO(user, token);
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 如果使用了Redis缓存进行登陆校验，则需要清除缓存
        return true;
    }

    @Override
    public boolean updateUserPwd(String oldPwd, String newPwd) {
        User loginUser = UserContext.getUser();
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + oldPwd).getBytes());
        String newPasswordSALT = DigestUtils.md5DigestAsHex((SALT + newPwd).getBytes());

        User user = userRepository.userLogin(loginUser.getUserAccount(), encryptPassword);
        if (user == null) {
            throw new BusinessException("旧密码错误");
        }

        return userRepository.updatePassword(loginUser.getId(), newPasswordSALT);
    }

    @Override
    public Boolean updateUserInfo(String userName, String userAvatar, String userProfile) {
        return null;
    }

    @Override
    public UserVO getUserVOByUserId(Long userId) {
        User user = getById(userId);
        return User.toVO(user);
    }

    @Override
    public Page<User> page(String userName, String userRole, int current, int pageSize) {
        return userRepository.page(userName, userRole, current, pageSize);
    }


}
