package com.librivault.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.librivault.repository.BookRepository;
import com.librivault.repository.CategoryRepository;
import com.librivault.repository.UserRepository;

@RestController
@RequestMapping("/")
public class HealthController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "LibriVault Backend API");
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health/ready")
    public ResponseEntity<Map<String, Object>> readiness() {
        Map<String, Object> response = new HashMap<>();
        try {
            // Check database connectivity and data availability
            long userCount = userRepository.count();
            long bookCount = bookRepository.count();
            long categoryCount = categoryRepository.count();
            
            if (userCount > 0 && bookCount > 0 && categoryCount > 0) {
                response.put("status", "READY");
                response.put("database", "CONNECTED");
                response.put("dataLoaded", true);
                response.put("userCount", userCount);
                response.put("bookCount", bookCount);
                response.put("categoryCount", categoryCount);
                response.put("timestamp", LocalDateTime.now());
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "NOT_READY");
                response.put("database", "CONNECTED");
                response.put("dataLoaded", false);
                response.put("userCount", userCount);
                response.put("bookCount", bookCount);
                response.put("categoryCount", categoryCount);
                response.put("message", "Database connected but data not fully loaded");
                response.put("timestamp", LocalDateTime.now());
                return ResponseEntity.status(503).body(response);
            }
        } catch (Exception e) {
            response.put("status", "NOT_READY");
            response.put("database", "ERROR");
            response.put("dataLoaded", false);
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(503).body(response);
        }
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to LibriVault API");
        response.put("status", "Running");
        response.put("timestamp", LocalDateTime.now());
        response.put("version", "1.0.0");
        response.put("endpoints", Map.of(
            "health", "/api/health",
            "auth", "/api/auth/*",
            "books", "/api/books",
            "categories", "/api/categories"
        ));
        return ResponseEntity.ok(response);
    }
}