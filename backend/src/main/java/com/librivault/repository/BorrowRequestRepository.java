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
import com.librivault.entity.BorrowRequest;
import com.librivault.entity.User;
import com.librivault.entity.enums.RequestStatus;

@Repository
public interface BorrowRequestRepository extends JpaRepository<BorrowRequest, Long> {
    
    // Basic queries
    List<BorrowRequest> findByStatus(RequestStatus status);
    
    Page<BorrowRequest> findByStatus(RequestStatus status, Pageable pageable);
    
    List<BorrowRequest> findByReader(User reader);
    
    Page<BorrowRequest> findByReader(User reader, Pageable pageable);
    
    List<BorrowRequest> findByReaderAndStatus(User reader, RequestStatus status);
    
    Page<BorrowRequest> findByReaderAndStatus(User reader, RequestStatus status, Pageable pageable);
    
    // Book-related queries
    List<BorrowRequest> findByBook(Book book);
    
    List<BorrowRequest> findByBookAndStatus(Book book, RequestStatus status);
    
    Optional<BorrowRequest> findByReaderAndBookAndStatus(User reader, Book book, RequestStatus status);
    
    boolean existsByReaderAndBookAndStatus(User reader, Book book, RequestStatus status);
    
    // Librarian-specific queries (for assigned category)
    @Query("SELECT br FROM BorrowRequest br WHERE br.book.category.id = :categoryId AND br.status = :status")
    List<BorrowRequest> findByCategoryAndStatus(@Param("categoryId") Long categoryId, 
                                               @Param("status") RequestStatus status);
    
    @Query("SELECT br FROM BorrowRequest br WHERE br.book.category.id = :categoryId AND br.status = :status")
    Page<BorrowRequest> findByCategoryAndStatus(@Param("categoryId") Long categoryId, 
                                               @Param("status") RequestStatus status, 
                                               Pageable pageable);
    
    @Query("SELECT br FROM BorrowRequest br WHERE br.book.category.id = :categoryId")
    List<BorrowRequest> findByCategory(@Param("categoryId") Long categoryId);
    
    @Query("SELECT br FROM BorrowRequest br WHERE br.book.category.id = :categoryId")
    Page<BorrowRequest> findByCategory(@Param("categoryId") Long categoryId, Pageable pageable);
    
    // Pending requests for librarian
    @Query("SELECT br FROM BorrowRequest br WHERE br.book.category.assignedLibrarian.user.id = :librarianUserId AND br.status = 'PENDING'")
    List<BorrowRequest> findPendingRequestsForLibrarian(@Param("librarianUserId") Long librarianUserId);
    
    @Query("SELECT br FROM BorrowRequest br WHERE br.book.category.assignedLibrarian.user.id = :librarianUserId AND br.status = 'PENDING'")
    Page<BorrowRequest> findPendingRequestsForLibrarian(@Param("librarianUserId") Long librarianUserId, Pageable pageable);
    
    // All requests for librarian's category
    @Query("SELECT br FROM BorrowRequest br WHERE br.book.category.assignedLibrarian.user.id = :librarianUserId")
    List<BorrowRequest> findRequestsForLibrarian(@Param("librarianUserId") Long librarianUserId);
    
    @Query("SELECT br FROM BorrowRequest br WHERE br.book.category.assignedLibrarian.user.id = :librarianUserId")
    Page<BorrowRequest> findRequestsForLibrarian(@Param("librarianUserId") Long librarianUserId, Pageable pageable);
    
    // Date-based queries
    List<BorrowRequest> findByRequestedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<BorrowRequest> findByRequestedAtBetweenAndStatus(LocalDateTime startDate, LocalDateTime endDate, RequestStatus status);
    
    @Query("SELECT br FROM BorrowRequest br WHERE br.requestedAt >= :since ORDER BY br.requestedAt DESC")
    List<BorrowRequest> findRecentRequests(@Param("since") LocalDateTime since);
    
    @Query("SELECT br FROM BorrowRequest br WHERE br.reviewedAt >= :since AND br.status != 'PENDING' ORDER BY br.reviewedAt DESC")
    List<BorrowRequest> findRecentlyReviewedRequests(@Param("since") LocalDateTime since);
    
    // Statistics queries
    @Query("SELECT COUNT(br) FROM BorrowRequest br WHERE br.status = :status")
    long countByStatus(@Param("status") RequestStatus status);
    
    @Query("SELECT COUNT(br) FROM BorrowRequest br WHERE br.book.category.id = :categoryId AND br.status = :status")
    long countByCategoryAndStatus(@Param("categoryId") Long categoryId, @Param("status") RequestStatus status);
    
    @Query("SELECT COUNT(br) FROM BorrowRequest br WHERE br.reader.id = :readerId AND br.status = 'PENDING'")
    long countPendingRequestsByReader(@Param("readerId") Long readerId);
    
    @Query("SELECT COUNT(br) FROM BorrowRequest br WHERE br.requestedAt >= :since")
    long countRequestsSince(@Param("since") LocalDateTime since);
    
    // Search queries
    @Query("SELECT br FROM BorrowRequest br WHERE " +
           "(LOWER(br.book.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(br.book.author) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(br.reader.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(br.reader.lastName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND br.status = :status")
    Page<BorrowRequest> searchRequestsByStatus(@Param("search") String search, 
                                              @Param("status") RequestStatus status, 
                                              Pageable pageable);
    
    // Overdue pending requests (requests that have been pending for too long)
    @Query("SELECT br FROM BorrowRequest br WHERE br.status = 'PENDING' AND br.requestedAt < :threshold")
    List<BorrowRequest> findOverduePendingRequests(@Param("threshold") LocalDateTime threshold);
    
    // Most requested books
    @Query("SELECT br.book, COUNT(br) as requestCount FROM BorrowRequest br " +
           "GROUP BY br.book " +
           "ORDER BY requestCount DESC")
    List<Object[]> findMostRequestedBooks(Pageable pageable);
    
    // User's request history
    @Query("SELECT br FROM BorrowRequest br WHERE br.reader.id = :readerId ORDER BY br.requestedAt DESC")
    Page<BorrowRequest> findReaderRequestHistory(@Param("readerId") Long readerId, Pageable pageable);
}