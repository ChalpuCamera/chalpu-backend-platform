package com.example.chalpuplatform.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeAsyncClient;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.time.Duration;

@Configuration
public class AwsConfig {

    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.region.static:ap-northeast-2}")
    private String region;

    @Bean
    public AwsCredentialsProvider awsCredentialsProvider() {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
        );
    }

    // 기존 PhotoService용 동기 클라이언트
    @Bean
    public S3Client s3Client(AwsCredentialsProvider credentialsProvider) {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @Bean
    public S3Presigner s3Presigner(AwsCredentialsProvider credentialsProvider) {
        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .build();
    }

    // 새로운 메뉴 추출 기능용 비동기 클라이언트
    @Bean
    public S3AsyncClient s3AsyncClient(AwsCredentialsProvider credentialsProvider) {
        return S3AsyncClient.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .httpClient(NettyNioAsyncHttpClient.builder()
                        .maxConcurrency(10)  // Micro 인스턴스 최적화
                        .connectionTimeout(Duration.ofSeconds(10))
                        .build())
                .build();
    }

    @Bean
    public BedrockRuntimeAsyncClient bedrockClient(AwsCredentialsProvider credentialsProvider) {
        return BedrockRuntimeAsyncClient.builder()
                .region(Region.US_EAST_1)  // Bedrock은 us-east-1 리전 사용
                .credentialsProvider(credentialsProvider)
                .httpClient(NettyNioAsyncHttpClient.builder()
                        .maxConcurrency(5)  // Micro 인스턴스 최적화
                        .connectionTimeout(Duration.ofSeconds(60))  // Bedrock 응답 시간 고려
                        .build())
                .build();
    }
}