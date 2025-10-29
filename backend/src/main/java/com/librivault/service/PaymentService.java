package com.librivault.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.librivault.dto.payment.CreatePaymentIntentRequest;
import com.librivault.dto.payment.PaymentIntentResponse;
import com.librivault.dto.payment.PaymentResponse;
import com.librivault.entity.Fine;
import com.librivault.entity.Payment;
import com.librivault.entity.Subscription;
import com.librivault.entity.User;
import com.librivault.entity.enums.PaymentStatus;
import com.librivault.entity.enums.PaymentType;
import com.librivault.repository.FineRepository;
import com.librivault.repository.PaymentRepository;
import com.librivault.repository.SubscriptionRepository;
import com.librivault.repository.UserRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

@Service
public class PaymentService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    
    @Autowired
    private FineRepository fineRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Value("${stripe.publishable-key}")
    private String stripePublishableKey;
    

    
    // Payment Intent creation
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    @Transactional
    public PaymentIntentResponse createPaymentIntent(Long userId, CreatePaymentIntentRequest request) {
        logger.info("Creating payment intent for user: {} with amount: {}", userId, request.getAmount());
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        // Create payment record
        Payment payment = new Payment();
        payment.setUser(user);
        payment.setAmount(request.getAmount());
        payment.setType(request.getType());
        payment.setDescription(request.getDescription());
        payment.setStatus(PaymentStatus.PENDING);
        
        // Set related entities based on payment type
        if (request.getType() == PaymentType.SUBSCRIPTION && request.getSubscriptionId() != null) {
            Subscription subscription = subscriptionRepository.findById(request.getSubscriptionId())
                    .orElseThrow(() -> new RuntimeException("Subscription not found"));
            payment.setSubscription(subscription);
        } else if (request.getType() == PaymentType.FINE && request.getFineId() != null) {
            Fine fine = fineRepository.findById(request.getFineId())
                    .orElseThrow(() -> new RuntimeException("Fine not found"));
            payment.setFine(fine);
        }
        
        Payment savedPayment = paymentRepository.save(payment);
        
        try {
            // Create Stripe PaymentIntent
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(request.getAmount().multiply(BigDecimal.valueOf(100)).longValue()) // Convert to cents
                    .setCurrency("inr")
                    .setDescription(request.getDescription())
                    .putMetadata("userId", userId.toString())
                    .putMetadata("paymentId", savedPayment.getId().toString())
                    .putMetadata("paymentType", request.getType().toString())
                    .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                            .setEnabled(true)
                            .build()
                    )
                    .build();
            
            PaymentIntent paymentIntent = PaymentIntent.create(params);
            
            // Update payment with Stripe details
            savedPayment.setStripePaymentIntentId(paymentIntent.getId());
            paymentRepository.save(savedPayment);
            
            logger.info("Payment intent created successfully: {}", paymentIntent.getId());
            
            return new PaymentIntentResponse(
                paymentIntent.getClientSecret(),
                paymentIntent.getId(),
                savedPayment.getId()
            );
            
        } catch (StripeException e) {
            logger.error("Failed to create Stripe payment intent: {}", e.getMessage(), e);
            
            // Mark payment as failed
            savedPayment.setStatus(PaymentStatus.FAILED);
            savedPayment.setFailureReason(e.getMessage());
            paymentRepository.save(savedPayment);
            
            throw new RuntimeException("Failed to create payment intent: " + e.getMessage());
        }
    }
    
    // Payment confirmation
    @Transactional
    public PaymentResponse confirmPayment(String paymentIntentId) {
        logger.info("Confirming payment for intent: {}", paymentIntentId);
        
        Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new RuntimeException("Payment not found for intent: " + paymentIntentId));
        
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            
            switch (paymentIntent.getStatus()) {
                case "succeeded":
                    // Mark payment as completed
                    payment.setStatus(PaymentStatus.COMPLETED);
                    payment.setStripePaymentId(paymentIntent.getId());
                    payment.setCompletedAt(LocalDateTime.now());
                    
                    // Set receipt URL - Note: In real Stripe implementation, you'd get this from charges
                    // For now, we'll generate a placeholder receipt URL
                    payment.setReceiptUrl("https://dashboard.stripe.com/receipts/" + paymentIntent.getId());
                    
                    Payment savedPayment = paymentRepository.save(payment);
                    
                    // Process payment based on type
                    processSuccessfulPayment(savedPayment);
                    
                    // Send confirmation notification
                    notificationService.sendPaymentConfirmation(
                        payment.getUser(),
                        payment.getStripePaymentId(),
                        payment.getAmount().toString()
                    );
                    
                    logger.info("Payment confirmed successfully: {}", paymentIntentId);
                    return convertToPaymentResponse(savedPayment);
                    
                case "payment_failed":
                    payment.setStatus(PaymentStatus.FAILED);
                    payment.setFailureReason("Payment failed");
                    paymentRepository.save(payment);
                    
                    throw new RuntimeException("Payment failed");
                    
                default:
                    throw new RuntimeException("Payment status is: " + paymentIntent.getStatus());
            }
            
        } catch (StripeException e) {
            logger.error("Failed to retrieve payment intent: {}", e.getMessage(), e);
            
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(e.getMessage());
            paymentRepository.save(payment);
            
            throw new RuntimeException("Failed to confirm payment: " + e.getMessage());
        }
    }
    
    @Transactional
    private void processSuccessfulPayment(Payment savedPayment) {
        if (savedPayment.getType() == PaymentType.SUBSCRIPTION && savedPayment.getSubscription() != null) {
            // Process subscription payment - subscription upgrade is handled in SubscriptionService
            logger.info("Processing subscription payment: {}", savedPayment.getId());
            
        } else if (savedPayment.getType() == PaymentType.FINE && savedPayment.getFine() != null) {
            // Mark fine as paid
            Fine fine = savedPayment.getFine();
            fine.markAsPaid();
            fineRepository.save(fine);
            
            logger.info("Fine marked as paid: {}", fine.getId());
        }
    }
    
    // Payment history and management
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    public Page<PaymentResponse> getUserPaymentHistory(Long userId, Pageable pageable) {
        logger.info("Fetching payment history for user: {}", userId);
        return paymentRepository.findUserPaymentHistory(userId, pageable)
                .map(this::convertToPaymentResponse);
    }
    
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    public Page<PaymentResponse> getUserCompletedPayments(Long userId, Pageable pageable) {
        logger.info("Fetching completed payments for user: {}", userId);
        return paymentRepository.findUserCompletedPayments(userId, pageable)
                .map(this::convertToPaymentResponse);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    public Page<PaymentResponse> getAllPayments(Pageable pageable) {
        logger.info("Fetching all payments with pagination");
        return paymentRepository.findAll(pageable)
                .map(this::convertToPaymentResponse);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    public Page<PaymentResponse> getPaymentsByStatus(PaymentStatus status, Pageable pageable) {
        logger.info("Fetching payments by status: {}", status);
        return paymentRepository.findByStatus(status, pageable)
                .map(this::convertToPaymentResponse);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    public Page<PaymentResponse> getPaymentsByType(PaymentType type, Pageable pageable) {
        logger.info("Fetching payments by type: {}", type);
        return paymentRepository.findByType(type, pageable)
                .map(this::convertToPaymentResponse);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    public Page<PaymentResponse> searchPayments(String search, PaymentStatus status, Pageable pageable) {
        logger.info("Searching payments with query: {} and status: {}", search, status);
        return paymentRepository.searchPaymentsByStatus(search, status, pageable)
                .map(this::convertToPaymentResponse);
    }
    
    // Receipt generation and download
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    public String getReceiptUrl(Long paymentId, Long userId) {
        logger.info("Getting receipt URL for payment: {} and user: {}", paymentId, userId);
        
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));
        
        // Check if user owns this payment or is admin
        if (!payment.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied to payment receipt");
        }
        
        if (payment.getReceiptUrl() == null) {
            throw new RuntimeException("Receipt not available for this payment");
        }
        
        return payment.getReceiptUrl();
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    public byte[] generateReceiptPdf(Long paymentId) {
        logger.info("Generating receipt PDF for payment: {}", paymentId);
        
        // Verify payment exists
        paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));
        
        // TODO: Implement PDF generation logic
        // This would typically use a library like iText or Apache PDFBox
        // For now, return empty byte array
        return new byte[0];
    }
    
    // Statistics and reporting
    @PreAuthorize("hasRole('ADMIN')")
    public BigDecimal getTotalRevenue() {
        return paymentRepository.getTotalRevenue();
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    public BigDecimal getTotalRevenueByType(PaymentType type) {
        return paymentRepository.getTotalRevenueByType(type);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    public BigDecimal getTotalRevenueSince(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return paymentRepository.getTotalRevenueSince(since);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    public BigDecimal getTotalRevenueByTypeSince(PaymentType type, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return paymentRepository.getTotalRevenueByTypeSince(type, since);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    public List<Object[]> getMonthlyRevenue(int months) {
        LocalDateTime since = LocalDateTime.now().minusMonths(months);
        return paymentRepository.getMonthlyRevenue(since);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    public List<Object[]> getMonthlyRevenueByType(PaymentType type, int months) {
        LocalDateTime since = LocalDateTime.now().minusMonths(months);
        return paymentRepository.getMonthlyRevenueByType(type, since);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    public long getTotalPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.countByStatus(status);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    public List<PaymentResponse> getRecentPayments(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return paymentRepository.findRecentPayments(since)
                .stream()
                .map(this::convertToPaymentResponse)
                .collect(Collectors.toList());
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    public List<PaymentResponse> getFailedPayments() {
        return paymentRepository.findFailedPayments()
                .stream()
                .map(this::convertToPaymentResponse)
                .collect(Collectors.toList());
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    public List<Object[]> getTopPayingUsers(int limit) {
        return paymentRepository.findTopPayingUsers(
            org.springframework.data.domain.PageRequest.of(0, limit));
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    public Double getPaymentSuccessRate(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return paymentRepository.getPaymentSuccessRateSince(since);
    }
    
    // Stripe configuration
    public String getStripePublishableKey() {
        return stripePublishableKey;
    }
    
    // Helper methods
    private PaymentResponse convertToPaymentResponse(Payment payment) {
        return new PaymentResponse(
            payment.getId(),
            payment.getUser().getId(),
            payment.getUser().getEmail(),
            payment.getUser().getFullName(),
            payment.getStripePaymentId(),
            payment.getStripePaymentIntentId(),
            payment.getAmount(),
            payment.getType(),
            payment.getStatus(),
            payment.getReceiptUrl(),
            payment.getDescription(),
            payment.getFailureReason(),
            payment.getCreatedAt(),
            payment.getCompletedAt()
        );
    }
    
    public Payment getPaymentEntityById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));
    }
}