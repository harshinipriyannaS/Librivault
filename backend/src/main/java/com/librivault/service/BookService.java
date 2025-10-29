package com.librivault.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.librivault.dto.book.BookRequest;
import com.librivault.dto.book.BookResponse;
import com.librivault.entity.Book;
import com.librivault.entity.Category;
import com.librivault.entity.User;
import com.librivault.repository.BookRepository;
import com.librivault.repository.CategoryRepository;
import com.librivault.repository.UserRepository;
import com.librivault.security.UserPrincipal;

@Service
public class BookService {
    
    private static final Logger logger = LoggerFactory.getLogger(BookService.class);
    
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private S3Service s3Service;
    
    // Public book browsing (no authentication required)
    public Page<BookResponse> getAllBooks(Pageable pageable) {
        logger.info("Fetching all active books with pagination");
        return bookRepository.findByActiveTrue(pageable)
                .map(this::convertToBookResponse);
    }
    
    public Page<BookResponse> getAvailableBooks(Pageable pageable) {
        logger.info("Fetching available books with pagination");
        return bookRepository.findAvailableBooks(pageable)
                .map(this::convertToBookResponse);
    }
    
    public BookResponse getBookById(Long bookId) {
        logger.info("Fetching book by ID: {}", bookId);
        Book book = bookRepository.findByIdAndActiveTrue(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + bookId));
        return convertToBookResponse(book);
    }
    
    public Page<BookResponse> getBooksByCategory(Long categoryId, Pageable pageable) {
        logger.info("Fetching books by category: {}", categoryId);
        return bookRepository.findByCategoryIdAndActiveTrue(categoryId, pageable)
                .map(this::convertToBookResponse);
    }
    
    public Page<BookResponse> searchBooks(String search, Pageable pageable) {
        logger.info("Searching books with query: {}", search);
        return bookRepository.searchBooks(search, pageable)
                .map(this::convertToBookResponse);
    }
    
    public Page<BookResponse> searchBooksInCategory(String search, Long categoryId, Pageable pageable) {
        logger.info("Searching books in category {} with query: {}", categoryId, search);
        return bookRepository.searchBooksInCategory(search, categoryId, pageable)
                .map(this::convertToBookResponse);
    }
    
    public Page<BookResponse> filterBooks(Long categoryId, String author, Integer year, 
                                         boolean availableOnly, Pageable pageable) {
        logger.info("Filtering books with category: {}, author: {}, year: {}, availableOnly: {}", 
                   categoryId, author, year, availableOnly);
        return bookRepository.findBooksWithFilters(categoryId, author, year, availableOnly, pageable)
                .map(this::convertToBookResponse);
    }
    
    public List<String> getDistinctAuthors() {
        logger.info("Fetching distinct authors");
        return bookRepository.findDistinctAuthors();
    }
    
    // Preview functionality (public access - first 5 pages only)
    public String getBookPreviewUrl(Long bookId) {
        logger.info("Generating preview URL for book: {}", bookId);
        Book book = bookRepository.findByIdAndActiveTrue(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + bookId));
        
        if (book.getS3Uri() == null) {
            throw new RuntimeException("Book PDF not available for preview");
        }
        
        // Same PDF file, but frontend will limit to first 5 pages for preview
        return s3Service.generatePreviewUrl(book.getS3Uri());
    }
    
    // Secure book access (requires authentication and borrow approval)
    @PreAuthorize("hasRole('READER')")
    public String getFullBookUrl(Long bookId, UserPrincipal currentUser) {
        logger.info("Generating full book URL for book: {} and user: {}", bookId, currentUser.getId());
        
        Book book = bookRepository.findByIdAndActiveTrue(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + bookId));
        
        // TODO: Check if user has active borrow record for this book
        // This will be implemented in BorrowingService
        
        if (book.getS3Uri() == null) {
            throw new RuntimeException("Book file not available");
        }
        
        return s3Service.generateFullBookUrl(book.getS3Uri());
    }
    
    // Book management (Admin/Librarian only)
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Transactional
    public BookResponse createBook(BookRequest bookRequest, MultipartFile bookFile, 
                                  MultipartFile coverImage) {
        logger.info("Creating new book: {}", bookRequest.getTitle());
        
        // Validate category
        Category category = categoryRepository.findByIdAndActiveTrue(bookRequest.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + bookRequest.getCategoryId()));
        
        // Check ISBN uniqueness if provided
        if (bookRequest.getIsbn() != null && bookRepository.existsByIsbn(bookRequest.getIsbn())) {
            throw new RuntimeException("Book with ISBN " + bookRequest.getIsbn() + " already exists");
        }
        
        // Create book entity
        Book book = new Book();
        book.setTitle(bookRequest.getTitle());
        book.setAuthor(bookRequest.getAuthor());
        book.setIsbn(bookRequest.getIsbn());
        book.setDescription(bookRequest.getDescription());
        book.setCategory(category);
        book.setTotalCopies(bookRequest.getTotalCopies());
        book.setAvailableCopies(bookRequest.getTotalCopies());
        book.setPublishedDate(bookRequest.getPublishedDate());
        book.setActive(true);
        
        // Upload files to S3
        if (bookFile != null && !bookFile.isEmpty()) {
            String s3Uri = s3Service.uploadBookFile(bookFile, book.getTitle());
            book.setS3Uri(s3Uri);
        }
        
        if (coverImage != null && !coverImage.isEmpty()) {
            String coverImageUri = s3Service.uploadCoverImage(coverImage, book.getTitle());
            book.setCoverImageUri(coverImageUri);
        }
        
        Book savedBook = bookRepository.save(book);
        logger.info("Book created successfully with ID: {}", savedBook.getId());
        
        return convertToBookResponse(savedBook);
    }
    
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Transactional
    public BookResponse updateBook(Long bookId, BookRequest bookRequest, MultipartFile bookFile, 
                                  MultipartFile coverImage) {
        logger.info("Updating book: {}", bookId);
        
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + bookId));
        
        // Validate category
        Category category = categoryRepository.findByIdAndActiveTrue(bookRequest.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + bookRequest.getCategoryId()));
        
        // Check ISBN uniqueness if changed
        if (bookRequest.getIsbn() != null && !bookRequest.getIsbn().equals(book.getIsbn()) 
            && bookRepository.existsByIsbn(bookRequest.getIsbn())) {
            throw new RuntimeException("Book with ISBN " + bookRequest.getIsbn() + " already exists");
        }
        
        // Update book details
        book.setTitle(bookRequest.getTitle());
        book.setAuthor(bookRequest.getAuthor());
        book.setIsbn(bookRequest.getIsbn());
        book.setDescription(bookRequest.getDescription());
        book.setCategory(category);
        book.setPublishedDate(bookRequest.getPublishedDate());
        
        // Update total copies (but maintain available copies logic)
        int difference = bookRequest.getTotalCopies() - book.getTotalCopies();
        book.setTotalCopies(bookRequest.getTotalCopies());
        book.setAvailableCopies(Math.max(0, book.getAvailableCopies() + difference));
        
        // Update files if provided
        if (bookFile != null && !bookFile.isEmpty()) {
            // Delete old file if exists
            if (book.getS3Uri() != null) {
                s3Service.deleteFile(book.getS3Uri());
            }
            String s3Uri = s3Service.uploadBookFile(bookFile, book.getTitle());
            book.setS3Uri(s3Uri);
        }
        
        if (coverImage != null && !coverImage.isEmpty()) {
            // Delete old cover if exists
            if (book.getCoverImageUri() != null) {
                s3Service.deleteFile(book.getCoverImageUri());
            }
            String coverImageUri = s3Service.uploadCoverImage(coverImage, book.getTitle());
            book.setCoverImageUri(coverImageUri);
        }
        
        Book updatedBook = bookRepository.save(book);
        logger.info("Book updated successfully: {}", bookId);
        
        return convertToBookResponse(updatedBook);
    }
    
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Transactional
    public void deleteBook(Long bookId) {
        logger.info("Deleting book: {}", bookId);
        
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + bookId));
        
        // Soft delete - just mark as inactive
        book.setActive(false);
        bookRepository.save(book);
        
        logger.info("Book deleted (deactivated) successfully: {}", bookId);
    }
    
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Transactional
    public void permanentlyDeleteBook(Long bookId) {
        logger.info("Permanently deleting book: {}", bookId);
        
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + bookId));
        
        // Delete files from S3
        if (book.getS3Uri() != null) {
            s3Service.deleteFile(book.getS3Uri());
        }
        if (book.getCoverImageUri() != null) {
            s3Service.deleteFile(book.getCoverImageUri());
        }
        
        // Delete from database
        bookRepository.delete(book);
        
        logger.info("Book permanently deleted: {}", bookId);
    }
    
    // Statistics and reporting
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public long getTotalActiveBooks() {
        return bookRepository.countActiveBooks();
    }
    
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public long getTotalAvailableBooks() {
        return bookRepository.countAvailableBooks();
    }
    
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public List<BookResponse> getRecentlyAddedBooks(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return bookRepository.findRecentlyAddedBooks(since)
                .stream()
                .map(this::convertToBookResponse)
                .collect(Collectors.toList());
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    public List<Object[]> getMostBorrowedBooks(int limit) {
        return bookRepository.findMostBorrowedBooks(org.springframework.data.domain.PageRequest.of(0, limit));
    }
    
    // Secure book access methods
    public String getSecureBookUrl(Long bookId, Long userId) {
        logger.info("Generating secure book URL for book: {} and user: {}", bookId, userId);
        
        Book book = bookRepository.findByIdAndActiveTrue(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + bookId));
        
        // Get user entity (you'll need to inject UserRepository)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        return s3Service.getSecureBookUrl(book, user);
    }
    
    public String getBookCoverUrl(Long bookId) {
        logger.info("Generating cover URL for book: {}", bookId);
        
        Book book = bookRepository.findByIdAndActiveTrue(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + bookId));
        
        return s3Service.getCoverImageUrl(book);
    }
    
    public String refreshBookUrl(Long bookId, Long userId) {
        logger.info("Refreshing book URL for book: {} and user: {}", bookId, userId);
        
        Book book = bookRepository.findByIdAndActiveTrue(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + bookId));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        return s3Service.refreshBookUrl(book, user);
    }
    
    public boolean canUserAccessBook(Long bookId, Long userId) {
        logger.info("Checking book access for book: {} and user: {}", bookId, userId);
        
        Book book = bookRepository.findByIdAndActiveTrue(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + bookId));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        return s3Service.canUserAccessFullBook(book, user);
    }
    
    public Map<String, Object> getBookMetadata(Long bookId) {
        logger.info("Getting metadata for book: {}", bookId);
        
        Book book = bookRepository.findByIdAndActiveTrue(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + bookId));
        
        S3Service.BookFileMetadata metadata = s3Service.getBookMetadata(book);
        
        Map<String, Object> result = new HashMap<>();
        result.put("bookId", bookId);
        result.put("title", book.getTitle());
        result.put("author", book.getAuthor());
        result.put("fileExists", metadata != null ? metadata.exists() : false);
        result.put("fileSize", metadata != null ? metadata.getFileSize() : 0);
        result.put("formattedFileSize", metadata != null ? metadata.getFormattedFileSize() : "Unknown");
        result.put("hasPreview", book.getS3Uri() != null && !book.getS3Uri().isEmpty());
        result.put("hasCover", book.getCoverImageUri() != null && !book.getCoverImageUri().isEmpty());
        
        return result;
    }
    
    // Helper methods
    private BookResponse convertToBookResponse(Book book) {
        String librarianName = null;
        if (book.getCategory().getAssignedLibrarian() != null) {
            librarianName = book.getCategory().getAssignedLibrarian().getUser().getFullName();
        }
        
        return new BookResponse(
            book.getId(),
            book.getTitle(),
            book.getAuthor(),
            book.getIsbn(),
            book.getDescription(),
            book.getCoverImageUri(),
            book.getTotalCopies(),
            book.getAvailableCopies(),
            book.getPublishedDate(),
            book.getActive(),
            book.getCreatedAt(),
            book.getCategory().getName(),
            book.getCategory().getId()
        );
    }
    
    public Book getBookEntityById(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + bookId));
    }
    
    @Transactional
    public void decreaseAvailableCopies(Long bookId) {
        Book book = getBookEntityById(bookId);
        book.decreaseAvailableCopies();
        bookRepository.save(book);
    }
    
    @Transactional
    public void increaseAvailableCopies(Long bookId) {
        Book book = getBookEntityById(bookId);
        book.increaseAvailableCopies();
        bookRepository.save(book);
    }
}