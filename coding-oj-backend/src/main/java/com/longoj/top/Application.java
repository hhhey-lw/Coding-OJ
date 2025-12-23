package com.longoj.top;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 主类（项目启动入口）
 *
 */
@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.longoj.top")
@MapperScan("com.longoj.top.infrastructure.mapper")
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@EnableTransactionManagement
public class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        Environment env = context.getEnvironment();

        String port = env.getProperty("server.port", "8080");
        String contextPath = env.getProperty("server.servlet.context-path", "");

        System.out.println("\n=====================================");
        System.out.println("🚀 项目启动成功！");
        System.out.println("=====================================");
        System.out.println("📖 API 文档地址：");
        System.out.println("   http://localhost:" + port + contextPath + "/doc.html");
        System.out.println("-------------------------------------");
        System.out.println("📥 文档导入地址：");
        System.out.println("   http://localhost:" + port + contextPath + "/v2/api-docs");
        System.out.println("=====================================\n");
    }

}
