package com.librivault.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.librivault.dto.subscription.SubscriptionResponse;
import com.librivault.entity.enums.SubscriptionType;
import com.librivault.security.CurrentUser;
import com.librivault.security.UserPrincipal;
import com.librivault.service.SubscriptionService;

@RestController
@RequestMapping("/subscriptions")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SubscriptionController {
    
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionController.class);
    
    @Autowired
    private SubscriptionService subscriptionService;
    
    // Public endpoints
    
    @GetMapping("/plans")
    public ResponseEntity<?> getAvailableSubscriptionPlans() {
        try {
            logger.info("Fetching available subscription plans");
            List<SubscriptionService.SubscriptionPlan> plans = subscriptionService.getAvailableSubscriptionPlans();
            return ResponseEntity.ok(plans);
            
        } catch (Exception e) {
            logger.error("Failed to fetch subscription plans", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to fetch subscription plans", e.getMessage()));
        }
    }
    
    // User subscription endpoints
    
    @GetMapping("/my-subscription")
    public ResponseEntity<?> getMySubscription(@CurrentUser UserPrincipal currentUser) {
        try {
            logger.info("Fetching subscription for user: {}", currentUser.getId());
            SubscriptionResponse subscription = subscriptionService.getUserSubscription(currentUser.getId());
            return ResponseEntity.ok(subscription);
            
        } catch (Exception e) {
            logger.error("Failed to fetch subscription for user: {}", currentUser.getId(), e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to fetch subscription", e.getMessage()));
        }
    }
    
    @PostMapping("/upgrade")
    public ResponseEntity<?> upgradeToPremium(@CurrentUser UserPrincipal currentUser) {
        try {
            logger.info("Upgrading user {} to premium subscription", currentUser.getId());
            SubscriptionResponse subscription = subscriptionService.upgradeToPremium(currentUser.getId());
            return ResponseEntity.ok(subscription);
            
        } catch (Exception e) {
            logger.error("Failed to upgrade user {} to premium", currentUser.getId(), e);
            return ResponseEntity.badRequest().body(createErrorResponse("Subscription upgrade failed", e.getMessage()));
        }
    }
    
    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUserSubscription(@PathVariable Long userId) {
        try {
            logger.info("Fetching subscription for user: {}", userId);
            SubscriptionResponse subscription = subscriptionService.getUserSubscription(userId);
            return ResponseEntity.ok(subscription);
            
        } catch (Exception e) {
            logger.error("Failed to fetch subscription for user: {}", userId, e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to fetch subscription", e.getMessage()));
        }
    }
    
    // Admin endpoints
    
    @GetMapping("/all")
    public ResponseEntity<?> getAllSubscriptions(Pageable pageable) {
        try {
            logger.info("Fetching all subscriptions with pagination");
            Page<SubscriptionResponse> subscriptions = subscriptionService.getAllSubscriptions(pageable);
            return ResponseEntity.ok(subscriptions);
            
        } catch (Exception e) {
            logger.error("Failed to fetch all subscriptions", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to fetch subscriptions", e.getMessage()));
        }
    }
    
    @GetMapping("/type/{type}")
    public ResponseEntity<?> getSubscriptionsByType(@PathVariable SubscriptionType type, Pageable pageable) {
        try {
            logger.info("Fetching subscriptions by type: {}", type);
            Page<SubscriptionResponse> subscriptions = subscriptionService.getSubscriptionsByType(type, pageable);
            return ResponseEntity.ok(subscriptions);
            
        } catch (Exception e) {
            logger.error("Failed to fetch subscriptions by type: {}", type, e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to fetch subscriptions", e.getMessage()));
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<?> searchSubscriptions(@RequestParam String query, Pageable pageable) {
        try {
            logger.info("Searching subscriptions with query: {}", query);
            Page<SubscriptionResponse> subscriptions = subscriptionService.searchSubscriptions(query, pageable);
            return ResponseEntity.ok(subscriptions);
            
        } catch (Exception e) {
            logger.error("Failed to search subscriptions with query: {}", query, e);
            return ResponseEntity.badRequest().body(createErrorResponse("Search failed", e.getMessage()));
        }
    }
    
    @PostMapping("/{id}/renew")
    public ResponseEntity<?> renewSubscription(@PathVariable Long id) {
        try {
            logger.info("Renewing subscription: {}", id);
            subscriptionService.renewSubscription(id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Subscription renewed successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to renew subscription: {}", id, e);
            return ResponseEntity.badRequest().body(createErrorResponse("Subscription renewal failed", e.getMessage()));
        }
    }
    
    // Statistics endpoints
    
    @GetMapping("/stats/total")
    public ResponseEntity<?> getTotalActiveSubscriptions() {
        try {
            long totalSubscriptions = subscriptionService.getTotalActiveSubscriptions();
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalActiveSubscriptions", totalSubscriptions);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get total active subscriptions", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to get statistics", e.getMessage()));
        }
    }
    
    @GetMapping("/stats/type/{type}")
    public ResponseEntity<?> getActiveSubscriptionsByType(@PathVariable SubscriptionType type) {
        try {
            long subscriptionCount = subscriptionService.getActiveSubscriptionsByType(type);
            
            Map<String, Object> response = new HashMap<>();
            response.put("subscriptionType", type);
            response.put("count", subscriptionCount);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get subscriptions count by type: {}", type, e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to get statistics", e.getMessage()));
        }
    }
    
    @GetMapping("/stats/recent")
    public ResponseEntity<?> getRecentSubscriptions(@RequestParam(defaultValue = "30") int days) {
        try {
            List<SubscriptionResponse> recentSubscriptions = subscriptionService.getRecentSubscriptions(days);
            
            Map<String, Object> response = new HashMap<>();
            response.put("days", days);
            response.put("subscriptions", recentSubscriptions);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get recent subscriptions", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to get statistics", e.getMessage()));
        }
    }
    
    @GetMapping("/stats/revenue")
    public ResponseEntity<?> getTotalRevenueFromPremiumSubscriptions(@RequestParam(defaultValue = "30") int days) {
        try {
            Double revenue = subscriptionService.getTotalRevenueFromPremiumSubscriptions(days);
            
            Map<String, Object> response = new HashMap<>();
            response.put("days", days);
            response.put("totalRevenue", revenue != null ? revenue : 0.0);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get revenue from premium subscriptions", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to get statistics", e.getMessage()));
        }
    }
    
    @GetMapping("/stats/monthly-premium")
    public ResponseEntity<?> getMonthlyPremiumSubscriptions(@RequestParam(defaultValue = "12") int months) {
        try {
            List<Object[]> monthlyStats = subscriptionService.getMonthlyPremiumSubscriptions(months);
            
            Map<String, Object> response = new HashMap<>();
            response.put("months", months);
            response.put("monthlyPremiumSubscriptions", monthlyStats);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get monthly premium subscriptions", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to get statistics", e.getMessage()));
        }
    }
    
    @GetMapping("/stats/monthly-revenue")
    public ResponseEntity<?> getMonthlyRevenueFromPremiumSubscriptions(@RequestParam(defaultValue = "12") int months) {
        try {
            List<Object[]> monthlyRevenue = subscriptionService.getMonthlyRevenueFromPremiumSubscriptions(months);
            
            Map<String, Object> response = new HashMap<>();
            response.put("months", months);
            response.put("monthlyRevenue", monthlyRevenue);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get monthly revenue from premium subscriptions", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to get statistics", e.getMessage()));
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