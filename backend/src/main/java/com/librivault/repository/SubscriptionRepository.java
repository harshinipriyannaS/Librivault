package com.librivault.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.librivault.entity.Subscription;
import com.librivault.entity.User;
import com.librivault.entity.enums.SubscriptionType;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    
    // Basic queries
    Optional<Subscription> findByUser(User user);
    
    Optional<Subscription> findByUserId(Long userId);
    
    List<Subscription> findByType(SubscriptionType type);
    
    Page<Subscription> findByType(SubscriptionType type, Pageable pageable);
    
    List<Subscription> findByActiveTrue();
    
    Page<Subscription> findByActiveTrue(Pageable pageable);
    
    List<Subscription> findByTypeAndActiveTrue(SubscriptionType type);
    
    Page<Subscription> findByTypeAndActiveTrue(SubscriptionType type, Pageable pageable);
    
    // Expiry-related queries
    @Query("SELECT s FROM Subscription s WHERE s.endDate <= :date AND s.active = true")
    List<Subscription> findExpiredSubscriptions(@Param("date") LocalDateTime date);
    
    @Query("SELECT s FROM Subscription s WHERE s.endDate BETWEEN :startDate AND :endDate AND s.active = true")
    List<Subscription> findSubscriptionsExpiringBetween(@Param("startDate") LocalDateTime startDate, 
                                                       @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT s FROM Subscription s WHERE s.endDate <= :warningDate AND s.endDate > :currentDate AND s.active = true")
    List<Subscription> findSubscriptionsExpiringSoon(@Param("currentDate") LocalDateTime currentDate,
                                                    @Param("warningDate") LocalDateTime warningDate);
    
    // Date-based queries
    List<Subscription> findByStartDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<Subscription> findByEndDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT s FROM Subscription s WHERE s.createdAt >= :since ORDER BY s.createdAt DESC")
    List<Subscription> findRecentSubscriptions(@Param("since") LocalDateTime since);
    
    // Statistics queries
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.type = :type AND s.active = true")
    long countActiveSubscriptionsByType(@Param("type") SubscriptionType type);
    
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.active = true")
    long countActiveSubscriptions();
    
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.endDate <= :date AND s.active = true")
    long countExpiredSubscriptions(@Param("date") LocalDateTime date);
    
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.createdAt >= :since")
    long countSubscriptionsSince(@Param("since") LocalDateTime since);
    
    // Revenue-related queries
    @Query("SELECT SUM(p.amount) FROM Subscription s JOIN s.payments p WHERE s.type = 'PREMIUM' AND p.status = 'COMPLETED' AND s.createdAt >= :since")
    Double getTotalRevenueFromPremiumSubscriptionsSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT SUM(p.amount) FROM Subscription s JOIN s.payments p WHERE p.status = 'COMPLETED' AND s.createdAt >= :since")
    Double getTotalRevenueSince(@Param("since") LocalDateTime since);
    
    // User-related queries
    @Query("SELECT s FROM Subscription s WHERE s.user.active = true AND s.active = true")
    List<Subscription> findActiveSubscriptionsForActiveUsers();
    
    @Query("SELECT s FROM Subscription s WHERE s.user.role = 'READER' AND s.active = true")
    List<Subscription> findActiveReaderSubscriptions();
    
    // Premium subscription queries
    @Query("SELECT s FROM Subscription s WHERE s.type = 'PREMIUM' AND s.active = true")
    List<Subscription> findActivePremiumSubscriptions();
    
    @Query("SELECT s FROM Subscription s WHERE s.type = 'PREMIUM' AND s.endDate <= :date AND s.active = true")
    List<Subscription> findExpiredPremiumSubscriptions(@Param("date") LocalDateTime date);
    
    // Free subscription queries
    @Query("SELECT s FROM Subscription s WHERE s.type = 'FREE' AND s.active = true")
    List<Subscription> findActiveFreeSubscriptions();
    
    // Upgrade/downgrade tracking
    @Query("SELECT s FROM Subscription s WHERE s.user.id = :userId ORDER BY s.createdAt DESC")
    List<Subscription> findUserSubscriptionHistory(@Param("userId") Long userId);
    
    // Search queries
    @Query("SELECT s FROM Subscription s WHERE " +
           "(LOWER(s.user.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.user.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.user.email) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND s.active = true")
    Page<Subscription> searchActiveSubscriptions(@Param("search") String search, Pageable pageable);
    
    // Monthly statistics
    @Query("SELECT YEAR(s.createdAt), MONTH(s.createdAt), COUNT(s) FROM Subscription s " +
           "WHERE s.type = 'PREMIUM' AND s.createdAt >= :since " +
           "GROUP BY YEAR(s.createdAt), MONTH(s.createdAt) " +
           "ORDER BY YEAR(s.createdAt), MONTH(s.createdAt)")
    List<Object[]> getPremiumSubscriptionsByMonth(@Param("since") LocalDateTime since);
    
    @Query("SELECT YEAR(s.createdAt), MONTH(s.createdAt), SUM(p.amount) FROM Subscription s " +
           "JOIN s.payments p " +
           "WHERE s.type = 'PREMIUM' AND p.status = 'COMPLETED' AND s.createdAt >= :since " +
           "GROUP BY YEAR(s.createdAt), MONTH(s.createdAt) " +
           "ORDER BY YEAR(s.createdAt), MONTH(s.createdAt)")
    List<Object[]> getMonthlyRevenueFromPremiumSubscriptions(@Param("since") LocalDateTime since);
    
    // Subscription utilization
    @Query("SELECT s, COUNT(br) as borrowCount FROM Subscription s " +
           "LEFT JOIN s.user.borrowHistory br " +
           "WHERE s.active = true " +
           "GROUP BY s " +
           "ORDER BY borrowCount DESC")
    List<Object[]> findSubscriptionsWithBorrowCount(Pageable pageable);
    
    // Auto-renewal candidates (premium subscriptions expiring soon)
    @Query("SELECT s FROM Subscription s WHERE s.type = 'PREMIUM' AND s.endDate BETWEEN :now AND :renewalWindow AND s.active = true")
    List<Subscription> findPremiumSubscriptionsForRenewal(@Param("now") LocalDateTime now,
                                                         @Param("renewalWindow") LocalDateTime renewalWindow);
}