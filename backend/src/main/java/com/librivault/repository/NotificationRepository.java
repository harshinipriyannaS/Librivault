package com.librivault.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.librivault.entity.Notification;
import com.librivault.entity.User;
import com.librivault.entity.enums.NotificationType;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Basic queries
    List<Notification> findByUser(User user);
    
    Page<Notification> findByUser(User user, Pageable pageable);
    
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    
    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    // Unread notifications
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);
    
    Page<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user, Pageable pageable);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user = :user AND n.isRead = false")
    long countUnreadByUser(@Param("user") User user);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.isRead = false")
    long countUnreadByUserId(@Param("userId") Long userId);
    
    // Read notifications
    List<Notification> findByUserAndIsReadTrueOrderByCreatedAtDesc(User user);
    
    Page<Notification> findByUserAndIsReadTrueOrderByCreatedAtDesc(User user, Pageable pageable);
    
    // Type-based queries
    List<Notification> findByUserAndType(User user, NotificationType type);
    
    Page<Notification> findByUserAndType(User user, NotificationType type, Pageable pageable);
    
    List<Notification> findByUserAndTypeOrderByCreatedAtDesc(User user, NotificationType type);
    
    // Date-based queries
    List<Notification> findByUserAndCreatedAtAfterOrderByCreatedAtDesc(User user, LocalDateTime since);
    
    List<Notification> findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(User user, LocalDateTime start, LocalDateTime end);
    
    // Recent notifications
    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.createdAt >= :since ORDER BY n.createdAt DESC")
    List<Notification> findRecentNotifications(@Param("user") User user, @Param("since") LocalDateTime since);
    
    // Reference-based queries
    List<Notification> findByUserAndReferenceIdAndReferenceType(User user, Long referenceId, String referenceType);
    
    // Bulk operations
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.user = :user AND n.isRead = false")
    int markAllAsReadForUser(@Param("user") User user, @Param("readAt") LocalDateTime readAt);
    
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.user = :user AND n.type = :type AND n.isRead = false")
    int markAllAsReadForUserAndType(@Param("user") User user, @Param("type") NotificationType type, @Param("readAt") LocalDateTime readAt);
    
    // Statistics
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user = :user")
    long countByUser(@Param("user") User user);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user = :user AND n.type = :type")
    long countByUserAndType(@Param("user") User user, @Param("type") NotificationType type);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.createdAt >= :since")
    long countNotificationsSince(@Param("since") LocalDateTime since);
    
    // Cleanup old notifications
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate")
    int deleteOldNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Query("DELETE FROM Notification n WHERE n.user = :user AND n.isRead = true AND n.createdAt < :cutoffDate")
    int deleteOldReadNotifications(@Param("user") User user, @Param("cutoffDate") LocalDateTime cutoffDate);
}