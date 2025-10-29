package com.librivault.controller;

import java.math.BigDecimal;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.librivault.dto.borrow.BorrowRecordResponse;
import com.librivault.dto.borrow.BorrowRequestResponse;
import com.librivault.dto.borrow.FineResponse;
import com.librivault.security.CurrentUser;
import com.librivault.security.UserPrincipal;
import com.librivault.service.BorrowingService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/borrow")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BorrowingController {
    
    private static final Logger logger = LoggerFactory.getLogger(BorrowingController.class);
    
    @Autowired
    private BorrowingService borrowingService;
    
    // Reader endpoints
    
    @PostMapping("/request")
    public ResponseEntity<?> createBorrowRequest(@Valid @RequestBody CreateBorrowRequestRequest request, 
                                                @CurrentUser UserPrincipal currentUser) {
        try {
            logger.info("Creating borrow request for book: {} by user: {}", request.bookId, currentUser.getId());
            BorrowRequestResponse borrowRequest = borrowingService.createBorrowRequest(request.bookId, currentUser);
            return ResponseEntity.ok(borrowRequest);
            
        } catch (Exception e) {
            logger.error("Failed to create borrow request for book: {} by user: {}", request.bookId, currentUser.getId(), e);
            return ResponseEntity.badRequest().body(createErrorResponse("Borrow request failed", e.getMessage()));
        }
    }
    
    @GetMapping("/my-requests")
    public ResponseEntity<?> getMyBorrowRequests(@CurrentUser UserPrincipal currentUser, Pageable pageable) {
        try {
            logger.info("Fetching borrow requests for user: {}", currentUser.getId());
            Page<BorrowRequestResponse> requests = borrowingService.getUserBorrowRequests(currentUser.getId(), pageable);
            return ResponseEntity.ok(requests);
            
        } catch (Exception e) {
            logger.error("Failed to fetch borrow requests for user: {}", currentUser.getId(), e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to fetch borrow requests", e.getMessage()));
        }
    }
    
    @GetMapping("/my-books")
    public ResponseEntity<?> getMyBorrowHistory(@CurrentUser UserPrincipal currentUser, Pageable pageable) {
        try {
            logger.info("Fetching borrow history for user: {}", currentUser.getId());
            Page<BorrowRecordResponse> history = borrowingService.getUserBorrowHistory(currentUser.getId(), pageable);
            return ResponseEntity.ok(history);
            
        } catch (Exception e) {
            logger.error("Failed to fetch borrow history for user: {}", currentUser.getId(), e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to fetch borrow history", e.getMessage()));
        }
    }
    
    @GetMapping("/my-active-books")
    public ResponseEntity<?> getMyActiveBorrows(@CurrentUser UserPrincipal currentUser) {
        try {
            logger.info("Fetching active borrows for user: {}", currentUser.getId());
            List<BorrowRecordResponse> activeBorrows = borrowingService.getUserActiveBorrows(currentUser.getId());
            return ResponseEntity.ok(activeBorrows);
            
        } catch (Exception e) {
            logger.error("Failed to fetch active borrows for user: {}", currentUser.getId(), e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to fetch active borrows", e.getMessage()));
        }
    }
    
    @PostMapping("/{id}/return")
    public ResponseEntity<?> returnBook(@PathVariable Long id, @CurrentUser UserPrincipal currentUser) {
        try {
            logger.info("Returning book for borrow record: {} by user: {}", id, currentUser.getId());
            BorrowRecordResponse borrowRecord = borrowingService.returnBook(id, currentUser);
            return ResponseEntity.ok(borrowRecord);
            
        } catch (Exception e) {
            logger.error("Failed to return book for borrow record: {} by user: {}", id, currentUser.getId(), e);
            return ResponseEntity.badRequest().body(createErrorResponse("Book return failed", e.getMessage()));
        }
    }
    
    @GetMapping("/my-fines")
    public ResponseEntity<?> getMyOutstandingFines(@CurrentUser UserPrincipal currentUser) {
        try {
            logger.info("Fetching outstanding fines for user: {}", currentUser.getId());
            List<FineResponse> fines = borrowingService.getUserOutstandingFines(currentUser.getId());
            BigDecimal totalFines = borrowingService.getUserTotalOutstandingFines(currentUser.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("fines", fines);
            response.put("totalAmount", totalFines);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to fetch outstanding fines for user: {}", currentUser.getId(), e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to fetch fines", e.getMessage()));
        }
    }
    
    // Librarian endpoints
    
    @GetMapping("/requests")
    public ResponseEntity<?> getPendingRequestsForLibrarian(@CurrentUser UserPrincipal currentUser, Pageable pageable) {
        try {
            logger.info("Fetching pending requests for librarian: {}", currentUser.getId());
            Page<BorrowRequestResponse> requests = borrowingService.getPendingRequestsForLibrarian(currentUser, pageable);
            return ResponseEntity.ok(requests);
            
        } catch (Exception e) {
            logger.error("Failed to fetch pending requests for librarian: {}", currentUser.getId(), e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to fetch pending requests", e.getMessage()));
        }
    }
    
    @PutMapping("/requests/{id}/approve")
    public ResponseEntity<?> approveBorrowRequest(@PathVariable Long id, 
                                                 @Valid @RequestBody ReviewBorrowRequestRequest request,
                                                 @CurrentUser UserPrincipal currentUser) {
        try {
            logger.info("Approving borrow request: {} by librarian: {}", id, currentUser.getId());
            BorrowRequestResponse borrowRequest = borrowingService.approveBorrowRequest(id, request.notes, currentUser);
            return ResponseEntity.ok(borrowRequest);
            
        } catch (Exception e) {
            logger.error("Failed to approve borrow request: {} by librarian: {}", id, currentUser.getId(), e);
            return ResponseEntity.badRequest().body(createErrorResponse("Request approval failed", e.getMessage()));
        }
    }
    
    @PutMapping("/requests/{id}/decline")
    public ResponseEntity<?> declineBorrowRequest(@PathVariable Long id, 
                                                 @Valid @RequestBody ReviewBorrowRequestRequest request,
                                                 @CurrentUser UserPrincipal currentUser) {
        try {
            logger.info("Declining borrow request: {} by librarian: {}", id, currentUser.getId());
            BorrowRequestResponse borrowRequest = borrowingService.declineBorrowRequest(id, request.notes, currentUser);
            return ResponseEntity.ok(borrowRequest);
            
        } catch (Exception e) {
            logger.error("Failed to decline borrow request: {} by librarian: {}", id, currentUser.getId(), e);
            return ResponseEntity.badRequest().body(createErrorResponse("Request decline failed", e.getMessage()));
        }
    }
    
    @GetMapping("/overdue/my-category")
    public ResponseEntity<?> getOverdueRecordsForLibrarian(@CurrentUser UserPrincipal currentUser, Pageable pageable) {
        try {
            logger.info("Fetching overdue records for librarian: {}", currentUser.getId());
            Page<BorrowRecordResponse> overdueRecords = borrowingService.getOverdueRecordsForLibrarian(currentUser, pageable);
            return ResponseEntity.ok(overdueRecords);
            
        } catch (Exception e) {
            logger.error("Failed to fetch overdue records for librarian: {}", currentUser.getId(), e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to fetch overdue records", e.getMessage()));
        }
    }
    
    // Admin/Librarian endpoints
    
    @GetMapping("/overdue")
    public ResponseEntity<?> getOverdueRecords(Pageable pageable) {
        try {
            logger.info("Fetching all overdue records");
            Page<BorrowRecordResponse> overdueRecords = borrowingService.getOverdueRecords(pageable);
            return ResponseEntity.ok(overdueRecords);
            
        } catch (Exception e) {
            logger.error("Failed to fetch overdue records", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to fetch overdue records", e.getMessage()));
        }
    }
    
    @GetMapping("/users/{userId}/requests")
    public ResponseEntity<?> getUserBorrowRequests(@PathVariable Long userId, Pageable pageable) {
        try {
            logger.info("Fetching borrow requests for user: {}", userId);
            Page<BorrowRequestResponse> requests = borrowingService.getUserBorrowRequests(userId, pageable);
            return ResponseEntity.ok(requests);
            
        } catch (Exception e) {
            logger.error("Failed to fetch borrow requests for user: {}", userId, e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to fetch borrow requests", e.getMessage()));
        }
    }
    
    @GetMapping("/users/{userId}/history")
    public ResponseEntity<?> getUserBorrowHistory(@PathVariable Long userId, Pageable pageable) {
        try {
            logger.info("Fetching borrow history for user: {}", userId);
            Page<BorrowRecordResponse> history = borrowingService.getUserBorrowHistory(userId, pageable);
            return ResponseEntity.ok(history);
            
        } catch (Exception e) {
            logger.error("Failed to fetch borrow history for user: {}", userId, e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to fetch borrow history", e.getMessage()));
        }
    }
    
    @GetMapping("/users/{userId}/active-books")
    public ResponseEntity<?> getUserActiveBorrows(@PathVariable Long userId) {
        try {
            logger.info("Fetching active borrows for user: {}", userId);
            List<BorrowRecordResponse> activeBorrows = borrowingService.getUserActiveBorrows(userId);
            return ResponseEntity.ok(activeBorrows);
            
        } catch (Exception e) {
            logger.error("Failed to fetch active borrows for user: {}", userId, e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to fetch active borrows", e.getMessage()));
        }
    }
    
    @GetMapping("/users/{userId}/fines")
    public ResponseEntity<?> getUserOutstandingFines(@PathVariable Long userId) {
        try {
            logger.info("Fetching outstanding fines for user: {}", userId);
            List<FineResponse> fines = borrowingService.getUserOutstandingFines(userId);
            BigDecimal totalFines = borrowingService.getUserTotalOutstandingFines(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("fines", fines);
            response.put("totalAmount", totalFines);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to fetch outstanding fines for user: {}", userId, e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to fetch fines", e.getMessage()));
        }
    }
    
    // Fine management endpoints
    
    @PutMapping("/fines/{id}/waive")
    public ResponseEntity<?> waiveFine(@PathVariable Long id, 
                                      @Valid @RequestBody WaiveFineRequest request,
                                      @CurrentUser UserPrincipal currentUser) {
        try {
            logger.info("Waiving fine: {} by user: {}", id, currentUser.getId());
            FineResponse fine = borrowingService.waiveFine(id, request.reason, currentUser);
            return ResponseEntity.ok(fine);
            
        } catch (Exception e) {
            logger.error("Failed to waive fine: {} by user: {}", id, currentUser.getId(), e);
            return ResponseEntity.badRequest().body(createErrorResponse("Fine waiver failed", e.getMessage()));
        }
    }
    
    // Statistics endpoints
    
    @GetMapping("/stats/active-borrows")
    public ResponseEntity<?> getTotalActiveBorrows() {
        try {
            long activeBorrows = borrowingService.getTotalActiveBorrows();
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalActiveBorrows", activeBorrows);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get total active borrows", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to get statistics", e.getMessage()));
        }
    }
    
    @GetMapping("/stats/overdue-books")
    public ResponseEntity<?> getTotalOverdueBooks() {
        try {
            long overdueBooks = borrowingService.getTotalOverdueBooks();
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalOverdueBooks", overdueBooks);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get total overdue books", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to get statistics", e.getMessage()));
        }
    }
    
    @GetMapping("/stats/outstanding-fines")
    public ResponseEntity<?> getTotalOutstandingFines() {
        try {
            BigDecimal outstandingFines = borrowingService.getTotalOutstandingFines();
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalOutstandingFines", outstandingFines);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get total outstanding fines", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to get statistics", e.getMessage()));
        }
    }
    
    @GetMapping("/stats/most-borrowed-books")
    public ResponseEntity<?> getMostBorrowedBooks(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<Object[]> mostBorrowed = borrowingService.getMostBorrowedBooks(limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("limit", limit);
            response.put("mostBorrowedBooks", mostBorrowed);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get most borrowed books", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to get statistics", e.getMessage()));
        }
    }
    
    @GetMapping("/stats/most-active-readers")
    public ResponseEntity<?> getMostActiveReaders(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<Object[]> mostActive = borrowingService.getMostActiveReaders(limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("limit", limit);
            response.put("mostActiveReaders", mostActive);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get most active readers", e);
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
    public static class CreateBorrowRequestRequest {
        @NotNull(message = "Book ID is required")
        public Long bookId;
    }
    
    public static class ReviewBorrowRequestRequest {
        public String notes;
    }
    
    public static class WaiveFineRequest {
        @NotBlank(message = "Reason is required")
        public String reason;
    }
}