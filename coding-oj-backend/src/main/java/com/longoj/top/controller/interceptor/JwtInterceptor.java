package com.longoj.top.controller.interceptor;

import com.longoj.top.infrastructure.exception.ErrorCode;
import com.longoj.top.infrastructure.exception.BusinessException;
import com.longoj.top.domain.entity.User;
import com.longoj.top.domain.service.UserService;
import com.longoj.top.infrastructure.utils.JwtTokenUtil;
import com.longoj.top.infrastructure.utils.UserContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Resource
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        System.err.println("执行拦截器！");
        // 1. 获取请求头中的token
        String token = request.getHeader(JwtTokenUtil.tokenHeader);
        if (token == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "未登录");
        }
        
        // 2. 解析token
        if (!JwtTokenUtil.validateToken(token)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "token已过期");
        }
        
        // 3. 解析用户信息
        Long userId = JwtTokenUtil.getUserIdFromToken(token);
        // 从数据库或其他存储获取完整用户信息
        User user = userService.getById(userId);
        
        // 4. 将用户信息存入ThreadLocal
        UserContext.setUser(user);
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, 
                               HttpServletResponse response, 
                               Object handler, Exception ex) {
        // 请求完成后清除ThreadLocal中的用户信息，防止内存泄漏
        UserContext.clear();
    }
}