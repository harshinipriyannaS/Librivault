package com.librivault.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.librivault.entity.Fine;
import com.librivault.entity.Payment;
import com.librivault.entity.Subscription;
import com.librivault.entity.User;
import com.librivault.entity.enums.PaymentStatus;
import com.librivault.entity.enums.PaymentType;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    // Basic queries
    List<Payment> findByUser(User user);
    
    Page<Payment> findByUser(User user, Pageable pageable);
    
    List<Payment> findByUserAndStatus(User user, PaymentStatus status);
    
    Page<Payment> findByUserAndStatus(User user, PaymentStatus status, Pageable pageable);
    
    List<Payment> findByStatus(PaymentStatus status);
    
    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);
    
    List<Payment> findByType(PaymentType type);
    
    Page<Payment> findByType(PaymentType type, Pageable pageable);
    
    List<Payment> findByTypeAndStatus(PaymentType type, PaymentStatus status);
    
    Page<Payment> findByTypeAndStatus(PaymentType type, PaymentStatus status, Pageable pageable);
    
    // Stripe-related queries
    Optional<Payment> findByStripePaymentId(String stripePaymentId);
    
    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);
    
    boolean existsByStripePaymentId(String stripePaymentId);
    
    boolean existsByStripePaymentIntentId(String stripePaymentIntentId);
    
    // Subscription-related queries
    List<Payment> findBySubscription(Subscription subscription);
    
    List<Payment> findBySubscriptionAndStatus(Subscription subscription, PaymentStatus status);
    
    @Query("SELECT p FROM Payment p WHERE p.subscription.id = :subscriptionId")
    List<Payment> findBySubscriptionId(@Param("subscriptionId") Long subscriptionId);
    
    @Query("SELECT p FROM Payment p WHERE p.subscription.id = :subscriptionId AND p.status = :status")
    List<Payment> findBySubscriptionIdAndStatus(@Param("subscriptionId") Long subscriptionId, 
                                               @Param("status") PaymentStatus status);
    
    // Fine-related queries
    List<Payment> findByFine(Fine fine);
    
    List<Payment> findByFineAndStatus(Fine fine, PaymentStatus status);
    
    @Query("SELECT p FROM Payment p WHERE p.fine.id = :fineId")
    List<Payment> findByFineId(@Param("fineId") Long fineId);
    
    @Query("SELECT p FROM Payment p WHERE p.fine.id = :fineId AND p.status = :status")
    List<Payment> findByFineIdAndStatus(@Param("fineId") Long fineId, 
                                       @Param("status") PaymentStatus status);
    
    // Date-based queries
    List<Payment> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<Payment> findByCompletedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT p FROM Payment p WHERE p.createdAt >= :since ORDER BY p.createdAt DESC")
    List<Payment> findRecentPayments(@Param("since") LocalDateTime since);
    
    @Query("SELECT p FROM Payment p WHERE p.completedAt >= :since AND p.status = 'COMPLETED' ORDER BY p.completedAt DESC")
    List<Payment> findRecentCompletedPayments(@Param("since") LocalDateTime since);
    
    // Amount-based queries
    List<Payment> findByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);
    
    List<Payment> findByAmountGreaterThanEqual(BigDecimal amount);
    
    List<Payment> findByAmountLessThanEqual(BigDecimal amount);
    
    // Statistics queries
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status")
    long countByStatus(@Param("status") PaymentStatus status);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.type = :type AND p.status = 'COMPLETED'")
    long countCompletedPaymentsByType(@Param("type") PaymentType type);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.createdAt >= :since")
    long countPaymentsSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.completedAt >= :since AND p.status = 'COMPLETED'")
    long countCompletedPaymentsSince(@Param("since") LocalDateTime since);
    
    // Revenue queries
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED'")
    BigDecimal getTotalRevenue();
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED' AND p.type = :type")
    BigDecimal getTotalRevenueByType(@Param("type") PaymentType type);
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED' AND p.completedAt >= :since")
    BigDecimal getTotalRevenueSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED' AND p.type = :type AND p.completedAt >= :since")
    BigDecimal getTotalRevenueByTypeSince(@Param("type") PaymentType type, @Param("since") LocalDateTime since);
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED' AND p.completedAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalRevenueBetween(@Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate);
    
    // Monthly revenue statistics
    @Query("SELECT YEAR(p.completedAt), MONTH(p.completedAt), SUM(p.amount) FROM Payment p " +
           "WHERE p.status = 'COMPLETED' AND p.completedAt >= :since " +
           "GROUP BY YEAR(p.completedAt), MONTH(p.completedAt) " +
           "ORDER BY YEAR(p.completedAt), MONTH(p.completedAt)")
    List<Object[]> getMonthlyRevenue(@Param("since") LocalDateTime since);
    
    @Query("SELECT YEAR(p.completedAt), MONTH(p.completedAt), SUM(p.amount) FROM Payment p " +
           "WHERE p.status = 'COMPLETED' AND p.type = :type AND p.completedAt >= :since " +
           "GROUP BY YEAR(p.completedAt), MONTH(p.completedAt) " +
           "ORDER BY YEAR(p.completedAt), MONTH(p.completedAt)")
    List<Object[]> getMonthlyRevenueByType(@Param("type") PaymentType type, @Param("since") LocalDateTime since);
    
    // User payment history
    @Query("SELECT p FROM Payment p WHERE p.user.id = :userId ORDER BY p.createdAt DESC")
    Page<Payment> findUserPaymentHistory(@Param("userId") Long userId, Pageable pageable);
    
    @Query("SELECT p FROM Payment p WHERE p.user.id = :userId AND p.status = 'COMPLETED' ORDER BY p.completedAt DESC")
    Page<Payment> findUserCompletedPayments(@Param("userId") Long userId, Pageable pageable);
    
    // Failed payments
    @Query("SELECT p FROM Payment p WHERE p.status = 'FAILED' ORDER BY p.createdAt DESC")
    List<Payment> findFailedPayments();
    
    @Query("SELECT p FROM Payment p WHERE p.status = 'FAILED' AND p.createdAt >= :since ORDER BY p.createdAt DESC")
    List<Payment> findRecentFailedPayments(@Param("since") LocalDateTime since);
    
    // Pending payments
    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING' ORDER BY p.createdAt ASC")
    List<Payment> findPendingPayments();
    
    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING' AND p.createdAt < :threshold ORDER BY p.createdAt ASC")
    List<Payment> findStalePendingPayments(@Param("threshold") LocalDateTime threshold);
    
    // Search queries
    @Query("SELECT p FROM Payment p WHERE " +
           "(LOWER(p.user.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.user.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.user.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.stripePaymentId) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND p.status = :status")
    Page<Payment> searchPaymentsByStatus(@Param("search") String search, 
                                        @Param("status") PaymentStatus status, 
                                        Pageable pageable);
    
    // Top paying users
    @Query("SELECT p.user, SUM(p.amount) as totalPaid FROM Payment p " +
           "WHERE p.status = 'COMPLETED' " +
           "GROUP BY p.user " +
           "ORDER BY totalPaid DESC")
    List<Object[]> findTopPayingUsers(Pageable pageable);
    
    // Average payment amount
    @Query("SELECT AVG(p.amount) FROM Payment p WHERE p.status = 'COMPLETED' AND p.type = :type")
    Double getAveragePaymentAmountByType(@Param("type") PaymentType type);
    
    // Payment success rate
    @Query("SELECT " +
           "(SELECT COUNT(p1) FROM Payment p1 WHERE p1.status = 'COMPLETED' AND p1.createdAt >= :since) * 100.0 / " +
           "(SELECT COUNT(p2) FROM Payment p2 WHERE p2.createdAt >= :since) " +
           "FROM Payment p WHERE p.createdAt >= :since")
    Double getPaymentSuccessRateSince(@Param("since") LocalDateTime since);
}