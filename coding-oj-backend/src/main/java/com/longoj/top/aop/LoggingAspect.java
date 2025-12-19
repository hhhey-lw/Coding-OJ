package com.longoj.top.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);
    private final ThreadLocal<Long> startTime = new ThreadLocal<>();


    // 定义切点
    @Pointcut("execution(public * com.longoj.top.controller..*.*(..))")
    public void controllerAndServiceLog() {
    }

    @Before("controllerAndServiceLog()")
    public void logBefore(JoinPoint joinPoint) {
        startTime.set(System.currentTimeMillis());
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        logger.info("==> 方法入参: {}.{}() with argument[s] = {}", className, methodName, Arrays.toString(args));
    }

    @AfterReturning(pointcut = "controllerAndServiceLog()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        long executionTime = System.currentTimeMillis() - startTime.get();
        startTime.remove(); // 清理ThreadLocal
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        logger.info("<== 方法出参: {}.{}() with result = {}. Execution time: {} ms", className, methodName, result, executionTime);
    }

    @AfterThrowing(pointcut = "controllerAndServiceLog()", throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable exception) {
        long executionTime = System.currentTimeMillis() - startTime.get();
        startTime.remove(); // 清理ThreadLocal
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        logger.error("<== 异常 in {}.{}() with cause = '{}' and exception = '{}'. Execution time: {} ms",
                className, methodName, exception.getCause() != null ? exception.getCause() : "NULL",
                exception.getMessage(), executionTime);
    }
}
