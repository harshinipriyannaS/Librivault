package com.librivault.controller;

import com.librivault.entity.Notification;
import com.librivault.security.CurrentUser;
import com.librivault.security.UserPrincipal;
import com.librivault.service.NotificationService;
import com.librivault.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@CrossOrigin(origins = "*", maxAge = 3600)
public class NotificationController {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public ResponseEntity<?> getMyNotifications(@CurrentUser UserPrincipal currentUser, Pageable pageable) {
        try {
            logger.info("Fetching notifications for user: {}", currentUser.getId());
            var user = userService.getUserEntityById(currentUser.getId());
            Page<Notification> notifications = notificationService.getUserNotifications(user, pageable);
            return ResponseEntity.ok(notifications);
            
        } catch (Exception e) {
            logger.error("Failed to fetch notifications for user: {}", currentUser.getId(), e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to fetch notifications", e.getMessage()));
        }
    }
    
    @GetMapping("/unread")
    public ResponseEntity<?> getMyUnreadNotifications(@CurrentUser UserPrincipal currentUser, Pageable pageable) {
        try {
            logger.info("Fetching unread notifications for user: {}", currentUser.getId());
            var user = userService.getUserEntityById(currentUser.getId());
            Page<Notification> notifications = notificationService.getUserUnreadNotifications(user, pageable);
            return ResponseEntity.ok(notifications);
            
        } catch (Exception e) {
            logger.error("Failed to fetch unread notifications for user: {}", currentUser.getId(), e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to fetch unread notifications", e.getMessage()));
        }
    }
    
    @GetMapping("/count/unread")
    public ResponseEntity<?> getUnreadNotificationCount(@CurrentUser UserPrincipal currentUser) {
        try {
            logger.info("Fetching unread notification count for user: {}", currentUser.getId());
            var user = userService.getUserEntityById(currentUser.getId());
            long unreadCount = notificationService.getUnreadNotificationCount(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("unreadCount", unreadCount);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to fetch unread notification count for user: {}", currentUser.getId(), e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to fetch unread count", e.getMessage()));
        }
    }
    
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markNotificationAsRead(@PathVariable Long id, @CurrentUser UserPrincipal currentUser) {
        try {
            logger.info("Marking notification {} as read for user: {}", id, currentUser.getId());
            var user = userService.getUserEntityById(currentUser.getId());
            notificationService.markNotificationAsRead(id, user);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Notification marked as read");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to mark notification {} as read for user: {}", id, currentUser.getId(), e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to mark notification as read", e.getMessage()));
        }
    }
    
    @PutMapping("/read-all")
    public ResponseEntity<?> markAllNotificationsAsRead(@CurrentUser UserPrincipal currentUser) {
        try {
            logger.info("Marking all notifications as read for user: {}", currentUser.getId());
            var user = userService.getUserEntityById(currentUser.getId());
            notificationService.markAllNotificationsAsRead(user);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "All notifications marked as read");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to mark all notifications as read for user: {}", currentUser.getId(), e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to mark all notifications as read", e.getMessage()));
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