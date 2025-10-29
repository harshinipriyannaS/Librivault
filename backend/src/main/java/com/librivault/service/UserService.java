package com.librivault.service;

import com.librivault.dto.user.UserResponse;
import com.librivault.entity.Category;
import com.librivault.entity.Librarian;
import com.librivault.entity.User;
import com.librivault.entity.enums.Role;
import com.librivault.repository.CategoryRepository;
import com.librivault.repository.LibrarianRepository;
import com.librivault.repository.UserRepository;
import com.librivault.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private LibrarianRepository librarianRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private NotificationService notificationService;
    
    // User CRUD operations
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        logger.info("Fetching all users with pagination");
        return userRepository.findAll(pageable)
                .map(this::convertToUserResponse);
    }
    
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public Page<UserResponse> getUsersByRole(Role role, Pageable pageable) {
        logger.info("Fetching users by role: {}", role);
        return userRepository.findByRoleAndActiveTrue(role, pageable)
                .map(this::convertToUserResponse);
    }
    
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or #userId == authentication.principal.id")
    public UserResponse getUserById(Long userId) {
        logger.info("Fetching user by ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return convertToUserResponse(user);
    }
    
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public Page<UserResponse> searchUsers(String search, Pageable pageable) {
        logger.info("Searching users with query: {}", search);
        return userRepository.searchActiveUsers(search, pageable)
                .map(this::convertToUserResponse);
    }
    
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public Page<UserResponse> searchUsersByRole(String search, Role role, Pageable pageable) {
        logger.info("Searching users by role {} with query: {}", role, search);
        return userRepository.searchActiveUsersByRole(search, role, pageable)
                .map(this::convertToUserResponse);
    }
    
    // Profile management
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    @Transactional
    public UserResponse updateUserProfile(Long userId, String firstName, String lastName, String email) {
        logger.info("Updating profile for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        // Check if email is already taken by another user
        if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email address already in use!");
        }
        
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        
        User updatedUser = userRepository.save(user);
        logger.info("Profile updated successfully for user: {}", userId);
        
        return convertToUserResponse(updatedUser);
    }
    
    @PreAuthorize("#userId == authentication.principal.id")
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        logger.info("Changing password for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        logger.info("Password changed successfully for user: {}", userId);
    }
    
    // Role management (Admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserResponse changeUserRole(Long userId, Role newRole, Long categoryId) {
        logger.info("Changing role for user {} to {}", userId, newRole);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        Role oldRole = user.getRole();
        user.setRole(newRole);
        
        // Handle librarian role assignment
        if (newRole == Role.LIBRARIAN) {
            assignLibrarianToCategory(user, categoryId);
        } else if (oldRole == Role.LIBRARIAN) {
            removeLibrarianAssignment(user);
        }
        
        User updatedUser = userRepository.save(user);
        
        // Send notification about role change
        try {
            notificationService.sendRoleChangeNotification(updatedUser, oldRole, newRole);
        } catch (Exception e) {
            logger.warn("Failed to send role change notification to user: {}", user.getEmail(), e);
        }
        
        logger.info("Role changed successfully for user: {} from {} to {}", userId, oldRole, newRole);
        return convertToUserResponse(updatedUser);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void activateUser(Long userId) {
        logger.info("Activating user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        user.setActive(true);
        userRepository.save(user);
        
        logger.info("User activated successfully: {}", userId);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deactivateUser(Long userId) {
        logger.info("Deactivating user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        user.setActive(false);
        userRepository.save(user);
        
        logger.info("User deactivated successfully: {}", userId);
    }
    
    // Credit management
    @Transactional
    public void addCreditsToUser(Long userId, Integer credits) {
        logger.info("Adding {} credits to user: {}", credits, userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        user.setReaderCredits(user.getReaderCredits() + credits);
        userRepository.save(user);
        
        logger.info("Credits added successfully to user: {}", userId);
    }
    
    @Transactional
    public void deductCreditsFromUser(Long userId, Integer credits) {
        logger.info("Deducting {} credits from user: {}", credits, userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        if (user.getReaderCredits() < credits) {
            throw new RuntimeException("Insufficient credits");
        }
        
        user.setReaderCredits(user.getReaderCredits() - credits);
        userRepository.save(user);
        
        logger.info("Credits deducted successfully from user: {}", userId);
    }
    
    // Librarian management
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void assignLibrarianToCategory(User librarian, Long categoryId) {
        logger.info("Assigning librarian {} to category: {}", librarian.getId(), categoryId);
        
        if (librarian.getRole() != Role.LIBRARIAN) {
            throw new RuntimeException("User is not a librarian");
        }
        
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));
        
        // Check if category already has a librarian
        if (librarianRepository.existsByAssignedCategory(category)) {
            throw new RuntimeException("Category already has an assigned librarian");
        }
        
        // Remove existing assignment if any
        librarianRepository.findByUser(librarian).ifPresent(existingLibrarian -> {
            existingLibrarian.setAssignedCategory(null);
            librarianRepository.save(existingLibrarian);
        });
        
        // Create or update librarian assignment
        Librarian librarianEntity = librarianRepository.findByUser(librarian)
                .orElse(new Librarian(librarian, category));
        
        librarianEntity.setAssignedCategory(category);
        librarianRepository.save(librarianEntity);
        
        logger.info("Librarian assigned successfully to category");
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void removeLibrarianAssignment(User librarian) {
        logger.info("Removing librarian assignment for user: {}", librarian.getId());
        
        librarianRepository.findByUser(librarian).ifPresent(librarianEntity -> {
            librarianEntity.setAssignedCategory(null);
            librarianRepository.save(librarianEntity);
        });
        
        logger.info("Librarian assignment removed successfully");
    }
    
    // Statistics
    @PreAuthorize("hasRole('ADMIN')")
    public long getTotalActiveUsers() {
        return userRepository.countActiveUsers();
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    public long getActiveUsersByRole(Role role) {
        return userRepository.countActiveUsersByRole(role);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getRecentlyRegisteredUsers(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return userRepository.findRecentlyRegisteredUsers(since)
                .stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getRecentlyActiveUsers(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return userRepository.findRecentlyActiveUsers(since)
                .stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }
    
    // Helper methods
    private UserResponse convertToUserResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getRole(),
            user.getActive(),
            user.getReaderCredits(),
            user.getCreatedAt(),
            user.getLastLogin()
        );
    }
    
    public User getUserEntityById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }
    
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}