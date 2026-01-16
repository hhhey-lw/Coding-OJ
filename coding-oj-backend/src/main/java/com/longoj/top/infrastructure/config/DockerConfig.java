package com.longoj.top.infrastructure.config;

import cn.hutool.json.JSONUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Version;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;


@Configuration
@ConditionalOnProperty(name = "codesandbox.type", havingValue = "docker")
public class DockerConfig {

    @Value("${docker-java.host}")
    private String DOCKER_HOST;

    @Value("${docker-java.cert-path:}")
    private String DOCKER_CERT_PATH;

    @Bean
    public DockerClient dockerClient() throws IOException {
        DefaultDockerClientConfig.Builder configBuilder = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(DOCKER_HOST);

        // 仅在启用TLS时配置证书
        if (DOCKER_CERT_PATH != null && !DOCKER_CERT_PATH.isEmpty()) {
            String certPath = resolveCertPath(DOCKER_CERT_PATH);
            configBuilder.withDockerTlsVerify(true)
                    .withDockerCertPath(certPath);
        }
        // 注释是配置私有仓库的连接信息
        // configBuilder.withRegistryUrl();
        // configBuilder.withRegistryUsername();
        // configBuilder.withRegistryPassword();
        DockerClientConfig config = configBuilder.build();


        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .maxConnections(5)
                .connectionTimeout(Duration.ofSeconds(10))
                .responseTimeout(Duration.ofSeconds(60))
                .build();

        DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);

        Version version = dockerClient.versionCmd().exec();
        String infoStr = JSONUtil.toJsonStr(version);
        System.out.println("Docker Version信息");
        System.out.println(infoStr);

        return dockerClient;
    }

    /**
     * 解析证书路径，支持 classpath: 前缀
     * 如果是 classpath 资源，会将证书文件复制到临时目录
     */
    private String resolveCertPath(String certPath) throws IOException {
        if (certPath.startsWith("classpath:")) {
            String resourcePath = certPath.substring("classpath:".length());
            // 创建临时目录存放证书
            Path tempDir = Files.createTempDirectory("docker-certs");
            tempDir.toFile().deleteOnExit();

            // 复制证书文件到临时目录
            String[] certFiles = {"ca.pem", "cert.pem", "key.pem"};
            for (String certFile : certFiles) {
                Resource resource = new ClassPathResource(resourcePath + "/" + certFile);
                if (resource.exists()) {
                    try (InputStream is = resource.getInputStream()) {
                        File targetFile = new File(tempDir.toFile(), certFile);
                        FileCopyUtils.copy(is, Files.newOutputStream(targetFile.toPath()));
                        targetFile.deleteOnExit();
                    }
                }
            }
            return tempDir.toString();
        }
        return certPath;
    }
}
