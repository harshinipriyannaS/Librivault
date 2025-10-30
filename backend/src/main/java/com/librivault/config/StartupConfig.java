package com.librivault.config;

import java.sql.Connection;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class StartupConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(StartupConfig.class);
    
    @Autowired
    private DataSource dataSource;
    
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        logger.info("🚀 LibriVault Backend Application is ready!");
        
        // Test database connection
        try (Connection connection = dataSource.getConnection()) {
            logger.info("✅ Database connection successful: {}", connection.getMetaData().getURL());
        } catch (Exception e) {
            logger.error("❌ Database connection failed: {}", e.getMessage());
        }
        
        logger.info("🌟 All systems ready - LibriVault Backend is fully operational!");
    }
}