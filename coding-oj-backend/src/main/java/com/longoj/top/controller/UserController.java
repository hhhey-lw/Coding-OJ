package com.longoj.top.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.longoj.top.domain.service.UserCheckInService;
import com.longoj.top.infrastructure.aop.annotation.AuthCheck;
import com.longoj.top.controller.dto.BaseResponse;
import com.longoj.top.controller.dto.DeleteRequest;
import com.longoj.top.infrastructure.exception.ErrorCode;
import com.longoj.top.infrastructure.utils.ResultUtils;
import com.longoj.top.domain.entity.constant.UserConstant;
import com.longoj.top.controller.dto.user.*;
import com.longoj.top.infrastructure.exception.BusinessException;
import com.longoj.top.infrastructure.utils.ThrowUtils;
import com.longoj.top.domain.entity.User;
import com.longoj.top.controller.dto.user.LoginUserVO;
import com.longoj.top.controller.dto.user.UserVO;
import com.longoj.top.domain.service.UserService;

import java.util.List;
import java.util.UUID;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.longoj.top.infrastructure.utils.JwtTokenUtil;
import com.longoj.top.infrastructure.utils.UserContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.longoj.top.domain.service.impl.UserServiceImpl.SALT;

/**
 * 用户接口
 *
 */
@Slf4j
@Api("用户接口")
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private UserCheckInService userCheckInService;

    // region 登录相关

    /**
     * 用户注册
     */
    @ApiOperation("用户注册")
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();

        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }

        return ResultUtils.success(userService.userRegister(userAccount, userPassword));
    }

    /**
     * 用户登录
     */
    @ApiOperation("用户登录")
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest) {
        if (StringUtils.isBlank(userLoginRequest.getUserAccount())) {
            throw new BusinessException("账号不能为空");
        }
        if (StringUtils.isBlank(userLoginRequest.getUserPassword())) {
            throw new BusinessException("密码不能为空");
        }
        return ResultUtils.success(userService.userLogin(userLoginRequest.getUserAccount(), userLoginRequest.getUserPassword()));
    }

    /**
     * 用户登出
     */
    @ApiOperation("用户登出")
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        return ResultUtils.success(userService.userLogout(request));
    }
    // endregion

    // region 增删改查

    @ApiOperation("创建用户")
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> addUser(@RequestBody UserAddRequest userAddRequest) {
        return ResultUtils.success(userService.save(userAddRequest));
    }

    @ApiOperation("删除用户")
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        return ResultUtils.success(userService.removeById(deleteRequest.getId()));
    }

    @ApiOperation("更新用户")
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.updateById(UserUpdateRequest.toUser(userUpdateRequest));
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @ApiOperation("更新密码")
    @PostMapping("/update/pwd")
    public BaseResponse<Boolean> updateUserPwd(@RequestBody UserUpdatePwdRequest userUpdatePwdRequest) {
        return ResultUtils.success(userService.updateUserPwd(userUpdatePwdRequest.getOldPwd(),
                userUpdatePwdRequest.getNewPwd(),
                userUpdatePwdRequest.getConfirmNewPwd()));
    }

    @ApiOperation("更新个人信息")
    @PostMapping("/update/my")
    public BaseResponse<Boolean> updateMyUser(@RequestBody UserUpdateMyRequest userUpdateMyRequest) {
        return ResultUtils.success(userService.updateUserInfo(userUpdateMyRequest.getUserName(),
                userUpdateMyRequest.getUserAvatar(),
                userUpdateMyRequest.getUserProfile()));
    }

    /**
     * 根据 id 获取用户（仅管理员）
     */
    @ApiOperation("查询用户信息")
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(Long id) {
        if (null == id || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据 id 获取包装类
     */
    @ApiOperation("获取用户包装类")
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id) {
        BaseResponse<User> response = getUserById(id);
        return ResultUtils.success(UserVO.toVO(response.getData()));
    }

    /**
     * 分页获取用户列表（仅管理员）
     */
    @ApiOperation("分页获取用户")
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<User>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest) {
        return ResultUtils.success(userService.page(userQueryRequest.getUserName(), userQueryRequest.getUserRole(), userQueryRequest.getCurrent(), userQueryRequest.getPageSize()));
    }
    // endregion

    @ApiOperation("获取签到信息")
    @GetMapping("/check-in/info")
    public BaseResponse<UserCheckInVO> getUserCheckInByUserIdAndYearMonth(String yearMonth) {
        Long userId = UserContext.getUser().getId();
        return ResultUtils.success(userCheckInService.getUserCheckInByUserIdAndYearMonth(userId, yearMonth));
    }

}
