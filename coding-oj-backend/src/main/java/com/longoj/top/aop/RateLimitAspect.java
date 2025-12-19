package com.longoj.top.aop;

import com.google.common.util.concurrent.RateLimiter;
import com.longoj.top.annotation.RateLimit;
import com.longoj.top.common.ErrorCode;
import com.longoj.top.exception.BusinessException;
import com.longoj.top.model.entity.User;
import com.longoj.top.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.longoj.top.constant.UserConstant.USER_LOGIN_STATE;

@Aspect
@Component
@Slf4j
public class RateLimitAspect {
    @Resource
    private UserService userService;

    // 使用 ConcurrentHashMap 存储每个方法的 RateLimiter 实例
    private final ConcurrentHashMap<String, RateLimiter> rateLimiterMap = new ConcurrentHashMap<>();

    @Around("@annotation(rateLimit)")
    public Object doAround(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        // 1. 从 RequestContextHolder 获取当前请求
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        User currentUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        long userId = currentUser.getId();

        // 获取方法签名作为Key
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String key = signature.getDeclaringTypeName() + ":" + signature.getName() + ":" + userId;

        // 获取或创建当前方法的 RateLimiter
        RateLimiter limiter = rateLimiterMap.computeIfAbsent(key, k -> RateLimiter.create(rateLimit.permitsPerSecond()));

        // 尝试获取令牌
        log.info("尝试从令牌桶获取令牌, key: {}", key);
        if (limiter.tryAcquire(1, 0, TimeUnit.SECONDS)) {
            // 获取到令牌，执行原方法
            return joinPoint.proceed();
        } else {
            // 未获取到令牌，抛出异常，实现限流
            log.warn("获取令牌失败，触发限流, key: {}", key);
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST, rateLimit.message());
        }
    }
}