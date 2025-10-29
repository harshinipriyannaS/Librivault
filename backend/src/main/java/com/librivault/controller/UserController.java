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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.librivault.dto.user.UserResponse;
import com.librivault.entity.enums.Role;
import com.librivault.security.CurrentUser;
import com.librivault.security.UserPrincipal;
import com.librivault.service.UserService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    private UserService userService;
    
    // Get all users (Admin/Librarian only)
    @GetMapping
    public ResponseEntity<?> getAllUsers(Pageable pageable) {
        try {
            logger.info("Fetching all users with pagination");
            Page<UserResponse> users = userService.getAllUsers(pageable);
            return ResponseEntity.ok(users);
            
        } catch (Exception e) {
            logger.error("Failed to fetch users", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to fetch users", e.getMessage()));
        }
    }
    
    // Get users by role
    @GetMapping("/role/{role}")
    public ResponseEntity<?> getUsersByRole(@PathVariable Role role, Pageable pageable) {
        try {
            logger.info("Fetching users by role: {}", role);
            Page<UserResponse> users = userService.getUsersByRole(role, pageable);
            return ResponseEntity.ok(users);
            
        } catch (Exception e) {
            logger.error("Failed to fetch users by role: {}", role, e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to fetch users by role", e.getMessage()));
        }
    }
    
    // Get user by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            logger.info("Fetching user by ID: {}", id);
            UserResponse user = userService.getUserById(id);
            return ResponseEntity.ok(user);
            
        } catch (Exception e) {
            logger.error("Failed to fetch user by ID: {}", id, e);
            return ResponseEntity.badRequest().body(createErrorResponse("User not found", e.getMessage()));
        }
    }
    
    // Search users
    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestParam String query, Pageable pageable) {
        try {
            logger.info("Searching users with query: {}", query);
            Page<UserResponse> users = userService.searchUsers(query, pageable);
            return ResponseEntity.ok(users);
            
        } catch (Exception e) {
            logger.error("Failed to search users with query: {}", query, e);
            return ResponseEntity.badRequest().body(createErrorResponse("Search failed", e.getMessage()));
        }
    }
    
    // Search users by role
    @GetMapping("/search/role/{role}")
    public ResponseEntity<?> searchUsersByRole(@RequestParam String query, @PathVariable Role role, Pageable pageable) {
        try {
            logger.info("Searching users by role {} with query: {}", role, query);
            Page<UserResponse> users = userService.searchUsersByRole(query, role, pageable);
            return ResponseEntity.ok(users);
            
        } catch (Exception e) {
            logger.error("Failed to search users by role {} with query: {}", role, query, e);
            return ResponseEntity.badRequest().body(createErrorResponse("Search failed", e.getMessage()));
        }
    }
    
    // Update user profile
    @PutMapping("/{id}/profile")
    public ResponseEntity<?> updateUserProfile(@PathVariable Long id, @Valid @RequestBody UpdateProfileRequest request) {
        try {
            logger.info("Updating profile for user: {}", id);
            UserResponse user = userService.updateUserProfile(id, request.firstName, request.lastName, request.email);
            return ResponseEntity.ok(user);
            
        } catch (Exception e) {
            logger.error("Failed to update profile for user: {}", id, e);
            return ResponseEntity.badRequest().body(createErrorResponse("Profile update failed", e.getMessage()));
        }
    }
    
    // Change password
    @PutMapping("/{id}/password")
    public ResponseEntity<?> changePassword(@PathVariable Long id, @Valid @RequestBody ChangePasswordRequest request) {
        try {
            logger.info("Changing password for user: {}", id);
            userService.changePassword(id, request.currentPassword, request.newPassword);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Password changed successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to change password for user: {}", id, e);
            return ResponseEntity.badRequest().body(createErrorResponse("Password change failed", e.getMessage()));
        }
    }
    
    // Change user role (Admin only)
    @PutMapping("/{id}/role")
    public ResponseEntity<?> changeUserRole(@PathVariable Long id, @Valid @RequestBody ChangeRoleRequest request) {
        try {
            logger.info("Changing role for user {} to {}", id, request.role);
            UserResponse user = userService.changeUserRole(id, request.role, request.categoryId);
            return ResponseEntity.ok(user);
            
        } catch (Exception e) {
            logger.error("Failed to change role for user: {}", id, e);
            return ResponseEntity.badRequest().body(createErrorResponse("Role change failed", e.getMessage()));
        }
    }
    
    // Activate user (Admin only)
    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activateUser(@PathVariable Long id) {
        try {
            logger.info("Activating user: {}", id);
            userService.activateUser(id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "User activated successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to activate user: {}", id, e);
            return ResponseEntity.badRequest().body(createErrorResponse("User activation failed", e.getMessage()));
        }
    }
    
    // Deactivate user (Admin only)
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateUser(@PathVariable Long id) {
        try {
            logger.info("Deactivating user: {}", id);
            userService.deactivateUser(id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "User deactivated successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to deactivate user: {}", id, e);
            return ResponseEntity.badRequest().body(createErrorResponse("User deactivation failed", e.getMessage()));
        }
    }
    
    // Add credits to user
    @PutMapping("/{id}/credits/add")
    public ResponseEntity<?> addCredits(@PathVariable Long id, @Valid @RequestBody CreditsRequest request) {
        try {
            logger.info("Adding {} credits to user: {}", request.credits, id);
            userService.addCreditsToUser(id, request.credits);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Credits added successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to add credits to user: {}", id, e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to add credits", e.getMessage()));
        }
    }
    
    // Get current user profile
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUserProfile(@CurrentUser UserPrincipal currentUser) {
        try {
            logger.info("Fetching current user profile: {}", currentUser.getId());
            UserResponse user = userService.getUserById(currentUser.getId());
            return ResponseEntity.ok(user);
            
        } catch (Exception e) {
            logger.error("Failed to fetch current user profile", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to fetch profile", e.getMessage()));
        }
    }
    
    // Update current user profile
    @PutMapping("/me")
    public ResponseEntity<?> updateCurrentUserProfile(@CurrentUser UserPrincipal currentUser, 
                                                     @Valid @RequestBody UpdateProfileRequest request) {
        try {
            logger.info("Updating current user profile: {}", currentUser.getId());
            UserResponse user = userService.updateUserProfile(currentUser.getId(), request.firstName, request.lastName, request.email);
            return ResponseEntity.ok(user);
            
        } catch (Exception e) {
            logger.error("Failed to update current user profile", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Profile update failed", e.getMessage()));
        }
    }
    
    // Statistics endpoints (Admin only)
    @GetMapping("/stats/total")
    public ResponseEntity<?> getTotalActiveUsers() {
        try {
            long totalUsers = userService.getTotalActiveUsers();
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalActiveUsers", totalUsers);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get total active users", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to get statistics", e.getMessage()));
        }
    }
    
    @GetMapping("/stats/role/{role}")
    public ResponseEntity<?> getActiveUsersByRole(@PathVariable Role role) {
        try {
            long userCount = userService.getActiveUsersByRole(role);
            
            Map<String, Object> response = new HashMap<>();
            response.put("role", role);
            response.put("count", userCount);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get users count by role: {}", role, e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to get statistics", e.getMessage()));
        }
    }
    
    @GetMapping("/stats/recent")
    public ResponseEntity<?> getRecentlyRegisteredUsers(@RequestParam(defaultValue = "30") int days) {
        try {
            List<UserResponse> recentUsers = userService.getRecentlyRegisteredUsers(days);
            
            Map<String, Object> response = new HashMap<>();
            response.put("days", days);
            response.put("users", recentUsers);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get recently registered users", e);
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
    public static class UpdateProfileRequest {
        @NotBlank(message = "First name is required")
        public String firstName;
        
        @NotBlank(message = "Last name is required")
        public String lastName;
        
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        public String email;
    }
    
    public static class ChangePasswordRequest {
        @NotBlank(message = "Current password is required")
        public String currentPassword;
        
        @NotBlank(message = "New password is required")
        public String newPassword;
    }
    
    public static class ChangeRoleRequest {
        @NotNull(message = "Role is required")
        public Role role;
        
        public Long categoryId; // Required only for LIBRARIAN role
    }
    
    public static class CreditsRequest {
        @NotNull(message = "Credits amount is required")
        public Integer credits;
    }
}