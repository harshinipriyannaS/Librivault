package com.librivault.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.librivault.entity.enums.PaymentType;
import com.librivault.entity.enums.SubscriptionType;
import com.librivault.repository.BookRepository;
import com.librivault.repository.BorrowRecordRepository;
import com.librivault.repository.CategoryRepository;
import com.librivault.repository.FineRepository;
import com.librivault.repository.PaymentRepository;
import com.librivault.repository.SubscriptionRepository;
import com.librivault.repository.UserRepository;

@Service
public class AnalyticsService {
    
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private BorrowRecordRepository borrowRecordRepository;
    
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private FineRepository fineRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    // Dashboard Overview Statistics
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getDashboardOverview() {
        logger.info("Generating dashboard overview statistics");
        
        Map<String, Object> overview = new HashMap<>();
        
        // User Statistics
        overview.put("totalUsers", userRepository.countActiveUsers());
        overview.put("totalReaders", userRepository.countActiveUsersByRole(com.librivault.entity.enums.Role.READER));
        overview.put("totalLibrarians", userRepository.countActiveUsersByRole(com.librivault.entity.enums.Role.LIBRARIAN));
        
        // Book Statistics
        overview.put("totalBooks", bookRepository.countActiveBooks());
        overview.put("availableBooks", bookRepository.countAvailableBooks());
        overview.put("totalCategories", categoryRepository.countActiveCategories());
        
        // Borrowing Statistics
        overview.put("activeBorrows", borrowRecordRepository.countByStatus(com.librivault.entity.enums.BorrowStatus.ACTIVE));
        overview.put("overdueBooks", borrowRecordRepository.countOverdueRecords(LocalDateTime.now()));
        
        // Subscription Statistics
        overview.put("freeSubscriptions", subscriptionRepository.countActiveSubscriptionsByType(SubscriptionType.FREE));
        overview.put("premiumSubscriptions", subscriptionRepository.countActiveSubscriptionsByType(SubscriptionType.PREMIUM));
        
        // Financial Statistics
        BigDecimal totalRevenue = paymentRepository.getTotalRevenue();
        BigDecimal outstandingFines = fineRepository.getTotalOutstandingFines();
        overview.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        overview.put("outstandingFines", outstandingFines != null ? outstandingFines : BigDecimal.ZERO);
        
        return overview;
    }
    
    // User Analytics
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getUserAnalytics(int days) {
        logger.info("Generating user analytics for {} days", days);
        
        Map<String, Object> analytics = new HashMap<>();
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        
        // User registration trends
        List<Object[]> recentRegistrations = userRepository.findRecentlyRegisteredUsers(since)
                .stream()
                .limit(10)
                .map(user -> new Object[]{user.getId(), user.getFullName(), user.getEmail(), user.getCreatedAt()})
                .toList();
        
        analytics.put("recentRegistrations", recentRegistrations);
        analytics.put("totalActiveUsers", userRepository.countActiveUsers());
        
        // User activity
        List<Object[]> recentlyActive = userRepository.findRecentlyActiveUsers(since)
                .stream()
                .limit(10)
                .map(user -> new Object[]{user.getId(), user.getFullName(), user.getEmail(), user.getLastLogin()})
                .toList();
        
        analytics.put("recentlyActiveUsers", recentlyActive);
        
        return analytics;
    }
    
    // Subscription Analytics
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getSubscriptionAnalytics(int months) {
        logger.info("Generating subscription analytics for {} months", months);
        
        Map<String, Object> analytics = new HashMap<>();
        LocalDateTime since = LocalDateTime.now().minusMonths(months);
        
        // Subscription distribution
        analytics.put("freeSubscriptions", subscriptionRepository.countActiveSubscriptionsByType(SubscriptionType.FREE));
        analytics.put("premiumSubscriptions", subscriptionRepository.countActiveSubscriptionsByType(SubscriptionType.PREMIUM));
        
        // Monthly premium subscriptions
        List<Object[]> monthlyPremium = subscriptionRepository.getPremiumSubscriptionsByMonth(since);
        analytics.put("monthlyPremiumSubscriptions", monthlyPremium);
        
        // Monthly revenue from subscriptions
        List<Object[]> monthlyRevenue = subscriptionRepository.getMonthlyRevenueFromPremiumSubscriptions(since);
        analytics.put("monthlySubscriptionRevenue", monthlyRevenue);
        
        // Recent subscriptions
        List<Object[]> recentSubscriptions = subscriptionRepository.findRecentSubscriptions(since)
                .stream()
                .limit(10)
                .map(sub -> new Object[]{sub.getId(), sub.getUser().getFullName(), sub.getType(), sub.getCreatedAt()})
                .toList();
        
        analytics.put("recentSubscriptions", recentSubscriptions);
        
        return analytics;
    }
    
    // Book and Borrowing Analytics
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getBookAnalytics(int days) {
        logger.info("Generating book analytics for {} days", days);
        
        Map<String, Object> analytics = new HashMap<>();
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        
        // Book statistics
        analytics.put("totalBooks", bookRepository.countActiveBooks());
        analytics.put("availableBooks", bookRepository.countAvailableBooks());
        analytics.put("totalCopies", bookRepository.getTotalCopiesCount());
        analytics.put("availableCopies", bookRepository.getAvailableCopiesCount());
        
        // Most borrowed books
        List<Object[]> mostBorrowed = borrowRecordRepository.findMostBorrowedBooks(
                org.springframework.data.domain.PageRequest.of(0, 10));
        analytics.put("mostBorrowedBooks", mostBorrowed);
        
        // Recent book additions
        List<Object[]> recentBooks = bookRepository.findRecentlyAddedBooks(since)
                .stream()
                .limit(10)
                .map(book -> new Object[]{book.getId(), book.getTitle(), book.getAuthor(), book.getCreatedAt()})
                .toList();
        
        analytics.put("recentlyAddedBooks", recentBooks);
        
        // Borrowing statistics
        analytics.put("activeBorrows", borrowRecordRepository.countByStatus(com.librivault.entity.enums.BorrowStatus.ACTIVE));
        analytics.put("totalBorrows", borrowRecordRepository.countBorrowsSince(since));
        analytics.put("totalReturns", borrowRecordRepository.countReturnsSince(since));
        analytics.put("overdueBooks", borrowRecordRepository.countOverdueRecords(LocalDateTime.now()));
        
        return analytics;
    }
    
    // Financial Analytics
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getFinancialAnalytics(int months) {
        logger.info("Generating financial analytics for {} months", months);
        
        Map<String, Object> analytics = new HashMap<>();
        LocalDateTime since = LocalDateTime.now().minusMonths(months);
        
        // Revenue statistics
        BigDecimal totalRevenue = paymentRepository.getTotalRevenue();
        BigDecimal subscriptionRevenue = paymentRepository.getTotalRevenueByType(PaymentType.SUBSCRIPTION);
        BigDecimal fineRevenue = paymentRepository.getTotalRevenueByType(PaymentType.FINE);
        
        analytics.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        analytics.put("subscriptionRevenue", subscriptionRevenue != null ? subscriptionRevenue : BigDecimal.ZERO);
        analytics.put("fineRevenue", fineRevenue != null ? fineRevenue : BigDecimal.ZERO);
        
        // Monthly revenue breakdown
        List<Object[]> monthlyRevenue = paymentRepository.getMonthlyRevenue(since);
        analytics.put("monthlyRevenue", monthlyRevenue);
        
        List<Object[]> monthlySubscriptionRevenue = paymentRepository.getMonthlyRevenueByType(PaymentType.SUBSCRIPTION, since);
        analytics.put("monthlySubscriptionRevenue", monthlySubscriptionRevenue);
        
        List<Object[]> monthlyFineRevenue = paymentRepository.getMonthlyRevenueByType(PaymentType.FINE, since);
        analytics.put("monthlyFineRevenue", monthlyFineRevenue);
        
        // Payment statistics
        analytics.put("totalPayments", paymentRepository.countByStatus(com.librivault.entity.enums.PaymentStatus.COMPLETED));
        analytics.put("failedPayments", paymentRepository.countByStatus(com.librivault.entity.enums.PaymentStatus.FAILED));
        
        // Top paying users
        List<Object[]> topPayingUsers = paymentRepository.findTopPayingUsers(
                org.springframework.data.domain.PageRequest.of(0, 10));
        analytics.put("topPayingUsers", topPayingUsers);
        
        // Payment success rate
        Double successRate = paymentRepository.getPaymentSuccessRateSince(since);
        analytics.put("paymentSuccessRate", successRate != null ? successRate : 0.0);
        
        return analytics;
    }
    
    // Fine Analytics
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getFineAnalytics(int months) {
        logger.info("Generating fine analytics for {} months", months);
        
        Map<String, Object> analytics = new HashMap<>();
        LocalDateTime since = LocalDateTime.now().minusMonths(months);
        
        // Fine statistics
        BigDecimal totalOutstanding = fineRepository.getTotalOutstandingFines();
        BigDecimal totalCollected = fineRepository.getTotalRevenueFromFines();
        
        analytics.put("totalOutstandingFines", totalOutstanding != null ? totalOutstanding : BigDecimal.ZERO);
        analytics.put("totalCollectedFines", totalCollected != null ? totalCollected : BigDecimal.ZERO);
        analytics.put("pendingFines", fineRepository.countByStatus(com.librivault.entity.enums.FineStatus.PENDING));
        analytics.put("paidFines", fineRepository.countByStatus(com.librivault.entity.enums.FineStatus.PAID));
        
        // Monthly fine statistics
        List<Object[]> monthlyFineStats = fineRepository.getMonthlyFineStatistics(since);
        analytics.put("monthlyFineStatistics", monthlyFineStats);
        
        List<Object[]> monthlyPaidFines = fineRepository.getMonthlyPaidFineStatistics(since);
        analytics.put("monthlyPaidFineStatistics", monthlyPaidFines);
        
        // Users with most fines
        List<Object[]> usersWithMostFines = fineRepository.findUsersWithMostFines(
                org.springframework.data.domain.PageRequest.of(0, 10));
        analytics.put("usersWithMostFines", usersWithMostFines);
        
        // Books generating most fines
        List<Object[]> booksWithMostFines = fineRepository.findBooksGeneratingMostFines(
                org.springframework.data.domain.PageRequest.of(0, 10));
        analytics.put("booksGeneratingMostFines", booksWithMostFines);
        
        return analytics;
    }
    
    // Category Analytics
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getCategoryAnalytics() {
        logger.info("Generating category analytics");
        
        Map<String, Object> analytics = new HashMap<>();
        
        // Category statistics
        analytics.put("totalCategories", categoryRepository.countActiveCategories());
        analytics.put("categoriesWithLibrarian", categoryRepository.countCategoriesWithLibrarian());
        analytics.put("categoriesWithoutLibrarian", categoryRepository.countCategoriesWithoutLibrarian());
        
        // Categories with book counts
        List<Object[]> categoriesWithBookCount = categoryRepository.findCategoriesWithBookCount();
        analytics.put("categoriesWithBookCount", categoriesWithBookCount);
        
        // Most popular categories
        List<Object[]> mostPopularCategories = categoryRepository.findMostPopularCategories(
                org.springframework.data.domain.PageRequest.of(0, 10));
        analytics.put("mostPopularCategories", mostPopularCategories);
        
        return analytics;
    }
    
    // Activity Analytics
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getActivityAnalytics(int days) {
        logger.info("Generating activity analytics for {} days", days);
        
        Map<String, Object> analytics = new HashMap<>();
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        
        // Most active readers
        List<Object[]> mostActiveReaders = borrowRecordRepository.findMostActiveReaders(
                org.springframework.data.domain.PageRequest.of(0, 10));
        analytics.put("mostActiveReaders", mostActiveReaders);
        
        // Recent activity
        analytics.put("recentBorrows", borrowRecordRepository.countBorrowsSince(since));
        analytics.put("recentReturns", borrowRecordRepository.countReturnsSince(since));
        analytics.put("recentPayments", paymentRepository.countCompletedPaymentsSince(since));
        analytics.put("recentRegistrations", userRepository.findRecentlyRegisteredUsers(since).size());
        
        return analytics;
    }
    
    // Comprehensive Analytics Report
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getComprehensiveAnalytics() {
        logger.info("Generating comprehensive analytics report");
        
        Map<String, Object> report = new HashMap<>();
        
        // Include all analytics
        report.put("overview", getDashboardOverview());
        report.put("users", getUserAnalytics(30));
        report.put("subscriptions", getSubscriptionAnalytics(12));
        report.put("books", getBookAnalytics(30));
        report.put("financial", getFinancialAnalytics(12));
        report.put("fines", getFineAnalytics(12));
        report.put("categories", getCategoryAnalytics());
        report.put("activity", getActivityAnalytics(30));
        
        // Add timestamp
        report.put("generatedAt", LocalDateTime.now());
        
        return report;
    }
}