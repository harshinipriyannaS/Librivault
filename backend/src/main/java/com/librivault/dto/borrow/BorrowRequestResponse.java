package com.librivault.dto.borrow;

import java.time.LocalDateTime;

import com.librivault.entity.enums.RequestStatus;

public class BorrowRequestResponse {
    
    private Long id;
    private Long readerId;
    private String readerName;
    private String readerEmail;
    private Long bookId;
    private String bookTitle;
    private String bookAuthor;
    private String categoryName;
    private RequestStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime reviewedAt;
    private String reviewedByName;
    private String reviewNotes;
    
    // Constructors
    public BorrowRequestResponse() {}
    
    public BorrowRequestResponse(Long id, Long readerId, String readerName, String readerEmail,
                                Long bookId, String bookTitle, String bookAuthor, String categoryName,
                                RequestStatus status, LocalDateTime requestedAt, LocalDateTime reviewedAt,
                                String reviewedByName, String reviewNotes) {
        this.id = id;
        this.readerId = readerId;
        this.readerName = readerName;
        this.readerEmail = readerEmail;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.bookAuthor = bookAuthor;
        this.categoryName = categoryName;
        this.status = status;
        this.requestedAt = requestedAt;
        this.reviewedAt = reviewedAt;
        this.reviewedByName = reviewedByName;
        this.reviewNotes = reviewNotes;
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
    
    public RequestStatus getStatus() {
        return status;
    }
    
    public void setStatus(RequestStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }
    
    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }
    
    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }
    
    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
    
    public String getReviewedByName() {
        return reviewedByName;
    }
    
    public void setReviewedByName(String reviewedByName) {
        this.reviewedByName = reviewedByName;
    }
    
    public String getReviewNotes() {
        return reviewNotes;
    }
    
    public void setReviewNotes(String reviewNotes) {
        this.reviewNotes = reviewNotes;
    }
}