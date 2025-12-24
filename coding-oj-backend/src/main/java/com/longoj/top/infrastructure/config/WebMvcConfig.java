package com.longoj.top.infrastructure.config;

import com.longoj.top.controller.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Autowired
    private JwtInterceptor jwtInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**") // 拦截所有开头的请求
                .excludePathPatterns(
                    // 用户相关 - 不需要登录
                    "/user/login",
                    "/user/register",
                    // 题目相关 - 不需要登录
                    "/question/list/page/vo",
                    "/question/tag/**",
                    "/question/submit/list/page",
                    "/question/submit/topPassed/**",
                    // 帖子相关 - 不需要登录
                    "/post/page/vo",
                    "/post/comment/list/page",
                    // Swagger 相关路径
                    "/doc.html",
                    "/webjars/**",
                    "/swagger-resources/**",
                    "/v2/api-docs",
                    "/v3/api-docs",
                    "/swagger-ui/**",
                    "/public/**"
                );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 覆盖所有请求
        registry.addMapping("/**")
                // 允许发送 Cookie
                .allowCredentials(true)
                // 放行哪些域名（必须用 patterns，否则 * 会和 allowCredentials 冲突）
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("*");
    }

}