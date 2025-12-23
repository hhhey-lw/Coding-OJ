package com.longoj.top.infrastructure.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云对象存储客户端配置
 */
@Configuration
@ConfigurationProperties(prefix = "aliyun")
@Data
public class OssClientConfig {

    /**
     * accessKeyId
     */
    private String accessKeyId;

    /**
     * accessKeySecret
     */
    private String accessKeySecret;

    /**
     * OSS 配置
     */
    private OssConfig oss;

    @Data
    public static class OssConfig {
        /**
         * 端点
         */
        private String endpoint;

        /**
         * 桶名
         */
        private String bucketName;
    }

    @Bean
    public OSS ossClient() {
        return new OSSClientBuilder().build(oss.getEndpoint(), accessKeyId, accessKeySecret);
    }

    /**
     * 获取 OSS 访问地址
     */
    public String getOssHost() {
        String endpoint = oss.getEndpoint().replace("https://", "").replace("http://", "");
        return "https://" + oss.getBucketName() + "." + endpoint;
    }
}

