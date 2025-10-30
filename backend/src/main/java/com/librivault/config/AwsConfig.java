//data

package com.librivault.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AwsConfig {

    @Value("${aws.access-key:}")
    private String accessKey;

    @Value("${aws.secret-key:}")
    private String secretKey;

    @Value("${aws.s3.region:us-east-1}")
    private String region;

    @Bean
    @ConditionalOnProperty(name = "aws.s3.enabled", havingValue = "true", matchIfMissing = false)
    public S3Client s3Client() {
        // Check if AWS credentials are provided
        if (accessKey == null || accessKey.trim().isEmpty() || 
            secretKey == null || secretKey.trim().isEmpty()) {
            
            System.out.println("⚠️ AWS credentials not provided. S3 functionality will be disabled.");
            return null;
        }
        
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }
}