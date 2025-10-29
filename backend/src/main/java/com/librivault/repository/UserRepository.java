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

import com.librivault.entity.User;
import com.librivault.entity.enums.Role;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Authentication queries
    Optional<User> findByEmail(String email);
    
    Optional<User> findByEmailAndActiveTrue(String email);
    
    boolean existsByEmail(String email);
    
    // Role-based queries
    List<User> findByRole(Role role);
    
    List<User> findByRoleAndActiveTrue(Role role);
    
    Page<User> findByRole(Role role, Pageable pageable);
    
    Page<User> findByRoleAndActiveTrue(Role role, Pageable pageable);
    
    // Search queries
    @Query("SELECT u FROM User u WHERE " +
           "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND u.active = true")
    Page<User> searchActiveUsers(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE " +
           "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND u.role = :role AND u.active = true")
    Page<User> searchActiveUsersByRole(@Param("search") String search, 
                                      @Param("role") Role role, 
                                      Pageable pageable);
    
    // Statistics queries
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.active = true")
    long countActiveUsersByRole(@Param("role") Role role);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.active = true")
    long countActiveUsers();
    
    // Recent activity queries
    @Query("SELECT u FROM User u WHERE u.lastLogin >= :since AND u.active = true ORDER BY u.lastLogin DESC")
    List<User> findRecentlyActiveUsers(@Param("since") LocalDateTime since);
    
    @Query("SELECT u FROM User u WHERE u.createdAt >= :since ORDER BY u.createdAt DESC")
    List<User> findRecentlyRegisteredUsers(@Param("since") LocalDateTime since);
    
    // Credit-related queries
    @Query("SELECT u FROM User u WHERE u.readerCredits > 0 AND u.role = :role AND u.active = true")
    List<User> findUsersWithCredits(@Param("role") Role role);
    
    @Query("SELECT SUM(u.readerCredits) FROM User u WHERE u.role = :role AND u.active = true")
    Long getTotalCreditsByRole(@Param("role") Role role);
    
    // Subscription-related queries
    @Query("SELECT u FROM User u JOIN u.subscription s WHERE s.type = :subscriptionType AND u.active = true")
    List<User> findUsersBySubscriptionType(@Param("subscriptionType") com.librivault.entity.enums.SubscriptionType subscriptionType);
    
    @Query("SELECT COUNT(u) FROM User u JOIN u.subscription s WHERE s.type = :subscriptionType AND u.active = true")
    long countUsersBySubscriptionType(@Param("subscriptionType") com.librivault.entity.enums.SubscriptionType subscriptionType);
}