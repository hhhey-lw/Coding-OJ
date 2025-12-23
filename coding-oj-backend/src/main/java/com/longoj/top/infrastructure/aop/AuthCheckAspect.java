package com.longoj.top.infrastructure.aop;

import com.longoj.top.infrastructure.aop.annotation.AuthCheck;
import com.longoj.top.infrastructure.exception.ErrorCode;
import com.longoj.top.infrastructure.exception.BusinessException;
import com.longoj.top.domain.entity.User;
import com.longoj.top.domain.entity.enums.UserRoleEnum;
import com.longoj.top.domain.service.UserService;
import javax.annotation.Resource;

import com.longoj.top.infrastructure.utils.UserContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * 权限校验 AOP
 *
 */
@Aspect
@Component
public class AuthCheckAspect {

    /**
     * 执行拦截
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        // 当前登录用户
        User loginUser = UserContext.getUser();
        UserRoleEnum mustRoleEnum = UserRoleEnum.getByCode(mustRole);
        // 不需要权限，放行
        if (mustRoleEnum == null) {
            return joinPoint.proceed();
        }
        // 必须有该权限才通过
        UserRoleEnum userRoleEnum = UserRoleEnum.getByCode(loginUser.getUserRole());
        if (userRoleEnum == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 如果被封号，直接拒绝
        if (UserRoleEnum.BAN.equals(userRoleEnum)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 必须有管理员权限
        if (UserRoleEnum.ADMIN.equals(mustRoleEnum)) {
            // 用户没有管理员权限，拒绝
            if (!UserRoleEnum.ADMIN.equals(userRoleEnum)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }
        // 通过权限校验，放行
        return joinPoint.proceed();
    }
}

