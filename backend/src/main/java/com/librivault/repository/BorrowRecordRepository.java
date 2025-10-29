package com.librivault.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.librivault.entity.Book;
import com.librivault.entity.BorrowRecord;
import com.librivault.entity.User;
import com.librivault.entity.enums.BorrowStatus;

@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {
    
    // Basic queries
    List<BorrowRecord> findByStatus(BorrowStatus status);
    
    Page<BorrowRecord> findByStatus(BorrowStatus status, Pageable pageable);
    
    List<BorrowRecord> findByReader(User reader);
    
    Page<BorrowRecord> findByReader(User reader, Pageable pageable);
    
    List<BorrowRecord> findByReaderAndStatus(User reader, BorrowStatus status);
    
    Page<BorrowRecord> findByReaderAndStatus(User reader, BorrowStatus status, Pageable pageable);
    
    // Active borrowing queries
    @Query("SELECT br FROM BorrowRecord br WHERE br.reader.id = :readerId AND br.status = 'ACTIVE'")
    List<BorrowRecord> findActiveRecordsByReader(@Param("readerId") Long readerId);
    
    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.reader.id = :readerId AND br.status = 'ACTIVE'")
    long countActiveRecordsByReader(@Param("readerId") Long readerId);
    
    // Book-related queries
    List<BorrowRecord> findByBook(Book book);
    
    List<BorrowRecord> findByBookAndStatus(Book book, BorrowStatus status);
    
    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.book.id = :bookId AND br.status = 'ACTIVE'")
    long countActiveBorrowsByBook(@Param("bookId") Long bookId);
    
    // Due date queries
    @Query("SELECT br FROM BorrowRecord br WHERE br.dueDate <= :date AND br.status = 'ACTIVE'")
    List<BorrowRecord> findOverdueRecords(@Param("date") LocalDateTime date);
    
    @Query("SELECT br FROM BorrowRecord br WHERE br.dueDate <= :date AND br.status = 'ACTIVE'")
    Page<BorrowRecord> findOverdueRecords(@Param("date") LocalDateTime date, Pageable pageable);
    
    @Query("SELECT br FROM BorrowRecord br WHERE br.dueDate BETWEEN :startDate AND :endDate AND br.status = 'ACTIVE'")
    List<BorrowRecord> findRecordsDueBetween(@Param("startDate") LocalDateTime startDate, 
                                            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT br FROM BorrowRecord br WHERE br.dueDate <= :date AND br.dueDate > :warningDate AND br.status = 'ACTIVE'")
    List<BorrowRecord> findRecordsDueSoon(@Param("date") LocalDateTime date, 
                                         @Param("warningDate") LocalDateTime warningDate);
    
    // Category-specific queries for librarians
    @Query("SELECT br FROM BorrowRecord br WHERE br.book.category.id = :categoryId AND br.status = :status")
    List<BorrowRecord> findByCategoryAndStatus(@Param("categoryId") Long categoryId, 
                                              @Param("status") BorrowStatus status);
    
    @Query("SELECT br FROM BorrowRecord br WHERE br.book.category.id = :categoryId AND br.status = :status")
    Page<BorrowRecord> findByCategoryAndStatus(@Param("categoryId") Long categoryId, 
                                              @Param("status") BorrowStatus status, 
                                              Pageable pageable);
    
    @Query("SELECT br FROM BorrowRecord br WHERE br.book.category.assignedLibrarian.user.id = :librarianUserId AND br.dueDate <= :date AND br.status = 'ACTIVE'")
    List<BorrowRecord> findOverdueRecordsForLibrarian(@Param("librarianUserId") Long librarianUserId, 
                                                     @Param("date") LocalDateTime date);
    
    @Query("SELECT br FROM BorrowRecord br WHERE br.book.category.assignedLibrarian.user.id = :librarianUserId AND br.dueDate <= :date AND br.status = 'ACTIVE'")
    Page<BorrowRecord> findOverdueRecordsForLibrarian(@Param("librarianUserId") Long librarianUserId, 
                                                     @Param("date") LocalDateTime date, 
                                                     Pageable pageable);
    
    @Query("SELECT br FROM BorrowRecord br WHERE br.book.category.assignedLibrarian.user.id = :librarianUserId AND br.dueDate BETWEEN :startDate AND :endDate AND br.status = 'ACTIVE'")
    List<BorrowRecord> findRecordsDueSoonForLibrarian(@Param("librarianUserId") Long librarianUserId,
                                                     @Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);
    
    // Credit-related queries
    @Query("SELECT br FROM BorrowRecord br WHERE br.usedCredit = true")
    List<BorrowRecord> findRecordsUsingCredits();
    
    @Query("SELECT br FROM BorrowRecord br WHERE br.creditsEarned > 0")
    List<BorrowRecord> findRecordsWithCreditsEarned();
    
    @Query("SELECT SUM(br.creditsEarned) FROM BorrowRecord br WHERE br.reader.id = :readerId")
    Long getTotalCreditsEarnedByReader(@Param("readerId") Long readerId);
    
    // Date-based queries
    List<BorrowRecord> findByBorrowedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<BorrowRecord> findByReturnedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT br FROM BorrowRecord br WHERE br.borrowedAt >= :since ORDER BY br.borrowedAt DESC")
    List<BorrowRecord> findRecentBorrows(@Param("since") LocalDateTime since);
    
    @Query("SELECT br FROM BorrowRecord br WHERE br.returnedAt >= :since AND br.status = 'RETURNED' ORDER BY br.returnedAt DESC")
    List<BorrowRecord> findRecentReturns(@Param("since") LocalDateTime since);
    
    // Statistics queries
    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.status = :status")
    long countByStatus(@Param("status") BorrowStatus status);
    
    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.borrowedAt >= :since")
    long countBorrowsSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.returnedAt >= :since AND br.status = 'RETURNED'")
    long countReturnsSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.dueDate <= :date AND br.status = 'ACTIVE'")
    long countOverdueRecords(@Param("date") LocalDateTime date);
    
    // Most borrowed books
    @Query("SELECT br.book, COUNT(br) as borrowCount FROM BorrowRecord br " +
           "GROUP BY br.book " +
           "ORDER BY borrowCount DESC")
    List<Object[]> findMostBorrowedBooks(Pageable pageable);
    
    // Most active readers
    @Query("SELECT br.reader, COUNT(br) as borrowCount FROM BorrowRecord br " +
           "GROUP BY br.reader " +
           "ORDER BY borrowCount DESC")
    List<Object[]> findMostActiveReaders(Pageable pageable);
    
    // Average borrow duration
    @Query("SELECT AVG(DATEDIFF(br.returnedAt, br.borrowedAt)) FROM BorrowRecord br WHERE br.status = 'RETURNED'")
    Double getAverageBorrowDurationInDays();
    
    // Search queries
    @Query("SELECT br FROM BorrowRecord br WHERE " +
           "(LOWER(br.book.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(br.book.author) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(br.reader.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(br.reader.lastName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND br.status = :status")
    Page<BorrowRecord> searchRecordsByStatus(@Param("search") String search, 
                                            @Param("status") BorrowStatus status, 
                                            Pageable pageable);
    
    // Reader's borrowing history
    @Query("SELECT br FROM BorrowRecord br WHERE br.reader.id = :readerId ORDER BY br.borrowedAt DESC")
    Page<BorrowRecord> findReaderBorrowHistory(@Param("readerId") Long readerId, Pageable pageable);
    
    // Early returns (for credit calculation)
    @Query("SELECT br FROM BorrowRecord br WHERE br.returnedAt < br.dueDate AND br.status = 'RETURNED'")
    List<BorrowRecord> findEarlyReturns();
    
    @Query("SELECT br FROM BorrowRecord br WHERE br.returnedAt < br.dueDate AND br.status = 'RETURNED' AND br.reader.id = :readerId")
    List<BorrowRecord> findEarlyReturnsByReader(@Param("readerId") Long readerId);
    
    // Find active borrow record for specific user and book
    @Query("SELECT br FROM BorrowRecord br WHERE br.reader = :reader AND br.book = :book AND br.returnedAt IS NULL")
    java.util.Optional<BorrowRecord> findByReaderAndBookAndReturnedAtIsNull(@Param("reader") User reader, @Param("book") Book book);
}