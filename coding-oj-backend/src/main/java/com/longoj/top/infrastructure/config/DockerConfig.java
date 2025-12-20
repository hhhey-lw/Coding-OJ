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

import java.time.Duration;


@Configuration
@ConditionalOnProperty(name = "codesandbox.type", havingValue = "docker")
public class DockerConfig {

    @Value("${docker-java.host}")
    private String DOCKER_HOST;


    @Bean
    public DockerClient dockerClient() {
        /* tls证书在/resource/certs中，使用绝对路径即可 */
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(DOCKER_HOST)
                .withDockerTlsVerify(true)
                .withDockerCertPath("/www/wwwroot/docker-tls/")
                .build();


        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .maxConnections(5)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(30))
                .build();

        DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);

        Version version = dockerClient.versionCmd().exec();
        String infoStr = JSONUtil.toJsonStr(version);
        System.out.println("Docker Version信息");
        System.out.println(infoStr);

        return dockerClient;
    }
}
