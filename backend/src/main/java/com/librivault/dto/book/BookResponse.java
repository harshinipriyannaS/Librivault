package com.librivault.dto.book;

import java.time.LocalDateTime;

public class BookResponse {
    
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private String description;
    private String coverImageUri;
    private Integer totalCopies;
    private Integer availableCopies;
    private LocalDateTime publishedDate;
    private Boolean active;
    private LocalDateTime createdAt;
    private String categoryName;
    private Long categoryId;
    private Boolean isAvailable;
    
    // Constructors
    public BookResponse() {}
    
    public BookResponse(Long id, String title, String author, String isbn, String description,
                       String coverImageUri, Integer totalCopies, Integer availableCopies,
                       LocalDateTime publishedDate, Boolean active, LocalDateTime createdAt,
                       String categoryName, Long categoryId) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.description = description;
        this.coverImageUri = coverImageUri;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
        this.publishedDate = publishedDate;
        this.active = active;
        this.createdAt = createdAt;
        this.categoryName = categoryName;
        this.categoryId = categoryId;
        this.isAvailable = availableCopies > 0 && active;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }
    
    public String getIsbn() {
        return isbn;
    }
    
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCoverImageUri() {
        return coverImageUri;
    }
    
    public void setCoverImageUri(String coverImageUri) {
        this.coverImageUri = coverImageUri;
    }
    
    public Integer getTotalCopies() {
        return totalCopies;
    }
    
    public void setTotalCopies(Integer totalCopies) {
        this.totalCopies = totalCopies;
    }
    
    public Integer getAvailableCopies() {
        return availableCopies;
    }
    
    public void setAvailableCopies(Integer availableCopies) {
        this.availableCopies = availableCopies;
        this.isAvailable = availableCopies > 0 && active;
    }
    
    public LocalDateTime getPublishedDate() {
        return publishedDate;
    }
    
    public void setPublishedDate(LocalDateTime publishedDate) {
        this.publishedDate = publishedDate;
    }
    
    public Boolean getActive() {
        return active;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
        this.isAvailable = availableCopies > 0 && active;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public Long getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    
    public Boolean getIsAvailable() {
        return isAvailable;
    }
    
    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }
}