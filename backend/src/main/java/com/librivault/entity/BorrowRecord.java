package com.librivault.entity;

import com.librivault.entity.enums.BorrowStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "borrow_records")
public class BorrowRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reader_id", nullable = false)
    private User reader;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;
    
    @CreationTimestamp
    @Column(name = "borrowed_at", nullable = false, updatable = false)
    private LocalDateTime borrowedAt;
    
    @Column(name = "due_date", nullable = false)
    @NotNull(message = "Due date is required")
    private LocalDateTime dueDate;
    
    @Column(name = "returned_at")
    private LocalDateTime returnedAt;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BorrowStatus status = BorrowStatus.ACTIVE;
    
    @Column(name = "used_credit", nullable = false)
    private Boolean usedCredit = false;
    
    @Column(name = "credits_earned")
    private Integer creditsEarned = 0;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToMany(mappedBy = "borrowRecord", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Fine> fines = new ArrayList<>();
    
    // Constructors
    public BorrowRecord() {}
    
    public BorrowRecord(User reader, Book book, LocalDateTime dueDate) {
        this.reader = reader;
        this.book = book;
        this.dueDate = dueDate;
        this.status = BorrowStatus.ACTIVE;
        this.usedCredit = false;
        this.creditsEarned = 0;
    }
    
    public BorrowRecord(User reader, Book book, LocalDateTime dueDate, boolean usedCredit) {
        this(reader, book, dueDate);
        this.usedCredit = usedCredit;
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
    
    public Book getBook() {
        return book;
    }
    
    public void setBook(Book book) {
        this.book = book;
    }
    
    public LocalDateTime getBorrowedAt() {
        return borrowedAt;
    }
    
    public void setBorrowedAt(LocalDateTime borrowedAt) {
        this.borrowedAt = borrowedAt;
    }
    
    public LocalDateTime getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }
    
    public LocalDateTime getReturnedAt() {
        return returnedAt;
    }
    
    public void setReturnedAt(LocalDateTime returnedAt) {
        this.returnedAt = returnedAt;
    }
    
    public BorrowStatus getStatus() {
        return status;
    }
    
    public void setStatus(BorrowStatus status) {
        this.status = status;
    }
    
    public Boolean getUsedCredit() {
        return usedCredit;
    }
    
    public void setUsedCredit(Boolean usedCredit) {
        this.usedCredit = usedCredit;
    }
    
    public Integer getCreditsEarned() {
        return creditsEarned;
    }
    
    public void setCreditsEarned(Integer creditsEarned) {
        this.creditsEarned = creditsEarned;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public List<Fine> getFines() {
        return fines;
    }
    
    public void setFines(List<Fine> fines) {
        this.fines = fines;
    }
    
    // Utility methods
    public boolean isActive() {
        return status == BorrowStatus.ACTIVE;
    }
    
    public boolean isReturned() {
        return status == BorrowStatus.RETURNED;
    }
    
    public boolean isOverdue() {
        return status == BorrowStatus.OVERDUE || 
               (status == BorrowStatus.ACTIVE && LocalDateTime.now().isAfter(dueDate));
    }
    
    public long getDaysOverdue() {
        if (!isOverdue()) {
            return 0;
        }
        LocalDateTime compareDate = returnedAt != null ? returnedAt : LocalDateTime.now();
        return java.time.temporal.ChronoUnit.DAYS.between(dueDate, compareDate);
    }
    
    public long getDaysUntilDue() {
        if (isReturned() || isOverdue()) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), dueDate);
    }
    
    public long getDaysReturnedEarly() {
        if (returnedAt == null || !isReturned()) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(returnedAt, dueDate);
    }
    
    public void returnBook() {
        this.status = BorrowStatus.RETURNED;
        this.returnedAt = LocalDateTime.now();
    }
    
    public void markAsOverdue() {
        this.status = BorrowStatus.OVERDUE;
    }
}