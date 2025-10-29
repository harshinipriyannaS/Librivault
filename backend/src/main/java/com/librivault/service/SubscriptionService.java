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

import com.librivault.dto.subscription.SubscriptionResponse;
import com.librivault.entity.Subscription;
import com.librivault.entity.User;
import com.librivault.entity.enums.Role;
import com.librivault.entity.enums.SubscriptionType;
import com.librivault.repository.SubscriptionRepository;
import com.librivault.repository.UserRepository;

@Service
public class SubscriptionService {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionService.class);

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Value("${app.subscription.free.book-limit}")
    private Integer freeBookLimit;

    @Value("${app.subscription.free.duration-days}")
    private Integer freeDurationDays;

    @Value("${app.subscription.free.daily-fine}")
    private BigDecimal freeDailyFine;

    @Value("${app.subscription.premium.book-limit}")
    private Integer premiumBookLimit;

    @Value("${app.subscription.premium.duration-days}")
    private Integer premiumDurationDays;

    @Value("${app.subscription.premium.daily-fine}")
    private BigDecimal premiumDailyFine;

    @Value("${app.subscription.premium.price}")
    private BigDecimal premiumPrice;

    // User subscription management
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    public SubscriptionResponse getUserSubscription(Long userId) {
        logger.info("Fetching subscription for user: {}", userId);

        // Check if user is admin or librarian - they don't have subscriptions
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (user.getRole() == Role.ADMIN || user.getRole() == Role.LIBRARIAN) {
            // Return a special response indicating no subscription needed for staff
            return createStaffSubscriptionResponse(user);
        }

        Subscription subscription = subscriptionRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("No subscription found for user: " + userId));

        return convertToSubscriptionResponse(subscription);
    }

    @PreAuthorize("#userId == authentication.principal.id")
    @Transactional
    public SubscriptionResponse upgradeToPremium(Long userId) {
        logger.info("Upgrading user {} to premium subscription", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Staff members cannot upgrade subscriptions
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.LIBRARIAN) {
            throw new RuntimeException("Staff members do not need subscription upgrades");
        }

        Subscription currentSubscription = subscriptionRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("No subscription found for user: " + userId));

        if (currentSubscription.getType() == SubscriptionType.PREMIUM && currentSubscription.getActive()) {
            throw new RuntimeException("User already has an active premium subscription");
        }

        // Deactivate current subscription
        currentSubscription.setActive(false);
        subscriptionRepository.save(currentSubscription);

        // Create new premium subscription
        Subscription premiumSubscription = new Subscription();
        premiumSubscription.setUser(user);
        premiumSubscription.setType(SubscriptionType.PREMIUM);
        premiumSubscription.setStartDate(LocalDateTime.now());
        premiumSubscription.setEndDate(LocalDateTime.now().plusDays(premiumDurationDays));
        premiumSubscription.setBookLimit(premiumBookLimit);
        premiumSubscription.setBorrowDurationDays(premiumDurationDays);
        premiumSubscription.setDailyFineAmount(premiumDailyFine);
        premiumSubscription.setActive(true);

        Subscription savedSubscription = subscriptionRepository.save(premiumSubscription);
        user.setSubscription(savedSubscription);
        userRepository.save(user);

        logger.info("User {} upgraded to premium subscription successfully", userId);
        return convertToSubscriptionResponse(savedSubscription);
    }

    @Transactional
    public void createDefaultFreeSubscription(User user) {
        logger.info("Creating default free subscription for user: {}", user.getId());

        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setType(SubscriptionType.FREE);
        subscription.setStartDate(LocalDateTime.now());
        subscription.setEndDate(LocalDateTime.now().plusDays(freeDurationDays));
        subscription.setBookLimit(freeBookLimit);
        subscription.setBorrowDurationDays(freeDurationDays);
        subscription.setDailyFineAmount(freeDailyFine);
        subscription.setActive(true);

        Subscription savedSubscription = subscriptionRepository.save(subscription);
        user.setSubscription(savedSubscription);

        logger.info("Default free subscription created for user: {}", user.getId());
    }

    @Transactional
    public void renewSubscription(Long subscriptionId) {
        logger.info("Renewing subscription: {}", subscriptionId);

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found with id: " + subscriptionId));

        if (subscription.getType() == SubscriptionType.FREE) {
            // Renew free subscription for another period
            subscription.setEndDate(LocalDateTime.now().plusDays(freeDurationDays));
        } else {
            // Renew premium subscription for another period
            subscription.setEndDate(LocalDateTime.now().plusDays(premiumDurationDays));
        }

        subscription.setActive(true);
        subscriptionRepository.save(subscription);

        logger.info("Subscription renewed successfully: {}", subscriptionId);
    }

    // Admin subscription management
    @PreAuthorize("hasRole('ADMIN')")
    public Page<SubscriptionResponse> getAllSubscriptions(Pageable pageable) {
        logger.info("Fetching all subscriptions with pagination");
        return subscriptionRepository.findAll(pageable)
                .map(this::convertToSubscriptionResponse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<SubscriptionResponse> getSubscriptionsByType(SubscriptionType type, Pageable pageable) {
        logger.info("Fetching subscriptions by type: {}", type);
        return subscriptionRepository.findByTypeAndActiveTrue(type, pageable)
                .map(this::convertToSubscriptionResponse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<SubscriptionResponse> searchSubscriptions(String search, Pageable pageable) {
        logger.info("Searching subscriptions with query: {}", search);
        return subscriptionRepository.searchActiveSubscriptions(search, pageable)
                .map(this::convertToSubscriptionResponse);
    }

    // Subscription expiry management
    @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
    @Transactional
    public void processExpiredSubscriptions() {
        logger.info("Processing expired subscriptions");

        List<Subscription> expiredSubscriptions = subscriptionRepository.findExpiredSubscriptions(LocalDateTime.now());

        for (Subscription subscription : expiredSubscriptions) {
            if (subscription.getType() == SubscriptionType.PREMIUM) {
                // Downgrade to free subscription
                downgradeToFreeSubscription(subscription.getUser());
            } else {
                // Renew free subscription automatically
                renewSubscription(subscription.getId());
            }
        }

        logger.info("Processed {} expired subscriptions", expiredSubscriptions.size());
    }

    @Scheduled(cron = "0 0 9 * * ?") // Run daily at 9 AM
    public void sendExpiryReminders() {
        logger.info("Sending subscription expiry reminders");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysFromNow = now.plusDays(7);
        LocalDateTime threeDaysFromNow = now.plusDays(3);

        // 7-day reminders
        List<Subscription> subscriptionsExpiringSoon = subscriptionRepository
                .findSubscriptionsExpiringBetween(sevenDaysFromNow.minusHours(1), sevenDaysFromNow.plusHours(1));

        for (Subscription subscription : subscriptionsExpiringSoon) {
            notificationService.sendSubscriptionExpiryReminder(subscription.getUser(), 7);
        }

        // 3-day reminders
        subscriptionsExpiringSoon = subscriptionRepository
                .findSubscriptionsExpiringBetween(threeDaysFromNow.minusHours(1), threeDaysFromNow.plusHours(1));

        for (Subscription subscription : subscriptionsExpiringSoon) {
            notificationService.sendSubscriptionExpiryReminder(subscription.getUser(), 3);
        }

        logger.info("Sent subscription expiry reminders");
    }

    @Transactional
    private void downgradeToFreeSubscription(User user) {
        logger.info("Downgrading user {} to free subscription", user.getId());

        // Deactivate current subscription
        Subscription currentSubscription = user.getSubscription();
        currentSubscription.setActive(false);
        subscriptionRepository.save(currentSubscription);

        // Create new free subscription
        createDefaultFreeSubscription(user);

        logger.info("User {} downgraded to free subscription", user.getId());
    }

    // Statistics and reporting
    @PreAuthorize("hasRole('ADMIN')")
    public long getTotalActiveSubscriptions() {
        return subscriptionRepository.countActiveSubscriptions();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public long getActiveSubscriptionsByType(SubscriptionType type) {
        return subscriptionRepository.countActiveSubscriptionsByType(type);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<SubscriptionResponse> getRecentSubscriptions(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return subscriptionRepository.findRecentSubscriptions(since)
                .stream()
                .map(this::convertToSubscriptionResponse)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Double getTotalRevenueFromPremiumSubscriptions(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return subscriptionRepository.getTotalRevenueFromPremiumSubscriptionsSince(since);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<Object[]> getMonthlyPremiumSubscriptions(int months) {
        LocalDateTime since = LocalDateTime.now().minusMonths(months);
        return subscriptionRepository.getPremiumSubscriptionsByMonth(since);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<Object[]> getMonthlyRevenueFromPremiumSubscriptions(int months) {
        LocalDateTime since = LocalDateTime.now().minusMonths(months);
        return subscriptionRepository.getMonthlyRevenueFromPremiumSubscriptions(since);
    }

    // Subscription plans information
    public List<SubscriptionPlan> getAvailableSubscriptionPlans() {
        return List.of(
                new SubscriptionPlan(
                        SubscriptionType.FREE,
                        "Free Plan",
                        "Basic access to our library",
                        BigDecimal.ZERO,
                        freeBookLimit,
                        freeDurationDays,
                        freeDailyFine
                ),
                new SubscriptionPlan(
                        SubscriptionType.PREMIUM,
                        "Premium Plan",
                        "Unlimited access with premium benefits",
                        premiumPrice,
                        premiumBookLimit,
                        premiumDurationDays,
                        premiumDailyFine
                )
        );
    }

    // Helper methods
    private SubscriptionResponse convertToSubscriptionResponse(Subscription subscription) {
        return new SubscriptionResponse(
                subscription.getId(),
                subscription.getUser().getId(),
                subscription.getType(),
                subscription.getStartDate(),
                subscription.getEndDate(),
                subscription.getBookLimit(),
                subscription.getBorrowDurationDays(),
                subscription.getDailyFineAmount(),
                subscription.getActive(),
                subscription.getCreatedAt()
        );
    }

    private SubscriptionResponse createStaffSubscriptionResponse(User user) {
        // Create a special subscription response for staff members (admin/librarian)
        // They have unlimited access without subscription restrictions
        return new SubscriptionResponse(
                null, // No subscription ID
                user.getId(),
                SubscriptionType.PREMIUM, // Staff get premium-level access
                LocalDateTime.now(), // Current date as start
                LocalDateTime.now().plusYears(100), // Far future end date
                -1, // Unlimited books
                365, // Long borrow duration
                BigDecimal.ZERO, // No fines for staff
                true, // Always active
                LocalDateTime.now() // Current timestamp
        );
    }

    public Subscription getSubscriptionEntityById(Long subscriptionId) {
        return subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found with id: " + subscriptionId));
    }

    public Subscription getUserSubscriptionEntity(Long userId) {
        // Check if user is admin or librarian - they don't have subscriptions
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (user.getRole() == Role.ADMIN || user.getRole() == Role.LIBRARIAN) {
            // Staff members don't have subscription entities, return null or handle appropriately
            return null;
        }

        return subscriptionRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("No subscription found for user: " + userId));
    }

    // Inner class for subscription plan information
    public static class SubscriptionPlan {

        private final SubscriptionType type;
        private final String name;
        private final String description;
        private final BigDecimal price;
        private final Integer bookLimit;
        private final Integer durationDays;
        private final BigDecimal dailyFineAmount;

        public SubscriptionPlan(SubscriptionType type, String name, String description,
                BigDecimal price, Integer bookLimit, Integer durationDays,
                BigDecimal dailyFineAmount) {
            this.type = type;
            this.name = name;
            this.description = description;
            this.price = price;
            this.bookLimit = bookLimit;
            this.durationDays = durationDays;
            this.dailyFineAmount = dailyFineAmount;
        }

        // Getters
        public SubscriptionType getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public Integer getBookLimit() {
            return bookLimit;
        }

        public Integer getDurationDays() {
            return durationDays;
        }

        public BigDecimal getDailyFineAmount() {
            return dailyFineAmount;
        }
    }
}
