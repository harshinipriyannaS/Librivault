package com.librivault.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.librivault.service.AnalyticsService;

@RestController
@RequestMapping("/analytics")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AnalyticsController {
    
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsController.class);
    
    @Autowired
    private AnalyticsService analyticsService;
    
    // Dashboard Overview
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardOverview() {
        try {
            logger.info("Fetching dashboard overview analytics");
            Map<String, Object> overview = analyticsService.getDashboardOverview();
            return ResponseEntity.ok(overview);
            
        } catch (Exception e) {
            logger.error("Failed to fetch dashboard overview", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to fetch dashboard overview", e.getMessage()));
        }
    }
    
    // User Analytics
    @GetMapping("/users")
    public ResponseEntity<?> getUserAnalytics(@RequestParam(defaultValue = "30") int days) {
        try {
            logger.info("Fetching user analytics for {} days", days);
            Map<String, Object> analytics = analyticsService.getUserAnalytics(days);
            return ResponseEntity.ok(analytics);
            
        } catch (Exception e) {
            logger.error("Failed to fetch user analytics", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to fetch user analytics", e.getMessage()));
        }
    }
    
    // Subscription Analytics
    @GetMapping("/subscriptions")
    public ResponseEntity<?> getSubscriptionAnalytics(@RequestParam(defaultValue = "12") int months) {
        try {
            logger.info("Fetching subscription analytics for {} months", months);
            Map<String, Object> analytics = analyticsService.getSubscriptionAnalytics(months);
            return ResponseEntity.ok(analytics);
            
        } catch (Exception e) {
            logger.error("Failed to fetch subscription analytics", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to fetch subscription analytics", e.getMessage()));
        }
    }
    
    // Book Analytics
    @GetMapping("/books")
    public ResponseEntity<?> getBookAnalytics(@RequestParam(defaultValue = "30") int days) {
        try {
            logger.info("Fetching book analytics for {} days", days);
            Map<String, Object> analytics = analyticsService.getBookAnalytics(days);
            return ResponseEntity.ok(analytics);
            
        } catch (Exception e) {
            logger.error("Failed to fetch book analytics", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to fetch book analytics", e.getMessage()));
        }
    }
    
    // Financial Analytics
    @GetMapping("/financial")
    public ResponseEntity<?> getFinancialAnalytics(@RequestParam(defaultValue = "12") int months) {
        try {
            logger.info("Fetching financial analytics for {} months", months);
            Map<String, Object> analytics = analyticsService.getFinancialAnalytics(months);
            return ResponseEntity.ok(analytics);
            
        } catch (Exception e) {
            logger.error("Failed to fetch financial analytics", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to fetch financial analytics", e.getMessage()));
        }
    }
    
    // Fine Analytics
    @GetMapping("/fines")
    public ResponseEntity<?> getFineAnalytics(@RequestParam(defaultValue = "12") int months) {
        try {
            logger.info("Fetching fine analytics for {} months", months);
            Map<String, Object> analytics = analyticsService.getFineAnalytics(months);
            return ResponseEntity.ok(analytics);
            
        } catch (Exception e) {
            logger.error("Failed to fetch fine analytics", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to fetch fine analytics", e.getMessage()));
        }
    }
    
    // Category Analytics
    @GetMapping("/categories")
    public ResponseEntity<?> getCategoryAnalytics() {
        try {
            logger.info("Fetching category analytics");
            Map<String, Object> analytics = analyticsService.getCategoryAnalytics();
            return ResponseEntity.ok(analytics);
            
        } catch (Exception e) {
            logger.error("Failed to fetch category analytics", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to fetch category analytics", e.getMessage()));
        }
    }
    
    // Activity Analytics
    @GetMapping("/activity")
    public ResponseEntity<?> getActivityAnalytics(@RequestParam(defaultValue = "30") int days) {
        try {
            logger.info("Fetching activity analytics for {} days", days);
            Map<String, Object> analytics = analyticsService.getActivityAnalytics(days);
            return ResponseEntity.ok(analytics);
            
        } catch (Exception e) {
            logger.error("Failed to fetch activity analytics", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to fetch activity analytics", e.getMessage()));
        }
    }
    
    // Comprehensive Report
    @GetMapping("/comprehensive")
    public ResponseEntity<?> getComprehensiveAnalytics() {
        try {
            logger.info("Fetching comprehensive analytics report");
            Map<String, Object> report = analyticsService.getComprehensiveAnalytics();
            return ResponseEntity.ok(report);
            
        } catch (Exception e) {
            logger.error("Failed to fetch comprehensive analytics", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to fetch comprehensive analytics", e.getMessage()));
        }
    }
    
    // Helper method to create error response
    private Map<String, String> createErrorResponse(String error, String message) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", error);
        errorResponse.put("message", message);
        return errorResponse;
    }
}