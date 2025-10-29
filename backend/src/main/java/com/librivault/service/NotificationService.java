package com.librivault.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.librivault.entity.Notification;
import com.librivault.entity.User;
import com.librivault.entity.enums.NotificationType;
import com.librivault.entity.enums.Role;
import com.librivault.repository.NotificationRepository;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private NotificationRepository notificationRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendWelcomeEmail(User user) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Welcome to LibriVault!");
            message.setText(String.format(
                    "Dear %s,\n\n"
                    + "Welcome to LibriVault Digital Library!\n\n"
                    + "Your account has been successfully created with the following details:\n"
                    + "Email: %s\n"
                    + "Role: %s\n\n"
                    + "You can now start browsing and borrowing books from our extensive collection.\n\n"
                    + "Best regards,\n"
                    + "LibriVault Team",
                    user.getFullName(),
                    user.getEmail(),
                    user.getRole()
            ));

            mailSender.send(message);
            logger.info("Welcome email sent successfully to: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send welcome email to: {}", user.getEmail(), e);
        }
    }

    @Async
    public void sendRoleChangeNotification(User user, Role oldRole, Role newRole) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("LibriVault - Role Updated");
            message.setText(String.format(
                    "Dear %s,\n\n"
                    + "Your role in LibriVault has been updated.\n\n"
                    + "Previous Role: %s\n"
                    + "New Role: %s\n\n"
                    + "This change may affect your access permissions and available features.\n\n"
                    + "If you have any questions, please contact our support team.\n\n"
                    + "Best regards,\n"
                    + "LibriVault Team",
                    user.getFullName(),
                    oldRole,
                    newRole
            ));

            mailSender.send(message);
            logger.info("Role change notification sent successfully to: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send role change notification to: {}", user.getEmail(), e);
        }
    }

    @Async
    public void sendPasswordResetEmail(User user, String resetToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("LibriVault - Password Reset Request");
            message.setText(String.format(
                    "Dear %s,\n\n"
                    + "You have requested to reset your password for your LibriVault account.\n\n"
                    + "Please use the following token to reset your password: %s\n\n"
                    + "This token will expire in 24 hours.\n\n"
                    + "If you did not request this password reset, please ignore this email.\n\n"
                    + "Best regards,\n"
                    + "LibriVault Team",
                    user.getFullName(),
                    resetToken
            ));

            mailSender.send(message);
            logger.info("Password reset email sent successfully to: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send password reset email to: {}", user.getEmail(), e);
        }
    }

    @Async
    public void sendBookDueReminder(User user, String bookTitle, int daysUntilDue) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());

            if (daysUntilDue == 0) {
                message.setSubject("LibriVault - Book Borrow Request Approved");
                message.setText(String.format(
                        "Dear %s,\n\n"
                        + "Great news! Your borrow request for \"%s\" has been approved.\n\n"
                        + "You can now access the full book content from your dashboard.\n\n"
                        + "Please remember to return the book by the due date to avoid any fines.\n\n"
                        + "Happy reading!\n\n"
                        + "Best regards,\n"
                        + "LibriVault Team",
                        user.getFullName(),
                        bookTitle
                ));
            } else {
                message.setSubject("LibriVault - Book Due Reminder");
                message.setText(String.format(
                        "Dear %s,\n\n"
                        + "This is a friendly reminder that your borrowed book \"%s\" is due in %d day(s).\n\n"
                        + "Please return the book on time to avoid any late fees.\n\n"
                        + "You can return the book from your dashboard or contact your librarian if you need assistance.\n\n"
                        + "Thank you for using LibriVault!\n\n"
                        + "Best regards,\n"
                        + "LibriVault Team",
                        user.getFullName(),
                        bookTitle,
                        daysUntilDue
                ));
            }

            mailSender.send(message);
            logger.info("Book due reminder sent successfully to: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send book due reminder to: {}", user.getEmail(), e);
        }
    }

    @Async
    public void sendOverdueNotification(User user, String bookTitle, int daysOverdue) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("LibriVault - Overdue Book Notice");
            message.setText(String.format(
                    "Dear %s,\n\n"
                    + "Your borrowed book \"%s\" is now %d day(s) overdue.\n\n"
                    + "Please return the book immediately to avoid additional late fees.\n\n"
                    + "Current late fee: Based on your subscription plan\n"
                    + "Daily fine rate: Continues to accrue until returned\n\n"
                    + "You can return the book from your dashboard or contact your librarian.\n\n"
                    + "Please note: Outstanding fines will prevent you from borrowing additional books.\n\n"
                    + "Best regards,\n"
                    + "LibriVault Team",
                    user.getFullName(),
                    bookTitle,
                    daysOverdue
            ));

            mailSender.send(message);
            logger.info("Overdue notification sent successfully to: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send overdue notification to: {}", user.getEmail(), e);
        }
    }

    @Async
    public void sendSubscriptionExpiryReminder(User user, int daysUntilExpiry) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("LibriVault - Subscription Expiry Reminder");
            message.setText(String.format(
                    "Dear %s,\n\n"
                    + "Your LibriVault subscription will expire in %d day(s).\n\n"
                    + "To continue enjoying unlimited access to our digital library:\n"
                    + "- Renew your current subscription\n"
                    + "- Upgrade to Premium for enhanced benefits\n"
                    + "- Or continue with our Free plan (limited access)\n\n"
                    + "Visit your dashboard to manage your subscription.\n\n"
                    + "Don't let your reading journey stop!\n\n"
                    + "Best regards,\n"
                    + "LibriVault Team",
                    user.getFullName(),
                    daysUntilExpiry
            ));

            mailSender.send(message);
            logger.info("Subscription expiry reminder sent successfully to: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send subscription expiry reminder to: {}", user.getEmail(), e);
        }
    }

    @Async
    public void sendPaymentConfirmation(User user, String paymentId, String amount) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("LibriVault - Payment Confirmation");
            message.setText(String.format(
                    "Dear %s,\n\n"
                    + "Thank you for your payment! Your transaction has been processed successfully.\n\n"
                    + "Payment Details:\n"
                    + "Transaction ID: %s\n"
                    + "Amount: ₹%s\n"
                    + "Date: %s\n\n"
                    + "Your account has been updated and you can now enjoy the benefits of your subscription.\n\n"
                    + "You can download your receipt from your dashboard or payment history.\n\n"
                    + "Thank you for choosing LibriVault!\n\n"
                    + "Best regards,\n"
                    + "LibriVault Team",
                    user.getFullName(),
                    paymentId,
                    amount,
                    java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))
            ));

            mailSender.send(message);
            logger.info("Payment confirmation sent successfully to: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send payment confirmation to: {}", user.getEmail(), e);
        }
    }

    // Dashboard Notification Methods
    @Transactional
    public Notification createDashboardNotification(User user, String title, String message, NotificationType type) {
        logger.info("Creating dashboard notification for user: {} - Type: {}", user.getEmail(), type);
        
        Notification notification = new Notification(user, title, message, type);
        return notificationRepository.save(notification);
    }
    
    @Transactional
    public Notification createDashboardNotification(User user, String title, String message, 
                                                   NotificationType type, Long referenceId, String referenceType) {
        logger.info("Creating dashboard notification with reference for user: {} - Type: {}", user.getEmail(), type);
        
        Notification notification = new Notification(user, title, message, type, referenceId, referenceType);
        return notificationRepository.save(notification);
    }
    
    public Page<Notification> getUserNotifications(User user, Pageable pageable) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }
    
    public Page<Notification> getUserUnreadNotifications(User user, Pageable pageable) {
        return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user, pageable);
    }
    
    public long getUnreadNotificationCount(User user) {
        return notificationRepository.countUnreadByUser(user);
    }
    
    @Transactional
    public void markNotificationAsRead(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied to notification");
        }
        
        if (!notification.getIsRead()) {
            notification.markAsRead();
            notificationRepository.save(notification);
        }
    }
    
    @Transactional
    public void markAllNotificationsAsRead(User user) {
        notificationRepository.markAllAsReadForUser(user, LocalDateTime.now());
    }
    
    // Enhanced notification methods with dashboard integration
    
    @Async
    @Transactional
    public void sendWelcomeNotification(User user) {
        // Send email
        sendWelcomeEmail(user);
        
        // Create dashboard notification
        createDashboardNotification(
            user,
            "Welcome to LibriVault!",
            "Your account has been successfully created. Start exploring our digital library collection.",
            NotificationType.WELCOME
        );
    }
    
    @Async
@Transactional
public void sendBorrowRequestApprovalNotification(User user, String bookTitle, Long borrowRequestId) {
        // Create dashboard notification
        createDashboardNotification(
            user,
            "Borrow Request Approved",
            String.format("Your request to borrow \"%s\" has been approved. You can now access the full book.", bookTitle),
            NotificationType.BORROW_REQUEST_APPROVED,
            borrowRequestId,
            "BORROW_REQUEST"
        );
        
        // Send email
        sendBookDueReminder(user, bookTitle, 0);
    }
    
    @Async
@Transactional
public void sendBorrowRequestDeclineNotification(User user, String bookTitle, String reason, Long borrowRequestId) {
        // Create dashboard notification
        createDashboardNotification(
            user,
            "Borrow Request Declined",
            String.format("Your request to borrow \"%s\" has been declined. Reason: %s", bookTitle, reason),
            NotificationType.BORROW_REQUEST_DECLINED,
            borrowRequestId,
            "BORROW_REQUEST"
        );
    }
    
    @Async
@Transactional
public void sendBookDueReminderNotification(User user, String bookTitle, int daysUntilDue, Long borrowRecordId) {
        // Create dashboard notification
        String message = daysUntilDue == 1 
            ? String.format("Your borrowed book \"%s\" is due tomorrow. Please return it on time.", bookTitle)
            : String.format("Your borrowed book \"%s\" is due in %d days. Please return it on time.", bookTitle, daysUntilDue);
            
        createDashboardNotification(
            user,
            "Book Due Reminder",
            message,
            NotificationType.BOOK_DUE_REMINDER,
            borrowRecordId,
            "BORROW_RECORD"
        );
        
        // Send email
        sendBookDueReminder(user, bookTitle, daysUntilDue);
    }
    
    @Async
@Transactional
public void sendOverdueBookNotification(User user, String bookTitle, int daysOverdue, Long borrowRecordId) {
        // Create dashboard notification
        createDashboardNotification(
            user,
            "Overdue Book Notice",
            String.format("Your book \"%s\" is %d day(s) overdue. Please return it immediately to avoid additional fines.", bookTitle, daysOverdue),
            NotificationType.BOOK_OVERDUE,
            borrowRecordId,
            "BORROW_RECORD"
        );
        
        // Send email
        sendOverdueNotification(user, bookTitle, daysOverdue);
    }
    
    @Async
@Transactional
public void sendPaymentSuccessNotification(User user, String paymentId, String amount, Long paymentEntityId) {
        // Create dashboard notification
        createDashboardNotification(
            user,
            "Payment Successful",
            String.format("Your payment of ₹%s has been processed successfully. Transaction ID: %s", amount, paymentId),
            NotificationType.PAYMENT_SUCCESS,
            paymentEntityId,
            "PAYMENT"
        );
        
        // Send email
        sendPaymentConfirmation(user, paymentId, amount);
    }
    
    @Async
@Transactional
public void sendFineGeneratedNotification(User user, String bookTitle, String fineAmount, Long fineId) {
        // Create dashboard notification
        createDashboardNotification(
            user,
            "Fine Generated",
            String.format("A fine of ₹%s has been generated for overdue book \"%s\". Please pay to continue borrowing.", fineAmount, bookTitle),
            NotificationType.FINE_GENERATED,
            fineId,
            "FINE"
        );
    }
    
    // Scheduled cleanup of old notifications
    @Scheduled(cron = "0 0 3 * * ?") // Run daily at 3 AM
@Transactional
public void cleanupOldNotifications() {
        logger.info("Cleaning up old notifications");
        
        // Delete read notifications older than 30 days
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        int deletedCount = notificationRepository.deleteOldReadNotifications(null, cutoffDate);
        
        logger.info("Deleted {} old read notifications", deletedCount);
    }
}
