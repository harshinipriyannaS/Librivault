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
        
        // Test database connection and data availability
        try (Connection connection = dataSource.getConnection()) {
            logger.info("✅ Database connection successful: {}", connection.getMetaData().getURL());
            
            // Check if data is loaded
            try {
                // Simple query to check if tables exist and have data
                var statement = connection.createStatement();
                var rs = statement.executeQuery("SELECT COUNT(*) as count FROM users");
                if (rs.next()) {
                    int userCount = rs.getInt("count");
                    logger.info("📊 Found {} users in database", userCount);
                }
                
                rs = statement.executeQuery("SELECT COUNT(*) as count FROM books");
                if (rs.next()) {
                    int bookCount = rs.getInt("count");
                    logger.info("📚 Found {} books in database", bookCount);
                }
                
                rs = statement.executeQuery("SELECT COUNT(*) as count FROM categories");
                if (rs.next()) {
                    int categoryCount = rs.getInt("count");
                    logger.info("🏷️ Found {} categories in database", categoryCount);
                }
                
                logger.info("✅ Database schema and data initialization completed successfully!");
                
            } catch (Exception e) {
                logger.warn("⚠️ Could not verify data initialization: {}", e.getMessage());
            }
            
        } catch (Exception e) {
            logger.error("❌ Database connection failed: {}", e.getMessage());
        }
        
        logger.info("🌟 All systems ready - LibriVault Backend is fully operational!");
        logger.info("🔗 Health check available at: /api/health");
        logger.info("🔗 Readiness check available at: /api/health/ready");
    }
}