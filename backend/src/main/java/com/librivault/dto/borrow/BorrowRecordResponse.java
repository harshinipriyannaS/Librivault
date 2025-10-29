package com.librivault.dto.borrow;

import java.time.LocalDateTime;

import com.librivault.entity.enums.BorrowStatus;

public class BorrowRecordResponse {
    
    private Long id;
    private Long readerId;
    private String readerName;
    private String readerEmail;
    private Long bookId;
    private String bookTitle;
    private String bookAuthor;
    private String categoryName;
    private LocalDateTime borrowedAt;
    private LocalDateTime dueDate;
    private LocalDateTime returnedAt;
    private BorrowStatus status;
    private Boolean usedCredit;
    private Integer creditsEarned;
    private Long daysUntilDue;
    private Long daysOverdue;
    private Boolean isOverdue;
    
    // Constructors
    public BorrowRecordResponse() {}
    
    public BorrowRecordResponse(Long id, Long readerId, String readerName, String readerEmail,
                               Long bookId, String bookTitle, String bookAuthor, String categoryName,
                               LocalDateTime borrowedAt, LocalDateTime dueDate, LocalDateTime returnedAt,
                               BorrowStatus status, Boolean usedCredit, Integer creditsEarned) {
        this.id = id;
        this.readerId = readerId;
        this.readerName = readerName;
        this.readerEmail = readerEmail;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.bookAuthor = bookAuthor;
        this.categoryName = categoryName;
        this.borrowedAt = borrowedAt;
        this.dueDate = dueDate;
        this.returnedAt = returnedAt;
        this.status = status;
        this.usedCredit = usedCredit;
        this.creditsEarned = creditsEarned;
        this.calculateTimeFields();
    }
    
    private void calculateTimeFields() {
        LocalDateTime now = LocalDateTime.now();
        
        if (status == BorrowStatus.RETURNED) {
            this.daysUntilDue = 0L;
            this.daysOverdue = 0L;
            this.isOverdue = false;
        } else if (now.isAfter(dueDate)) {
            this.daysUntilDue = 0L;
            this.daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(dueDate, now);
            this.isOverdue = true;
        } else {
            this.daysUntilDue = java.time.temporal.ChronoUnit.DAYS.between(now, dueDate);
            this.daysOverdue = 0L;
            this.isOverdue = false;
        }
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getReaderId() {
        return readerId;
    }
    
    public void setReaderId(Long readerId) {
        this.readerId = readerId;
    }
    
    public String getReaderName() {
        return readerName;
    }
    
    public void setReaderName(String readerName) {
        this.readerName = readerName;
    }
    
    public String getReaderEmail() {
        return readerEmail;
    }
    
    public void setReaderEmail(String readerEmail) {
        this.readerEmail = readerEmail;
    }
    
    public Long getBookId() {
        return bookId;
    }
    
    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }
    
    public String getBookTitle() {
        return bookTitle;
    }
    
    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }
    
    public String getBookAuthor() {
        return bookAuthor;
    }
    
    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
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
        this.calculateTimeFields();
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
        this.calculateTimeFields();
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
    
    public Long getDaysUntilDue() {
        return daysUntilDue;
    }
    
    public void setDaysUntilDue(Long daysUntilDue) {
        this.daysUntilDue = daysUntilDue;
    }
    
    public Long getDaysOverdue() {
        return daysOverdue;
    }
    
    public void setDaysOverdue(Long daysOverdue) {
        this.daysOverdue = daysOverdue;
    }
    
    public Boolean getIsOverdue() {
        return isOverdue;
    }
    
    public void setIsOverdue(Boolean isOverdue) {
        this.isOverdue = isOverdue;
    }
}