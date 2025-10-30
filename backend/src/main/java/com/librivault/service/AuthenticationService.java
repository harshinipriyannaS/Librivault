package com.librivault.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.librivault.dto.auth.JwtAuthenticationResponse;
import com.librivault.dto.auth.LoginRequest;
import com.librivault.dto.auth.RegisterRequest;
import com.librivault.entity.Subscription;
import com.librivault.entity.User;
import com.librivault.entity.enums.Role;
import com.librivault.entity.enums.SubscriptionType;
import com.librivault.repository.UserRepository;
import com.librivault.security.JwtTokenProvider;
import com.librivault.security.UserPrincipal;

@Service
public class AuthenticationService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Autowired
    private NotificationService notificationService;
    
    @Value("${app.subscription.free.book-limit}")
    private Integer freeBookLimit;
    
    @Value("${app.subscription.free.duration-days}")
    private Integer freeDurationDays;
    
    @Value("${app.subscription.free.daily-fine}")
    private BigDecimal freeDailyFine;
    
    @Transactional
    public JwtAuthenticationResponse registerUser(RegisterRequest registerRequest) {
        logger.info("Registering new user with email: {}", registerRequest.getEmail());
        
        // Check if email already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email address already in use!");
        }
        
        // Create new user
        User user = new User();
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(Role.READER); // Default role
        user.setActive(true);
        user.setReaderCredits(0);
        
        // Save user
        User savedUser = userRepository.save(user);
        
        // Create default free subscription
        createDefaultSubscription(savedUser);
        
        // Send welcome email
        try {
            notificationService.sendWelcomeEmail(savedUser);
        } catch (Exception e) {
            logger.warn("Failed to send welcome email to user: {}", savedUser.getEmail(), e);
        }
        
        // Authenticate the newly registered user
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                registerRequest.getEmail(),
                registerRequest.getPassword()
            )
        );
        
        String jwt = tokenProvider.generateToken(authentication);
        
        logger.info("User registered successfully: {}", savedUser.getEmail());
        
        return new JwtAuthenticationResponse(
            jwt,
            savedUser.getId(),
            savedUser.getEmail(),
            savedUser.getFirstName(),
            savedUser.getLastName(),
            savedUser.getRole(),
            tokenProvider.getExpirationTimeInMs()
        );
    }
    
    @Transactional
    public JwtAuthenticationResponse authenticateUser(LoginRequest loginRequest) {
        logger.info("Authenticating user: {}", loginRequest.getEmail());
        
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(),
                loginRequest.getPassword()
            )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String jwt = tokenProvider.generateToken(authentication);
        
        // Update last login time
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        
        logger.info("User authenticated successfully: {}", loginRequest.getEmail());
        
        return new JwtAuthenticationResponse(
            jwt,
            userPrincipal.getId(),
            userPrincipal.getEmail(),
            userPrincipal.getFirstName(),
            userPrincipal.getLastName(),
            userPrincipal.getRole(),
            tokenProvider.getExpirationTimeInMs()
        );
    }
    
    public JwtAuthenticationResponse refreshToken(String token) {
        logger.info("Refreshing JWT token");
        
        if (!tokenProvider.validateToken(token)) {
            throw new RuntimeException("Invalid token");
        }
        
        Long userId = tokenProvider.getUserIdFromToken(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!user.getActive()) {
            throw new RuntimeException("User account is deactivated");
        }
        
        String newToken = tokenProvider.generateTokenFromUserId(
            user.getId(),
            user.getEmail(),
            user.getRole().name(),
            user.getFirstName(),
            user.getLastName()
        );
        
        return new JwtAuthenticationResponse(
            newToken,
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getRole(),
            tokenProvider.getExpirationTimeInMs()
        );
    }
    
    public boolean validateToken(String token) {
        return tokenProvider.validateToken(token);
    }
    
    public UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            return (UserPrincipal) authentication.getPrincipal();
        }
        return null;
    }
    
    public Long getCurrentUserId() {
        UserPrincipal currentUser = getCurrentUser();
        return currentUser != null ? currentUser.getId() : null;
    }
    
    private void createDefaultSubscription(User user) {
        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setType(SubscriptionType.FREE);
        subscription.setStartDate(LocalDateTime.now());
        subscription.setEndDate(LocalDateTime.now().plusDays(freeDurationDays));
        subscription.setBookLimit(freeBookLimit);
        subscription.setBorrowDurationDays(freeDurationDays);
        subscription.setDailyFineAmount(freeDailyFine);
        subscription.setActive(true);
        
        user.setSubscription(subscription);
    }
    
    @Transactional
    public void logout(String token) {
        // In a stateless JWT implementation, logout is typically handled client-side
        // by removing the token. However, we can log the logout event.
        try {
            Long userId = tokenProvider.getUserIdFromToken(token);
            logger.info("User logged out: {}", userId);
        } catch (Exception e) {
            logger.warn("Error processing logout: {}", e.getMessage());
        }
    }
    
    public boolean isTokenExpired(String token) {
        return tokenProvider.isTokenExpired(token);
    }
}