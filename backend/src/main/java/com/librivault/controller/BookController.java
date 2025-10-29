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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.librivault.dto.book.BookRequest;
import com.librivault.dto.book.BookResponse;
import com.librivault.security.CurrentUser;
import com.librivault.security.UserPrincipal;
import com.librivault.service.BookService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/books")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BookController {
    
    private static final Logger logger = LoggerFactory.getLogger(BookController.class);
    
    @Autowired
    private BookService bookService;
    
    // Public endpoints (no authentication required)
    
    @GetMapping
    public ResponseEntity<?> getAllBooks(Pageable pageable) {
        try {
            logger.info("Fetching all books with pagination");
            Page<BookResponse> books = bookService.getAllBooks(pageable);
            return ResponseEntity.ok(books);
            
        } catch (Exception e) {
            logger.error("Failed to fetch books", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to fetch books", e.getMessage()));
        }
    }
    
    @GetMapping("/available")
    public ResponseEntity<?> getAvailableBooks(Pageable pageable) {
        try {
            logger.info("Fetching available books with pagination");
            Page<BookResponse> books = bookService.getAvailableBooks(pageable);
            return ResponseEntity.ok(books);
            
        } catch (Exception e) {
            logger.error("Failed to fetch available books", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to fetch available books", e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getBookById(@PathVariable Long id) {
        try {
            logger.info("Fetching book by ID: {}", id);
            BookResponse book = bookService.getBookById(id);
            return ResponseEntity.ok(book);
            
        } catch (Exception e) {
            logger.error("Failed to fetch book by ID: {}", id, e);
            return ResponseEntity.badRequest().body(createErrorResponse("Book not found", e.getMessage()));
        }
    }
    
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getBooksByCategory(@PathVariable Long categoryId, Pageable pageable) {
        try {
            logger.info("Fetching books by category: {}", categoryId);
            Page<BookResponse> books = bookService.getBooksByCategory(categoryId, pageable);
            return ResponseEntity.ok(books);
            
        } catch (Exception e) {
            logger.error("Failed to fetch books by category: {}", categoryId, e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to fetch books by category", e.getMessage()));
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<?> searchBooks(@RequestParam String query, Pageable pageable) {
        try {
            logger.info("Searching books with query: {}", query);
            Page<BookResponse> books = bookService.searchBooks(query, pageable);
            return ResponseEntity.ok(books);
            
        } catch (Exception e) {
            logger.error("Failed to search books with query: {}", query, e);
            return ResponseEntity.badRequest().body(createErrorResponse("Search failed", e.getMessage()));
        }
    }
    
    @GetMapping("/search/category/{categoryId}")
    public ResponseEntity<?> searchBooksInCategory(@RequestParam String query, @PathVariable Long categoryId, Pageable pageable) {
        try {
            logger.info("Searching books in category {} with query: {}", categoryId, query);
            Page<BookResponse> books = bookService.searchBooksInCategory(query, categoryId, pageable);
            return ResponseEntity.ok(books);
            
        } catch (Exception e) {
            logger.error("Failed to search books in category {} with query: {}", categoryId, query, e);
            return ResponseEntity.badRequest().body(createErrorResponse("Search failed", e.getMessage()));
        }
    }
    
    @GetMapping("/filter")
    public ResponseEntity<?> filterBooks(@RequestParam(required = false) Long categoryId,
                                        @RequestParam(required = false) String author,
                                        @RequestParam(required = false) Integer year,
                                        @RequestParam(defaultValue = "false") boolean availableOnly,
                                        Pageable pageable) {
        try {
            logger.info("Filtering books with category: {}, author: {}, year: {}, availableOnly: {}", 
                       categoryId, author, year, availableOnly);
            Page<BookResponse> books = bookService.filterBooks(categoryId, author, year, availableOnly, pageable);
            return ResponseEntity.ok(books);
            
        } catch (Exception e) {
            logger.error("Failed to filter books", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Filter failed", e.getMessage()));
        }
    }
    
    @GetMapping("/authors")
    public ResponseEntity<?> getDistinctAuthors() {
        try {
            logger.info("Fetching distinct authors");
            List<String> authors = bookService.getDistinctAuthors();
            
            Map<String, Object> response = new HashMap<>();
            response.put("authors", authors);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to fetch distinct authors", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to fetch authors", e.getMessage()));
        }
    }
    
    // Authenticated endpoints
    
    @GetMapping("/{id}/download")
    public ResponseEntity<?> getFullBookUrl(@PathVariable Long id, @CurrentUser UserPrincipal currentUser) {
        try {
            logger.info("Generating full book URL for book: {} and user: {}", id, currentUser.getId());
            String bookUrl = bookService.getFullBookUrl(id, currentUser);
            
            Map<String, String> response = new HashMap<>();
            response.put("downloadUrl", bookUrl);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to generate full book URL for book: {} and user: {}", id, currentUser.getId(), e);
            return ResponseEntity.badRequest().body(createErrorResponse("Book access denied", e.getMessage()));
        }
    }
    
    // Admin/Librarian endpoints
    
    @PostMapping
    public ResponseEntity<?> createBook(@Valid @RequestPart("book") BookRequest bookRequest,
                                       @RequestPart(value = "bookFile", required = false) MultipartFile bookFile,
                                       @RequestPart(value = "coverImage", required = false) MultipartFile coverImage) {
        try {
            logger.info("Creating new book: {}", bookRequest.getTitle());
            BookResponse book = bookService.createBook(bookRequest, bookFile, coverImage);
            return ResponseEntity.ok(book);
            
        } catch (Exception e) {
            logger.error("Failed to create book: {}", bookRequest.getTitle(), e);
            return ResponseEntity.badRequest().body(createErrorResponse("Book creation failed", e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBook(@PathVariable Long id,
                                       @Valid @RequestPart("book") BookRequest bookRequest,
                                       @RequestPart(value = "bookFile", required = false) MultipartFile bookFile,
                                       @RequestPart(value = "coverImage", required = false) MultipartFile coverImage) {
        try {
            logger.info("Updating book: {}", id);
            BookResponse book = bookService.updateBook(id, bookRequest, bookFile, coverImage);
            return ResponseEntity.ok(book);
            
        } catch (Exception e) {
            logger.error("Failed to update book: {}", id, e);
            return ResponseEntity.badRequest().body(createErrorResponse("Book update failed", e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable Long id) {
        try {
            logger.info("Deleting book: {}", id);
            bookService.deleteBook(id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Book deleted successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to delete book: {}", id, e);
            return ResponseEntity.badRequest().body(createErrorResponse("Book deletion failed", e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<?> permanentlyDeleteBook(@PathVariable Long id) {
        try {
            logger.info("Permanently deleting book: {}", id);
            bookService.permanentlyDeleteBook(id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Book permanently deleted successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to permanently delete book: {}", id, e);
            return ResponseEntity.badRequest().body(createErrorResponse("Permanent deletion failed", e.getMessage()));
        }
    }
    
    // Secure book access endpoints
    
    @GetMapping("/{id}/access")
    @PreAuthorize("hasRole('READER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<?> getSecureBookAccess(@PathVariable Long id, Authentication authentication) {
        try {
            logger.info("Generating secure book access for book: {} by user: {}", id, authentication.getName());
            
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            String secureUrl = bookService.getSecureBookUrl(id, userPrincipal.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("bookId", id);
            response.put("secureUrl", secureUrl);
            response.put("expiresIn", "2 hours");
            response.put("message", "Secure access URL generated successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to generate secure book access for book: {}", id, e);
            return ResponseEntity.badRequest().body(createErrorResponse("Access denied", e.getMessage()));
        }
    }
    
    @GetMapping("/{id}/preview")
    public ResponseEntity<?> getBookPreview(@PathVariable Long id) {
        try {
            logger.info("Generating preview URL for book: {}", id);
            
            String previewUrl = bookService.getBookPreviewUrl(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("bookId", id);
            response.put("previewUrl", previewUrl);
            response.put("expiresIn", "24 hours");
            response.put("message", "Preview URL generated successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to generate preview URL for book: {}", id, e);
            return ResponseEntity.badRequest().body(createErrorResponse("Preview not available", e.getMessage()));
        }
    }
    
    @GetMapping("/{id}/cover")
    public ResponseEntity<?> getBookCover(@PathVariable Long id) {
        try {
            logger.info("Generating cover image URL for book: {}", id);
            
            String coverUrl = bookService.getBookCoverUrl(id);
            
            if (coverUrl != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("bookId", id);
                response.put("coverUrl", coverUrl);
                response.put("expiresIn", "24 hours");
                response.put("message", "Cover image URL generated successfully");
                
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> response = new HashMap<>();
                response.put("message", "No cover image available for this book");
                return ResponseEntity.ok(response);
            }
            
        } catch (Exception e) {
            logger.error("Failed to generate cover image URL for book: {}", id, e);
            return ResponseEntity.badRequest().body(createErrorResponse("Cover image not available", e.getMessage()));
        }
    }
    
    @PostMapping("/{id}/refresh-access")
    @PreAuthorize("hasRole('READER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<?> refreshBookAccess(@PathVariable Long id, Authentication authentication) {
        try {
            logger.info("Refreshing book access for book: {} by user: {}", id, authentication.getName());
            
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            String refreshedUrl = bookService.refreshBookUrl(id, userPrincipal.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("bookId", id);
            response.put("secureUrl", refreshedUrl);
            response.put("expiresIn", "2 hours");
            response.put("message", "Book access URL refreshed successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to refresh book access for book: {}", id, e);
            return ResponseEntity.badRequest().body(createErrorResponse("Access refresh failed", e.getMessage()));
        }
    }
    
    @GetMapping("/{id}/access-status")
    @PreAuthorize("hasRole('READER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<?> checkBookAccessStatus(@PathVariable Long id, Authentication authentication) {
        try {
            logger.info("Checking book access status for book: {} by user: {}", id, authentication.getName());
            
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            boolean hasAccess = bookService.canUserAccessBook(id, userPrincipal.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("bookId", id);
            response.put("hasAccess", hasAccess);
            response.put("message", hasAccess ? "User has access to this book" : "User does not have access to this book");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to check book access status for book: {}", id, e);
            return ResponseEntity.badRequest().body(createErrorResponse("Access check failed", e.getMessage()));
        }
    }
    
    @GetMapping("/{id}/metadata")
    public ResponseEntity<?> getBookMetadata(@PathVariable Long id) {
        try {
            logger.info("Getting metadata for book: {}", id);
            
            Map<String, Object> metadata = bookService.getBookMetadata(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("bookId", id);
            response.put("metadata", metadata);
            response.put("message", "Book metadata retrieved successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get book metadata for book: {}", id, e);
            return ResponseEntity.badRequest().body(createErrorResponse("Metadata not available", e.getMessage()));
        }
    }
    
    // Statistics endpoints
    
    @GetMapping("/stats/total")
    public ResponseEntity<?> getTotalActiveBooks() {
        try {
            long totalBooks = bookService.getTotalActiveBooks();
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalActiveBooks", totalBooks);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get total active books", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to get statistics", e.getMessage()));
        }
    }
    
    @GetMapping("/stats/available")
    public ResponseEntity<?> getTotalAvailableBooks() {
        try {
            long availableBooks = bookService.getTotalAvailableBooks();
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalAvailableBooks", availableBooks);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get total available books", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to get statistics", e.getMessage()));
        }
    }
    
    @GetMapping("/stats/recent")
    public ResponseEntity<?> getRecentlyAddedBooks(@RequestParam(defaultValue = "30") int days) {
        try {
            List<BookResponse> recentBooks = bookService.getRecentlyAddedBooks(days);
            
            Map<String, Object> response = new HashMap<>();
            response.put("days", days);
            response.put("books", recentBooks);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get recently added books", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to get statistics", e.getMessage()));
        }
    }
    
    @GetMapping("/stats/most-borrowed")
    public ResponseEntity<?> getMostBorrowedBooks(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<Object[]> mostBorrowed = bookService.getMostBorrowedBooks(limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("limit", limit);
            response.put("mostBorrowedBooks", mostBorrowed);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get most borrowed books", e);
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
}