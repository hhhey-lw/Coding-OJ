package com.longoj.top.config;

import com.longoj.top.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// @Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    // @Autowired
    private JwtInterceptor jwtInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**") // 拦截所有开头的请求
                .excludePathPatterns(
                    "/user/login",
                    "/user/login/token",
                    "/user/register",
                    "/question/list/page/vo",
                    "/question_submit/list/page",
                    // Swagger 相关路径
                    "/doc.html",
                    "/webjars/**",
                    "/swagger-resources/**",
                    "/v2/api-docs",
                    "/v3/api-docs",
                    "/swagger-ui/**",
                    "/public/**"
                ); // 排除登录注册等公开接口
    }
}