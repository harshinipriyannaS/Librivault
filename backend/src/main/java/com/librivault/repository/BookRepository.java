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

import com.librivault.entity.Book;
import com.librivault.entity.Category;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    
    // Basic queries
    List<Book> findByActiveTrue();
    
    Page<Book> findByActiveTrue(Pageable pageable);
    
    Optional<Book> findByIdAndActiveTrue(Long id);
    
    Optional<Book> findByIsbn(String isbn);
    
    boolean existsByIsbn(String isbn);
    
    // Category-based queries
    List<Book> findByCategoryAndActiveTrue(Category category);
    
    Page<Book> findByCategoryAndActiveTrue(Category category, Pageable pageable);
    
    List<Book> findByCategoryIdAndActiveTrue(Long categoryId);
    
    Page<Book> findByCategoryIdAndActiveTrue(Long categoryId, Pageable pageable);
    
    @Query("SELECT COUNT(b) FROM Book b WHERE b.category.id = :categoryId AND b.active = true")
    long countActiveBooksInCategory(@Param("categoryId") Long categoryId);
    
    // Availability queries
    @Query("SELECT b FROM Book b WHERE b.availableCopies > 0 AND b.active = true")
    List<Book> findAvailableBooks();
    
    @Query("SELECT b FROM Book b WHERE b.availableCopies > 0 AND b.active = true")
    Page<Book> findAvailableBooks(Pageable pageable);
    
    @Query("SELECT b FROM Book b WHERE b.availableCopies > 0 AND b.category.id = :categoryId AND b.active = true")
    List<Book> findAvailableBooksInCategory(@Param("categoryId") Long categoryId);
    
    @Query("SELECT b FROM Book b WHERE b.availableCopies > 0 AND b.category.id = :categoryId AND b.active = true")
    Page<Book> findAvailableBooksInCategory(@Param("categoryId") Long categoryId, Pageable pageable);
    
    @Query("SELECT b FROM Book b WHERE b.availableCopies = 0 AND b.active = true")
    List<Book> findUnavailableBooks();
    
    // Search queries
    @Query("SELECT b FROM Book b WHERE " +
           "(LOWER(b.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(b.isbn) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(b.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND b.active = true")
    Page<Book> searchBooks(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT b FROM Book b WHERE " +
           "(LOWER(b.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(b.isbn) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(b.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND b.category.id = :categoryId AND b.active = true")
    Page<Book> searchBooksInCategory(@Param("search") String search, 
                                    @Param("categoryId") Long categoryId, 
                                    Pageable pageable);
    
    // Author-based queries
    List<Book> findByAuthorContainingIgnoreCaseAndActiveTrue(String author);
    
    Page<Book> findByAuthorContainingIgnoreCaseAndActiveTrue(String author, Pageable pageable);
    
    @Query("SELECT DISTINCT b.author FROM Book b WHERE b.active = true ORDER BY b.author")
    List<String> findDistinctAuthors();
    
    // Title-based queries
    List<Book> findByTitleContainingIgnoreCaseAndActiveTrue(String title);
    
    Page<Book> findByTitleContainingIgnoreCaseAndActiveTrue(String title, Pageable pageable);
    
    // Date-based queries
    List<Book> findByPublishedDateBetweenAndActiveTrue(LocalDateTime startDate, LocalDateTime endDate);
    
    Page<Book> findByPublishedDateBetweenAndActiveTrue(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    @Query("SELECT b FROM Book b WHERE YEAR(b.publishedDate) = :year AND b.active = true")
    List<Book> findByPublishedYear(@Param("year") int year);
    
    @Query("SELECT b FROM Book b WHERE YEAR(b.publishedDate) = :year AND b.active = true")
    Page<Book> findByPublishedYear(@Param("year") int year, Pageable pageable);
    
    // Statistics queries
    @Query("SELECT COUNT(b) FROM Book b WHERE b.active = true")
    long countActiveBooks();
    
    @Query("SELECT COUNT(b) FROM Book b WHERE b.availableCopies > 0 AND b.active = true")
    long countAvailableBooks();
    
    @Query("SELECT SUM(b.totalCopies) FROM Book b WHERE b.active = true")
    Long getTotalCopiesCount();
    
    @Query("SELECT SUM(b.availableCopies) FROM Book b WHERE b.active = true")
    Long getAvailableCopiesCount();
    
    // Most borrowed books (based on borrow records)
    @Query("SELECT b, COUNT(br) as borrowCount FROM Book b " +
           "LEFT JOIN b.borrowRecords br " +
           "WHERE b.active = true " +
           "GROUP BY b " +
           "ORDER BY borrowCount DESC")
    List<Object[]> findMostBorrowedBooks(Pageable pageable);
    
    // Recently added books
    @Query("SELECT b FROM Book b WHERE b.createdAt >= :since AND b.active = true ORDER BY b.createdAt DESC")
    List<Book> findRecentlyAddedBooks(@Param("since") LocalDateTime since);
    
    // Books with low availability
    @Query("SELECT b FROM Book b WHERE b.availableCopies <= :threshold AND b.availableCopies > 0 AND b.active = true")
    List<Book> findBooksWithLowAvailability(@Param("threshold") int threshold);
    
    // Advanced filtering
    @Query("SELECT b FROM Book b WHERE " +
           "(:categoryId IS NULL OR b.category.id = :categoryId) AND " +
           "(:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))) AND " +
           "(:year IS NULL OR YEAR(b.publishedDate) = :year) AND " +
           "(:availableOnly = false OR b.availableCopies > 0) AND " +
           "b.active = true")
    Page<Book> findBooksWithFilters(@Param("categoryId") Long categoryId,
                                   @Param("author") String author,
                                   @Param("year") Integer year,
                                   @Param("availableOnly") boolean availableOnly,
                                   Pageable pageable);
}