package com.librivault.entity;

import com.librivault.entity.enums.SubscriptionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "subscriptions")
public class Subscription {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionType type = SubscriptionType.FREE;
    
    @Column(name = "start_date", nullable = false)
    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;
    
    @Column(name = "end_date", nullable = false)
    @NotNull(message = "End date is required")
    private LocalDateTime endDate;
    
    @Column(name = "book_limit", nullable = false)
    @NotNull(message = "Book limit is required")
    @Positive(message = "Book limit must be positive")
    private Integer bookLimit;
    
    @Column(name = "borrow_duration_days", nullable = false)
    @NotNull(message = "Borrow duration is required")
    @Positive(message = "Borrow duration must be positive")
    private Integer borrowDurationDays;
    
    @Column(name = "daily_fine_amount", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Daily fine amount is required")
    private BigDecimal dailyFineAmount;
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Payment> payments = new ArrayList<>();
    
    // Constructors
    public Subscription() {}
    
    public Subscription(User user, SubscriptionType type, LocalDateTime startDate, 
                       LocalDateTime endDate, Integer bookLimit, Integer borrowDurationDays, 
                       BigDecimal dailyFineAmount) {
        this.user = user;
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
        this.bookLimit = bookLimit;
        this.borrowDurationDays = borrowDurationDays;
        this.dailyFineAmount = dailyFineAmount;
        this.active = true;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public SubscriptionType getType() {
        return type;
    }
    
    public void setType(SubscriptionType type) {
        this.type = type;
    }
    
    public LocalDateTime getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }
    
    public LocalDateTime getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
    
    public Integer getBookLimit() {
        return bookLimit;
    }
    
    public void setBookLimit(Integer bookLimit) {
        this.bookLimit = bookLimit;
    }
    
    public Integer getBorrowDurationDays() {
        return borrowDurationDays;
    }
    
    public void setBorrowDurationDays(Integer borrowDurationDays) {
        this.borrowDurationDays = borrowDurationDays;
    }
    
    public BigDecimal getDailyFineAmount() {
        return dailyFineAmount;
    }
    
    public void setDailyFineAmount(BigDecimal dailyFineAmount) {
        this.dailyFineAmount = dailyFineAmount;
    }
    
    public Boolean getActive() {
        return active;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public List<Payment> getPayments() {
        return payments;
    }
    
    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }
    
    // Utility methods
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(endDate);
    }
    
    public boolean isPremium() {
        return type == SubscriptionType.PREMIUM;
    }
    
    public boolean isFree() {
        return type == SubscriptionType.FREE;
    }
    
    public long getDaysUntilExpiry() {
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), endDate);
    }
}