package com.librivault.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = {"classpath:env.properties", "file:env.properties"}, ignoreResourceNotFound = true)
public class EnvironmentConfig {
    // This class loads environment variables from env.properties file
}