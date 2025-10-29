package com.librivault.dto.borrow;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.librivault.entity.enums.FineStatus;

public class FineResponse {
    
    private Long id;
    private Long readerId;
    private String readerName;
    private String readerEmail;
    private Long borrowRecordId;
    private String bookTitle;
    private String bookAuthor;
    private BigDecimal amount;
    private Integer overdueDays;
    private FineStatus status;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime waivedAt;
    private String waivedByName;
    private String waiverReason;
    
    // Constructors
    public FineResponse() {}
    
    public FineResponse(Long id, Long readerId, String readerName, String readerEmail,
                       Long borrowRecordId, String bookTitle, String bookAuthor,
                       BigDecimal amount, Integer overdueDays, FineStatus status,
                       String description, LocalDateTime createdAt, LocalDateTime paidAt,
                       LocalDateTime waivedAt, String waivedByName, String waiverReason) {
        this.id = id;
        this.readerId = readerId;
        this.readerName = readerName;
        this.readerEmail = readerEmail;
        this.borrowRecordId = borrowRecordId;
        this.bookTitle = bookTitle;
        this.bookAuthor = bookAuthor;
        this.amount = amount;
        this.overdueDays = overdueDays;
        this.status = status;
        this.description = description;
        this.createdAt = createdAt;
        this.paidAt = paidAt;
        this.waivedAt = waivedAt;
        this.waivedByName = waivedByName;
        this.waiverReason = waiverReason;
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
    
    public Long getBorrowRecordId() {
        return borrowRecordId;
    }
    
    public void setBorrowRecordId(Long borrowRecordId) {
        this.borrowRecordId = borrowRecordId;
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
    
    public String getWaivedByName() {
        return waivedByName;
    }
    
    public void setWaivedByName(String waivedByName) {
        this.waivedByName = waivedByName;
    }
    
    public String getWaiverReason() {
        return waiverReason;
    }
    
    public void setWaiverReason(String waiverReason) {
        this.waiverReason = waiverReason;
    }
}