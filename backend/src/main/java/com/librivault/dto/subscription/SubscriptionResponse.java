package com.librivault.dto.subscription;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.librivault.entity.enums.SubscriptionType;

public class SubscriptionResponse {
    
    private Long id;
    private Long userId;
    private SubscriptionType type;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer bookLimit;
    private Integer borrowDurationDays;
    private BigDecimal dailyFineAmount;
    private Boolean active;
    private LocalDateTime createdAt;
    private Long daysUntilExpiry;
    private Boolean isExpired;
    
    // Constructors
    public SubscriptionResponse() {}
    
    public SubscriptionResponse(Long id, Long userId, SubscriptionType type, LocalDateTime startDate,
                               LocalDateTime endDate, Integer bookLimit, Integer borrowDurationDays,
                               BigDecimal dailyFineAmount, Boolean active, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
        this.bookLimit = bookLimit;
        this.borrowDurationDays = borrowDurationDays;
        this.dailyFineAmount = dailyFineAmount;
        this.active = active;
        this.createdAt = createdAt;
        this.daysUntilExpiry = calculateDaysUntilExpiry();
        this.isExpired = LocalDateTime.now().isAfter(endDate);
    }
    
    private Long calculateDaysUntilExpiry() {
        if (LocalDateTime.now().isAfter(endDate)) {
            return 0L;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), endDate);
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
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
        this.daysUntilExpiry = calculateDaysUntilExpiry();
        this.isExpired = LocalDateTime.now().isAfter(endDate);
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
    
    public Long getDaysUntilExpiry() {
        return daysUntilExpiry;
    }
    
    public void setDaysUntilExpiry(Long daysUntilExpiry) {
        this.daysUntilExpiry = daysUntilExpiry;
    }
    
    public Boolean getIsExpired() {
        return isExpired;
    }
    
    public void setIsExpired(Boolean isExpired) {
        this.isExpired = isExpired;
    }
}