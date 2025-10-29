package com.librivault.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.librivault.entity.BorrowRecord;
import com.librivault.entity.Fine;
import com.librivault.entity.User;
import com.librivault.entity.enums.FineStatus;

@Repository
public interface FineRepository extends JpaRepository<Fine, Long> {
    
    // Basic queries
    List<Fine> findByReader(User reader);
    
    Page<Fine> findByReader(User reader, Pageable pageable);
    
    List<Fine> findByReaderAndStatus(User reader, FineStatus status);
    
    Page<Fine> findByReaderAndStatus(User reader, FineStatus status, Pageable pageable);
    
    List<Fine> findByStatus(FineStatus status);
    
    Page<Fine> findByStatus(FineStatus status, Pageable pageable);
    
    Optional<Fine> findByBorrowRecord(BorrowRecord borrowRecord);
    
    List<Fine> findByBorrowRecordIn(List<BorrowRecord> borrowRecords);
    
    // Outstanding fines queries
    @Query("SELECT f FROM Fine f WHERE f.reader.id = :readerId AND f.status = 'PENDING'")
    List<Fine> findOutstandingFinesByReader(@Param("readerId") Long readerId);
    
    @Query("SELECT SUM(f.amount) FROM Fine f WHERE f.reader.id = :readerId AND f.status = 'PENDING'")
    BigDecimal getTotalOutstandingFinesByReader(@Param("readerId") Long readerId);
    
    @Query("SELECT COUNT(f) FROM Fine f WHERE f.reader.id = :readerId AND f.status = 'PENDING'")
    long countOutstandingFinesByReader(@Param("readerId") Long readerId);
    
    // Check if user has outstanding fines
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Fine f WHERE f.reader.id = :readerId AND f.status = 'PENDING'")
    boolean hasOutstandingFines(@Param("readerId") Long readerId);
    
    // Amount-based queries
    List<Fine> findByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);
    
    List<Fine> findByAmountGreaterThanEqual(BigDecimal amount);
    
    List<Fine> findByAmountLessThanEqual(BigDecimal amount);
    
    // Overdue days queries
    List<Fine> findByOverdueDaysGreaterThanEqual(Integer days);
    
    List<Fine> findByOverdueDaysLessThanEqual(Integer days);
    
    List<Fine> findByOverdueDaysBetween(Integer minDays, Integer maxDays);
    
    // Date-based queries
    List<Fine> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<Fine> findByPaidAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<Fine> findByWaivedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT f FROM Fine f WHERE f.createdAt >= :since ORDER BY f.createdAt DESC")
    List<Fine> findRecentFines(@Param("since") LocalDateTime since);
    
    @Query("SELECT f FROM Fine f WHERE f.paidAt >= :since AND f.status = 'PAID' ORDER BY f.paidAt DESC")
    List<Fine> findRecentlyPaidFines(@Param("since") LocalDateTime since);
    
    // Category-specific queries for librarians
    @Query("SELECT f FROM Fine f WHERE f.borrowRecord.book.category.id = :categoryId AND f.status = :status")
    List<Fine> findByCategoryAndStatus(@Param("categoryId") Long categoryId, 
                                      @Param("status") FineStatus status);
    
    @Query("SELECT f FROM Fine f WHERE f.borrowRecord.book.category.assignedLibrarian.user.id = :librarianUserId AND f.status = 'PENDING'")
    List<Fine> findOutstandingFinesForLibrarian(@Param("librarianUserId") Long librarianUserId);
    
    @Query("SELECT f FROM Fine f WHERE f.borrowRecord.book.category.assignedLibrarian.user.id = :librarianUserId")
    List<Fine> findFinesForLibrarian(@Param("librarianUserId") Long librarianUserId);
    
    @Query("SELECT f FROM Fine f WHERE f.borrowRecord.book.category.assignedLibrarian.user.id = :librarianUserId")
    Page<Fine> findFinesForLibrarian(@Param("librarianUserId") Long librarianUserId, Pageable pageable);
    
    // Statistics queries
    @Query("SELECT COUNT(f) FROM Fine f WHERE f.status = :status")
    long countByStatus(@Param("status") FineStatus status);
    
    @Query("SELECT COUNT(f) FROM Fine f WHERE f.createdAt >= :since")
    long countFinesSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(f) FROM Fine f WHERE f.paidAt >= :since AND f.status = 'PAID'")
    long countPaidFinesSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(f) FROM Fine f WHERE f.waivedAt >= :since AND f.status = 'WAIVED'")
    long countWaivedFinesSince(@Param("since") LocalDateTime since);
    
    // Revenue from fines
    @Query("SELECT SUM(f.amount) FROM Fine f WHERE f.status = 'PAID'")
    BigDecimal getTotalRevenueFromFines();
    
    @Query("SELECT SUM(f.amount) FROM Fine f WHERE f.status = 'PAID' AND f.paidAt >= :since")
    BigDecimal getTotalRevenueFromFinesSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT SUM(f.amount) FROM Fine f WHERE f.status = 'PAID' AND f.paidAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalRevenueFromFinesBetween(@Param("startDate") LocalDateTime startDate, 
                                              @Param("endDate") LocalDateTime endDate);
    
    // Outstanding fines total
    @Query("SELECT SUM(f.amount) FROM Fine f WHERE f.status = 'PENDING'")
    BigDecimal getTotalOutstandingFines();
    
    @Query("SELECT COUNT(f) FROM Fine f WHERE f.status = 'PENDING'")
    long countOutstandingFines();
    
    // Monthly fine statistics
    @Query("SELECT YEAR(f.createdAt), MONTH(f.createdAt), COUNT(f), SUM(f.amount) FROM Fine f " +
           "WHERE f.createdAt >= :since " +
           "GROUP BY YEAR(f.createdAt), MONTH(f.createdAt) " +
           "ORDER BY YEAR(f.createdAt), MONTH(f.createdAt)")
    List<Object[]> getMonthlyFineStatistics(@Param("since") LocalDateTime since);
    
    @Query("SELECT YEAR(f.paidAt), MONTH(f.paidAt), COUNT(f), SUM(f.amount) FROM Fine f " +
           "WHERE f.status = 'PAID' AND f.paidAt >= :since " +
           "GROUP BY YEAR(f.paidAt), MONTH(f.paidAt) " +
           "ORDER BY YEAR(f.paidAt), MONTH(f.paidAt)")
    List<Object[]> getMonthlyPaidFineStatistics(@Param("since") LocalDateTime since);
    
    // Users with most fines
    @Query("SELECT f.reader, COUNT(f) as fineCount, SUM(f.amount) as totalAmount FROM Fine f " +
           "GROUP BY f.reader " +
           "ORDER BY fineCount DESC, totalAmount DESC")
    List<Object[]> findUsersWithMostFines(Pageable pageable);
    
    // Books generating most fines
    @Query("SELECT f.borrowRecord.book, COUNT(f) as fineCount, SUM(f.amount) as totalAmount FROM Fine f " +
           "GROUP BY f.borrowRecord.book " +
           "ORDER BY fineCount DESC, totalAmount DESC")
    List<Object[]> findBooksGeneratingMostFines(Pageable pageable);
    
    // Average fine amount
    @Query("SELECT AVG(f.amount) FROM Fine f WHERE f.status = :status")
    Double getAverageFineAmountByStatus(@Param("status") FineStatus status);
    
    @Query("SELECT AVG(f.overdueDays) FROM Fine f")
    Double getAverageOverdueDays();
    
    // Search queries
    @Query("SELECT f FROM Fine f WHERE " +
           "(LOWER(f.reader.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(f.reader.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(f.reader.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(f.borrowRecord.book.title) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND f.status = :status")
    Page<Fine> searchFinesByStatus(@Param("search") String search, 
                                  @Param("status") FineStatus status, 
                                  Pageable pageable);
    
    // Long overdue fines (potential write-offs)
    @Query("SELECT f FROM Fine f WHERE f.status = 'PENDING' AND f.createdAt < :threshold ORDER BY f.createdAt ASC")
    List<Fine> findLongOverdueFines(@Param("threshold") LocalDateTime threshold);
    
    // Waived fines by admin/librarian
    @Query("SELECT f FROM Fine f WHERE f.waivedBy.id = :userId AND f.status = 'WAIVED'")
    List<Fine> findFinesWaivedByUser(@Param("userId") Long userId);
    
    @Query("SELECT f FROM Fine f WHERE f.waivedBy.id = :userId AND f.status = 'WAIVED'")
    Page<Fine> findFinesWaivedByUser(@Param("userId") Long userId, Pageable pageable);
    
    // Fine payment success rate
    @Query("SELECT " +
           "(SELECT COUNT(f1) FROM Fine f1 WHERE f1.status = 'PAID' AND f1.createdAt >= :since) * 100.0 / " +
           "(SELECT COUNT(f2) FROM Fine f2 WHERE f2.createdAt >= :since) " +
           "FROM Fine f WHERE f.createdAt >= :since")
    Double getFinePaymentSuccessRateSince(@Param("since") LocalDateTime since);
}