package com.librivault.controller;

import java.math.BigDecimal;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.librivault.dto.payment.CreatePaymentIntentRequest;
import com.librivault.dto.payment.PaymentIntentResponse;
import com.librivault.dto.payment.PaymentResponse;
import com.librivault.entity.enums.PaymentStatus;
import com.librivault.entity.enums.PaymentType;
import com.librivault.security.CurrentUser;
import com.librivault.security.UserPrincipal;
import com.librivault.service.PaymentService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/payments")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PaymentController {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    
    @Autowired
    private PaymentService paymentService;
    
    // Stripe configuration endpoint
    
    @GetMapping("/config")
    public ResponseEntity<?> getStripeConfig() {
        try {
            Map<String, String> config = new HashMap<>();
            config.put("publishableKey", paymentService.getStripePublishableKey());
            return ResponseEntity.ok(config);
            
        } catch (Exception e) {
            logger.error("Failed to get Stripe configuration", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to get payment configuration", e.getMessage()));
        }
    }
    
    // Payment Intent endpoints
    
    @PostMapping("/create-intent")
    public ResponseEntity<?> createPaymentIntent(@Valid @RequestBody CreatePaymentIntentRequest request,
                                                @CurrentUser UserPrincipal currentUser) {
        try {
            logger.info("Creating payment intent for user: {} with amount: {}", currentUser.getId(), request.getAmount());
            PaymentIntentResponse response = paymentService.createPaymentIntent(currentUser.getId(), request);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to create payment intent for user: {}", currentUser.getId(), e);
            return ResponseEntity.badRequest().body(createErrorResponse("Payment intent creation failed", e.getMessage()));
        }
    }
    
    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPayment(@Valid @RequestBody ConfirmPaymentRequest request) {
        try {
            logger.info("Confirming payment for intent: {}", request.paymentIntentId);
            PaymentResponse payment = paymentService.confirmPayment(request.paymentIntentId);
            return ResponseEntity.ok(payment);
            
        } catch (Exception e) {
            logger.error("Failed to confirm payment for intent: {}", request.paymentIntentId, e);
            return ResponseEntity.badRequest().body(createErrorResponse("Payment confirmation failed", e.getMessage()));
        }
    }
    
    // User payment endpoints
    
    @GetMapping("/my-payments")
    public ResponseEntity<?> getMyPaymentHistory(@CurrentUser UserPrincipal currentUser, Pageable pageable) {
        try {
            logger.info("Fetching payment history for user: {}", currentUser.getId());
            Page<PaymentResponse> payments = paymentService.getUserPaymentHistory(currentUser.getId(), pageable);
            return ResponseEntity.ok(payments);
            
        } catch (Exception e) {
            logger.error("Failed to fetch payment history for user: {}", currentUser.getId(), e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to fetch payment history", e.getMessage()));
        }
    }
    
    @GetMapping("/my-payments/completed")
    public ResponseEntity<?> getMyCompletedPayments(@CurrentUser UserPrincipal currentUser, Pageable pageable) {
        try {
            logger.info("Fetching completed payments for user: {}", currentUser.getId());
            Page<PaymentResponse> payments = paymentService.getUserCompletedPayments(currentUser.getId(), pageable);
            return ResponseEntity.ok(payments);
            
        } catch (Exception e) {
            logger.error("Failed to fetch completed payments for user: {}", currentUser.getId(), e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to fetch completed payments", e.getMessage()));
        }
    }
    
    @GetMapping("/{id}/receipt")
    public ResponseEntity<?> getReceiptUrl(@PathVariable Long id, @CurrentUser UserPrincipal currentUser) {
        try {
            logger.info("Getting receipt URL for payment: {} and user: {}", id, currentUser.getId());
            String receiptUrl = paymentService.getReceiptUrl(id, currentUser.getId());
            
            Map<String, String> response = new HashMap<>();
            response.put("receiptUrl", receiptUrl);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get receipt URL for payment: {} and user: {}", id, currentUser.getId(), e);
            return ResponseEntity.badRequest().body(createErrorResponse("Receipt not available", e.getMessage()));
        }
    }
    
    // Admin endpoints
    
    @GetMapping("/all")
    public ResponseEntity<?> getAllPayments(Pageable pageable) {
        try {
            logger.info("Fetching all payments with pagination");
            Page<PaymentResponse> payments = paymentService.getAllPayments(pageable);
            return ResponseEntity.ok(payments);
            
        } catch (Exception e) {
            logger.error("Failed to fetch all payments", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to fetch payments", e.getMessage()));
        }
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getPaymentsByStatus(@PathVariable PaymentStatus status, Pageable pageable) {
        try {
            logger.info("Fetching payments by status: {}", status);
            Page<PaymentResponse> payments = paymentService.getPaymentsByStatus(status, pageable);
            return ResponseEntity.ok(payments);
            
        } catch (Exception e) {
            logger.error("Failed to fetch payments by status: {}", status, e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to fetch payments", e.getMessage()));
        }
    }
    
    @GetMapping("/type/{type}")
    public ResponseEntity<?> getPaymentsByType(@PathVariable PaymentType type, Pageable pageable) {
        try {
            logger.info("Fetching payments by type: {}", type);
            Page<PaymentResponse> payments = paymentService.getPaymentsByType(type, pageable);
            return ResponseEntity.ok(payments);
            
        } catch (Exception e) {
            logger.error("Failed to fetch payments by type: {}", type, e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to fetch payments", e.getMessage()));
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<?> searchPayments(@RequestParam String query, 
                                           @RequestParam PaymentStatus status, 
                                           Pageable pageable) {
        try {
            logger.info("Searching payments with query: {} and status: {}", query, status);
            Page<PaymentResponse> payments = paymentService.searchPayments(query, status, pageable);
            return ResponseEntity.ok(payments);
            
        } catch (Exception e) {
            logger.error("Failed to search payments with query: {} and status: {}", query, status, e);
            return ResponseEntity.badRequest().body(createErrorResponse("Search failed", e.getMessage()));
        }
    }
    
    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUserPaymentHistory(@PathVariable Long userId, Pageable pageable) {
        try {
            logger.info("Fetching payment history for user: {}", userId);
            Page<PaymentResponse> payments = paymentService.getUserPaymentHistory(userId, pageable);
            return ResponseEntity.ok(payments);
            
        } catch (Exception e) {
            logger.error("Failed to fetch payment history for user: {}", userId, e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to fetch payment history", e.getMessage()));
        }
    }
    
    @GetMapping("/{id}/receipt/admin")
    public ResponseEntity<?> getReceiptUrlAdmin(@PathVariable Long id) {
        try {
            logger.info("Admin getting receipt URL for payment: {}", id);
            // For admin, we need to get the payment first to get the user ID
            var payment = paymentService.getPaymentEntityById(id);
            String receiptUrl = paymentService.getReceiptUrl(id, payment.getUser().getId());
            
            Map<String, String> response = new HashMap<>();
            response.put("receiptUrl", receiptUrl);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get receipt URL for payment: {}", id, e);
            return ResponseEntity.badRequest().body(createErrorResponse("Receipt not available", e.getMessage()));
        }
    }
    
    @GetMapping("/{id}/receipt/download")
    public ResponseEntity<?> downloadReceiptPdf(@PathVariable Long id) {
        try {
            logger.info("Generating receipt PDF for payment: {}", id);
            byte[] pdfData = paymentService.generateReceiptPdf(id);
            
            // In a real implementation, you would return the PDF as a file download
            Map<String, String> response = new HashMap<>();
            response.put("message", "PDF generation feature coming soon");
            response.put("paymentId", id.toString());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to generate receipt PDF for payment: {}", id, e);
            return ResponseEntity.badRequest().body(createErrorResponse("PDF generation failed", e.getMessage()));
        }
    }
    
    // Statistics endpoints
    
    @GetMapping("/stats/total-revenue")
    public ResponseEntity<?> getTotalRevenue() {
        try {
            BigDecimal totalRevenue = paymentService.getTotalRevenue();
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get total revenue", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to get statistics", e.getMessage()));
        }
    }
    
    @GetMapping("/stats/revenue/type/{type}")
    public ResponseEntity<?> getTotalRevenueByType(@PathVariable PaymentType type) {
        try {
            BigDecimal revenue = paymentService.getTotalRevenueByType(type);
            
            Map<String, Object> response = new HashMap<>();
            response.put("paymentType", type);
            response.put("totalRevenue", revenue != null ? revenue : BigDecimal.ZERO);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get total revenue by type: {}", type, e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to get statistics", e.getMessage()));
        }
    }
    
    @GetMapping("/stats/revenue/recent")
    public ResponseEntity<?> getTotalRevenueSince(@RequestParam(defaultValue = "30") int days) {
        try {
            BigDecimal revenue = paymentService.getTotalRevenueSince(days);
            
            Map<String, Object> response = new HashMap<>();
            response.put("days", days);
            response.put("totalRevenue", revenue != null ? revenue : BigDecimal.ZERO);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get recent revenue", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to get statistics", e.getMessage()));
        }
    }
    
    @GetMapping("/stats/monthly-revenue")
    public ResponseEntity<?> getMonthlyRevenue(@RequestParam(defaultValue = "12") int months) {
        try {
            List<Object[]> monthlyRevenue = paymentService.getMonthlyRevenue(months);
            
            Map<String, Object> response = new HashMap<>();
            response.put("months", months);
            response.put("monthlyRevenue", monthlyRevenue);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get monthly revenue", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to get statistics", e.getMessage()));
        }
    }
    
    @GetMapping("/stats/monthly-revenue/type/{type}")
    public ResponseEntity<?> getMonthlyRevenueByType(@PathVariable PaymentType type, 
                                                    @RequestParam(defaultValue = "12") int months) {
        try {
            List<Object[]> monthlyRevenue = paymentService.getMonthlyRevenueByType(type, months);
            
            Map<String, Object> response = new HashMap<>();
            response.put("paymentType", type);
            response.put("months", months);
            response.put("monthlyRevenue", monthlyRevenue);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get monthly revenue by type: {}", type, e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to get statistics", e.getMessage()));
        }
    }
    
    @GetMapping("/stats/count/status/{status}")
    public ResponseEntity<?> getTotalPaymentsByStatus(@PathVariable PaymentStatus status) {
        try {
            long count = paymentService.getTotalPaymentsByStatus(status);
            
            Map<String, Object> response = new HashMap<>();
            response.put("paymentStatus", status);
            response.put("count", count);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get payments count by status: {}", status, e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to get statistics", e.getMessage()));
        }
    }
    
    @GetMapping("/stats/recent")
    public ResponseEntity<?> getRecentPayments(@RequestParam(defaultValue = "30") int days) {
        try {
            List<PaymentResponse> recentPayments = paymentService.getRecentPayments(days);
            
            Map<String, Object> response = new HashMap<>();
            response.put("days", days);
            response.put("payments", recentPayments);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get recent payments", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to get statistics", e.getMessage()));
        }
    }
    
    @GetMapping("/stats/failed")
    public ResponseEntity<?> getFailedPayments() {
        try {
            List<PaymentResponse> failedPayments = paymentService.getFailedPayments();
            
            Map<String, Object> response = new HashMap<>();
            response.put("failedPayments", failedPayments);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get failed payments", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to get statistics", e.getMessage()));
        }
    }
    
    @GetMapping("/stats/top-paying-users")
    public ResponseEntity<?> getTopPayingUsers(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<Object[]> topUsers = paymentService.getTopPayingUsers(limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("limit", limit);
            response.put("topPayingUsers", topUsers);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get top paying users", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to get statistics", e.getMessage()));
        }
    }
    
    @GetMapping("/stats/success-rate")
    public ResponseEntity<?> getPaymentSuccessRate(@RequestParam(defaultValue = "30") int days) {
        try {
            Double successRate = paymentService.getPaymentSuccessRate(days);
            
            Map<String, Object> response = new HashMap<>();
            response.put("days", days);
            response.put("successRate", successRate != null ? successRate : 0.0);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get payment success rate", e);
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
    
    // Request DTOs
    public static class ConfirmPaymentRequest {
        @NotBlank(message = "Payment intent ID is required")
        public String paymentIntentId;
    }
}