package com.librivault.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.librivault.entity.enums.FineStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "fines")
public class Fine {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reader_id", nullable = false)
    private User reader;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrow_record_id", nullable = false)
    private BorrowRecord borrowRecord;
    
    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @Column(name = "overdue_days", nullable = false)
    @NotNull(message = "Overdue days is required")
    @Positive(message = "Overdue days must be positive")
    private Integer overdueDays;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FineStatus status = FineStatus.PENDING;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    
    @Column(name = "waived_at")
    private LocalDateTime waivedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "waived_by")
    private User waivedBy;
    
    @Column(name = "waiver_reason")
    private String waiverReason;
    
    // Relationships
    @OneToMany(mappedBy = "fine", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Payment> payments = new ArrayList<>();
    
    // Constructors
    public Fine() {}
    
    public Fine(User reader, BorrowRecord borrowRecord, BigDecimal amount, Integer overdueDays) {
        this.reader = reader;
        this.borrowRecord = borrowRecord;
        this.amount = amount;
        this.overdueDays = overdueDays;
        this.status = FineStatus.PENDING;
        this.description = String.format("Fine for overdue book: %s (%d days overdue)", 
                                        borrowRecord.getBook().getTitle(), overdueDays);
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getReader() {
        return reader;
    }
    
    public void setReader(User reader) {
        this.reader = reader;
    }
    
    public BorrowRecord getBorrowRecord() {
        return borrowRecord;
    }
    
    public void setBorrowRecord(BorrowRecord borrowRecord) {
        this.borrowRecord = borrowRecord;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public Integer getOverdueDays() {
        return overdueDays;
    }
    
    public void setOverdueDays(Integer overdueDays) {
        this.overdueDays = overdueDays;
    }
    
    public FineStatus getStatus() {
        return status;
    }
    
    public void setStatus(FineStatus status) {
        this.status = status;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
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
    
    public LocalDateTime getPaidAt() {
        return paidAt;
    }
    
    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }
    
    public LocalDateTime getWaivedAt() {
        return waivedAt;
    }
    
    public void setWaivedAt(LocalDateTime waivedAt) {
        this.waivedAt = waivedAt;
    }
    
    public User getWaivedBy() {
        return waivedBy;
    }
    
    public void setWaivedBy(User waivedBy) {
        this.waivedBy = waivedBy;
    }
    
    public String getWaiverReason() {
        return waiverReason;
    }
    
    public void setWaiverReason(String waiverReason) {
        this.waiverReason = waiverReason;
    }
    
    public List<Payment> getPayments() {
        return payments;
    }
    
    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }
    
    // Utility methods
    public boolean isPending() {
        return status == FineStatus.PENDING;
    }
    
    public boolean isPaid() {
        return status == FineStatus.PAID;
    }
    
    public boolean isWaived() {
        return status == FineStatus.WAIVED;
    }
    
    public void markAsPaid() {
        this.status = FineStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }
    
    public void waive(User waivedBy, String reason) {
        this.status = FineStatus.WAIVED;
        this.waivedBy = waivedBy;
        this.waiverReason = reason;
        this.waivedAt = LocalDateTime.now();
    }
    
    public BigDecimal calculateDailyRate() {
        if (overdueDays == 0) {
            return BigDecimal.ZERO;
        }
        return amount.divide(BigDecimal.valueOf(overdueDays), 2, BigDecimal.ROUND_HALF_UP);
    }
}