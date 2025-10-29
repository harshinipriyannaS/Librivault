package com.librivault.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.librivault.dto.borrow.BorrowRecordResponse;
import com.librivault.dto.borrow.BorrowRequestResponse;
import com.librivault.dto.borrow.FineResponse;
import com.librivault.entity.Book;
import com.librivault.entity.BorrowRecord;
import com.librivault.entity.BorrowRequest;
import com.librivault.entity.Fine;
import com.librivault.entity.Librarian;
import com.librivault.entity.Subscription;
import com.librivault.entity.User;
import com.librivault.entity.enums.BorrowStatus;
import com.librivault.entity.enums.RequestStatus;
import com.librivault.entity.enums.Role;
import com.librivault.repository.BookRepository;
import com.librivault.repository.BorrowRecordRepository;
import com.librivault.repository.BorrowRequestRepository;
import com.librivault.repository.FineRepository;
import com.librivault.repository.LibrarianRepository;
import com.librivault.repository.SubscriptionRepository;
import com.librivault.repository.UserRepository;
import com.librivault.security.UserPrincipal;

@Service
public class BorrowingService {

    private static final Logger logger = LoggerFactory.getLogger(BorrowingService.class);

    @Autowired
    private BorrowRequestRepository borrowRequestRepository;

    @Autowired
    private BorrowRecordRepository borrowRecordRepository;

    @Autowired
    private FineRepository fineRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private LibrarianRepository librarianRepository;

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Value("${app.credits.max-limit}")
    private Integer maxCreditLimit;

    @Value("${app.credits.early-return-multiplier}")
    private Integer earlyReturnMultiplier;

    // Borrow Request Management
    @PreAuthorize("hasRole('READER')")
    @Transactional
    public BorrowRequestResponse createBorrowRequest(Long bookId, UserPrincipal currentUser) {
        logger.info("Creating borrow request for book: {} by user: {}", bookId, currentUser.getId());

        User reader = userService.getUserEntityById(currentUser.getId());
        Book book = bookService.getBookEntityById(bookId);

        // Check if user has outstanding fines
        if (fineRepository.hasOutstandingFines(reader.getId())) {
            throw new RuntimeException("Cannot borrow books with outstanding fines. Please pay your fines first.");
        }

        // Check if book is available
        if (!book.isAvailable()) {
            throw new RuntimeException("Book is not available for borrowing");
        }

        // Check subscription limits (staff members have unlimited access)
        if (reader.getRole() != Role.ADMIN && reader.getRole() != Role.LIBRARIAN) {
            Subscription subscription = subscriptionRepository.findByUserId(reader.getId())
                    .orElseThrow(() -> new RuntimeException("No active subscription found"));

            long activeBorrows = borrowRecordRepository.countActiveRecordsByReader(reader.getId());
            long availableSlots = subscription.getBookLimit() + reader.getReaderCredits() - activeBorrows;

            if (availableSlots <= 0) {
                throw new RuntimeException("Borrowing limit reached. Return books or upgrade subscription to borrow more.");
            }
        }

        // Check if user already has a pending request for this book
        if (borrowRequestRepository.existsByReaderAndBookAndStatus(reader, book, RequestStatus.PENDING)) {
            throw new RuntimeException("You already have a pending request for this book");
        }

        // Create borrow request
        BorrowRequest borrowRequest = new BorrowRequest(reader, book);
        BorrowRequest savedRequest = borrowRequestRepository.save(borrowRequest);

        logger.info("Borrow request created successfully: {}", savedRequest.getId());
        return convertToBorrowRequestResponse(savedRequest);
    }

    @PreAuthorize("hasRole('LIBRARIAN')")
    @Transactional
    public BorrowRequestResponse approveBorrowRequest(Long requestId, String notes, UserPrincipal currentUser) {
        logger.info("Approving borrow request: {} by librarian: {}", requestId, currentUser.getId());

        BorrowRequest borrowRequest = borrowRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Borrow request not found with id: " + requestId));

        User librarian = userService.getUserEntityById(currentUser.getId());

        // Check if librarian is assigned to this book's category
        Librarian librarianEntity = librarianRepository.findByUser(librarian)
                .orElseThrow(() -> new RuntimeException("Librarian profile not found"));

        if (!borrowRequest.getBook().getCategory().equals(librarianEntity.getAssignedCategory())) {
            throw new RuntimeException("You can only approve requests for books in your assigned category");
        }

        if (borrowRequest.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Request has already been reviewed");
        }

        // Check if book is still available
        if (!borrowRequest.getBook().isAvailable()) {
            throw new RuntimeException("Book is no longer available");
        }

        // Approve the request
        borrowRequest.approve(librarian, notes);
        BorrowRequest savedRequest = borrowRequestRepository.save(borrowRequest);

        // Create borrow record
        createBorrowRecord(borrowRequest.getReader(), borrowRequest.getBook());

        // Send notification to reader
        notificationService.sendBookDueReminder(
                borrowRequest.getReader(),
                borrowRequest.getBook().getTitle(),
                0 // Approved notification
        );

        logger.info("Borrow request approved successfully: {}", requestId);
        return convertToBorrowRequestResponse(savedRequest);
    }

    @PreAuthorize("hasRole('LIBRARIAN')")
    @Transactional
    public BorrowRequestResponse declineBorrowRequest(Long requestId, String notes, UserPrincipal currentUser) {
        logger.info("Declining borrow request: {} by librarian: {}", requestId, currentUser.getId());

        BorrowRequest borrowRequest = borrowRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Borrow request not found with id: " + requestId));

        User librarian = userService.getUserEntityById(currentUser.getId());

        // Check if librarian is assigned to this book's category
        Librarian librarianEntity = librarianRepository.findByUser(librarian)
                .orElseThrow(() -> new RuntimeException("Librarian profile not found"));

        if (!borrowRequest.getBook().getCategory().equals(librarianEntity.getAssignedCategory())) {
            throw new RuntimeException("You can only decline requests for books in your assigned category");
        }

        if (borrowRequest.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Request has already been reviewed");
        }

        // Decline the request
        borrowRequest.decline(librarian, notes);
        BorrowRequest savedRequest = borrowRequestRepository.save(borrowRequest);

        logger.info("Borrow request declined successfully: {}", requestId);
        return convertToBorrowRequestResponse(savedRequest);
    }

    @Transactional
    private void createBorrowRecord(User reader, Book book) {
        logger.info("Creating borrow record for user: {} and book: {}", reader.getId(), book.getId());

        // Calculate due date and credit usage (staff get premium terms)
        LocalDateTime dueDate;
        boolean usedCredit = false;

        if (reader.getRole() == Role.ADMIN || reader.getRole() == Role.LIBRARIAN) {
            // Staff members get extended borrow period (30 days) and no credit usage
            dueDate = LocalDateTime.now().plusDays(30);
            usedCredit = false;
        } else {
            Subscription subscription = subscriptionRepository.findByUserId(reader.getId())
                    .orElseThrow(() -> new RuntimeException("No active subscription found"));

            // Calculate due date
            dueDate = LocalDateTime.now().plusDays(subscription.getBorrowDurationDays());

            // Check if using credit
            long activeBorrows = borrowRecordRepository.countActiveRecordsByReader(reader.getId());
            usedCredit = activeBorrows >= subscription.getBookLimit();
        }

        // Create borrow record
        BorrowRecord borrowRecord = new BorrowRecord(reader, book, dueDate, usedCredit);
        borrowRecordRepository.save(borrowRecord);

        // Decrease available copies
        bookService.decreaseAvailableCopies(book.getId());

        // Deduct credit if used
        if (usedCredit) {
            userService.deductCreditsFromUser(reader.getId(), 1);
        }

        logger.info("Borrow record created successfully");
    }

    // Book Return Management
    @PreAuthorize("hasRole('READER')")
    @Transactional
    public BorrowRecordResponse returnBook(Long borrowRecordId, UserPrincipal currentUser) {
        logger.info("Returning book for borrow record: {} by user: {}", borrowRecordId, currentUser.getId());

        BorrowRecord borrowRecord = borrowRecordRepository.findById(borrowRecordId)
                .orElseThrow(() -> new RuntimeException("Borrow record not found with id: " + borrowRecordId));

        // Check if user owns this borrow record
        if (!borrowRecord.getReader().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only return your own borrowed books");
        }

        if (borrowRecord.getStatus() != BorrowStatus.ACTIVE) {
            throw new RuntimeException("Book has already been returned");
        }

        // Return the book
        borrowRecord.returnBook();

        // Calculate credits for early return
        long daysReturnedEarly = borrowRecord.getDaysReturnedEarly();
        if (daysReturnedEarly > 0) {
            int creditsEarned = Math.min((int) daysReturnedEarly * earlyReturnMultiplier, maxCreditLimit);
            borrowRecord.setCreditsEarned(creditsEarned);

            // Add credits to user (but don't exceed max limit)
            User reader = borrowRecord.getReader();
            int newCredits = Math.min(reader.getReaderCredits() + creditsEarned, maxCreditLimit);
            reader.setReaderCredits(newCredits);
            userRepository.save(reader);
        }

        BorrowRecord savedRecord = borrowRecordRepository.save(borrowRecord);

        // Increase available copies
        bookService.increaseAvailableCopies(borrowRecord.getBook().getId());

        // Process any overdue fines if applicable
        if (borrowRecord.isOverdue()) {
            processOverdueFine(borrowRecord);
        }

        logger.info("Book returned successfully: {}", borrowRecordId);
        return convertToBorrowRecordResponse(savedRecord);
    }

    // Fine Management
    @Transactional
    private void processOverdueFine(BorrowRecord borrowRecord) {
        logger.info("Processing overdue fine for borrow record: {}", borrowRecord.getId());

        long overdueDays = borrowRecord.getDaysOverdue();
        if (overdueDays <= 0) {
            return;
        }

        // Calculate fine amount (staff members don't get fines)
        BigDecimal fineAmount;
        User reader = borrowRecord.getReader();

        if (reader.getRole() == Role.ADMIN || reader.getRole() == Role.LIBRARIAN) {
            // Staff members don't get fines
            fineAmount = BigDecimal.ZERO;
        } else {
            Subscription subscription = subscriptionRepository.findByUserId(reader.getId())
                    .orElseThrow(() -> new RuntimeException("No active subscription found"));
            fineAmount = subscription.getDailyFineAmount().multiply(BigDecimal.valueOf(overdueDays));
        }

        Fine fine = new Fine(borrowRecord.getReader(), borrowRecord, fineAmount, (int) overdueDays);
        fineRepository.save(fine);

        logger.info("Fine created: {} for {} days overdue", fineAmount, overdueDays);
    }

    @Scheduled(cron = "0 0 1 * * ?") // Run daily at 1 AM
    @Transactional
    public void processOverdueBooks() {
        logger.info("Processing overdue books");

        List<BorrowRecord> overdueRecords = borrowRecordRepository.findOverdueRecords(LocalDateTime.now());

        for (BorrowRecord record : overdueRecords) {
            if (record.getStatus() == BorrowStatus.ACTIVE) {
                record.markAsOverdue();
                borrowRecordRepository.save(record);

                // Create or update fine
                processOverdueFine(record);

                // Send overdue notification
                notificationService.sendOverdueNotification(
                        record.getReader(),
                        record.getBook().getTitle(),
                        (int) record.getDaysOverdue()
                );
            }
        }

        logger.info("Processed {} overdue books", overdueRecords.size());
    }

    @Scheduled(cron = "0 0 9 * * ?") // Run daily at 9 AM
    public void sendDueReminders() {
        logger.info("Sending due date reminders");

        LocalDateTime threeDaysFromNow = LocalDateTime.now().plusDays(3);
        LocalDateTime oneDayFromNow = LocalDateTime.now().plusDays(1);

        // 3-day reminders
        List<BorrowRecord> recordsDueSoon = borrowRecordRepository.findRecordsDueBetween(
                threeDaysFromNow.minusHours(1), threeDaysFromNow.plusHours(1));

        for (BorrowRecord record : recordsDueSoon) {
            notificationService.sendBookDueReminder(
                    record.getReader(),
                    record.getBook().getTitle(),
                    3
            );
        }

        // 1-day reminders
        recordsDueSoon = borrowRecordRepository.findRecordsDueBetween(
                oneDayFromNow.minusHours(1), oneDayFromNow.plusHours(1));

        for (BorrowRecord record : recordsDueSoon) {
            notificationService.sendBookDueReminder(
                    record.getReader(),
                    record.getBook().getTitle(),
                    1
            );
        }

        logger.info("Sent due date reminders");
    }

    // Query Methods
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    public Page<BorrowRequestResponse> getUserBorrowRequests(Long userId, Pageable pageable) {
        logger.info("Fetching borrow requests for user: {}", userId);
        User user = userService.getUserEntityById(userId);
        return borrowRequestRepository.findByReader(user, pageable)
                .map(this::convertToBorrowRequestResponse);
    }

    @PreAuthorize("hasRole('LIBRARIAN')")
    public Page<BorrowRequestResponse> getPendingRequestsForLibrarian(UserPrincipal currentUser, Pageable pageable) {
        logger.info("Fetching pending requests for librarian: {}", currentUser.getId());
        return borrowRequestRepository.findPendingRequestsForLibrarian(currentUser.getId(), pageable)
                .map(this::convertToBorrowRequestResponse);
    }

    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    public Page<BorrowRecordResponse> getUserBorrowHistory(Long userId, Pageable pageable) {
        logger.info("Fetching borrow history for user: {}", userId);
        return borrowRecordRepository.findReaderBorrowHistory(userId, pageable)
                .map(this::convertToBorrowRecordResponse);
    }

    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    public List<BorrowRecordResponse> getUserActiveBorrows(Long userId) {
        logger.info("Fetching active borrows for user: {}", userId);
        return borrowRecordRepository.findActiveRecordsByReader(userId)
                .stream()
                .map(this::convertToBorrowRecordResponse)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public Page<BorrowRecordResponse> getOverdueRecords(Pageable pageable) {
        logger.info("Fetching overdue records");
        return borrowRecordRepository.findOverdueRecords(LocalDateTime.now(), pageable)
                .map(this::convertToBorrowRecordResponse);
    }

    @PreAuthorize("hasRole('LIBRARIAN')")
    public Page<BorrowRecordResponse> getOverdueRecordsForLibrarian(UserPrincipal currentUser, Pageable pageable) {
        logger.info("Fetching overdue records for librarian: {}", currentUser.getId());
        return borrowRecordRepository.findOverdueRecordsForLibrarian(currentUser.getId(), LocalDateTime.now(), pageable)
                .map(this::convertToBorrowRecordResponse);
    }

    // Fine Management
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    public List<FineResponse> getUserOutstandingFines(Long userId) {
        logger.info("Fetching outstanding fines for user: {}", userId);
        return fineRepository.findOutstandingFinesByReader(userId)
                .stream()
                .map(this::convertToFineResponse)
                .collect(Collectors.toList());
    }

    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    public BigDecimal getUserTotalOutstandingFines(Long userId) {
        logger.info("Calculating total outstanding fines for user: {}", userId);
        BigDecimal total = fineRepository.getTotalOutstandingFinesByReader(userId);
        return total != null ? total : BigDecimal.ZERO;
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Transactional
    public FineResponse waiveFine(Long fineId, String reason, UserPrincipal currentUser) {
        logger.info("Waiving fine: {} by user: {}", fineId, currentUser.getId());

        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new RuntimeException("Fine not found with id: " + fineId));

        User waivedBy = userService.getUserEntityById(currentUser.getId());
        fine.waive(waivedBy, reason);

        Fine savedFine = fineRepository.save(fine);
        logger.info("Fine waived successfully: {}", fineId);

        return convertToFineResponse(savedFine);
    }

    // Statistics
    @PreAuthorize("hasRole('ADMIN')")
    public long getTotalActiveBorrows() {
        return borrowRecordRepository.countByStatus(BorrowStatus.ACTIVE);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public long getTotalOverdueBooks() {
        return borrowRecordRepository.countOverdueRecords(LocalDateTime.now());
    }

    @PreAuthorize("hasRole('ADMIN')")
    public BigDecimal getTotalOutstandingFines() {
        BigDecimal total = fineRepository.getTotalOutstandingFines();
        return total != null ? total : BigDecimal.ZERO;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<Object[]> getMostBorrowedBooks(int limit) {
        return borrowRecordRepository.findMostBorrowedBooks(
                org.springframework.data.domain.PageRequest.of(0, limit));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<Object[]> getMostActiveReaders(int limit) {
        return borrowRecordRepository.findMostActiveReaders(
                org.springframework.data.domain.PageRequest.of(0, limit));
    }

    // Helper methods
    private BorrowRequestResponse convertToBorrowRequestResponse(BorrowRequest request) {
        String reviewedByName = null;
        if (request.getReviewedBy() != null) {
            reviewedByName = request.getReviewedBy().getFullName();
        }

        return new BorrowRequestResponse(
                request.getId(),
                request.getReader().getId(),
                request.getReader().getFullName(),
                request.getReader().getEmail(),
                request.getBook().getId(),
                request.getBook().getTitle(),
                request.getBook().getAuthor(),
                request.getBook().getCategory().getName(),
                request.getStatus(),
                request.getRequestedAt(),
                request.getReviewedAt(),
                reviewedByName,
                request.getReviewNotes()
        );
    }

    private BorrowRecordResponse convertToBorrowRecordResponse(BorrowRecord record) {
        return new BorrowRecordResponse(
                record.getId(),
                record.getReader().getId(),
                record.getReader().getFullName(),
                record.getReader().getEmail(),
                record.getBook().getId(),
                record.getBook().getTitle(),
                record.getBook().getAuthor(),
                record.getBook().getCategory().getName(),
                record.getBorrowedAt(),
                record.getDueDate(),
                record.getReturnedAt(),
                record.getStatus(),
                record.getUsedCredit(),
                record.getCreditsEarned()
        );
    }

    private FineResponse convertToFineResponse(Fine fine) {
        String waivedByName = null;
        if (fine.getWaivedBy() != null) {
            waivedByName = fine.getWaivedBy().getFullName();
        }

        return new FineResponse(
                fine.getId(),
                fine.getReader().getId(),
                fine.getReader().getFullName(),
                fine.getReader().getEmail(),
                fine.getBorrowRecord().getId(),
                fine.getBorrowRecord().getBook().getTitle(),
                fine.getBorrowRecord().getBook().getAuthor(),
                fine.getAmount(),
                fine.getOverdueDays(),
                fine.getStatus(),
                fine.getDescription(),
                fine.getCreatedAt(),
                fine.getPaidAt(),
                fine.getWaivedAt(),
                waivedByName,
                fine.getWaiverReason()
        );
    }
}
