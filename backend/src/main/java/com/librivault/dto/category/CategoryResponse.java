package com.librivault.dto.category;

import java.time.LocalDateTime;

public class CategoryResponse {
    
    private Long id;
    private String name;
    private String description;
    private Boolean active;
    private LocalDateTime createdAt;
    private String assignedLibrarianName;
    private Long assignedLibrarianId;
    private Long bookCount;
    private Long availableBookCount;
    
    // Constructors
    public CategoryResponse() {}
    
    public CategoryResponse(Long id, String name, String description, Boolean active,
                           LocalDateTime createdAt, String assignedLibrarianName,
                           Long assignedLibrarianId, Long bookCount, Long availableBookCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.active = active;
        this.createdAt = createdAt;
        this.assignedLibrarianName = assignedLibrarianName;
        this.assignedLibrarianId = assignedLibrarianId;
        this.bookCount = bookCount;
        this.availableBookCount = availableBookCount;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
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
    
    public String getAssignedLibrarianName() {
        return assignedLibrarianName;
    }
    
    public void setAssignedLibrarianName(String assignedLibrarianName) {
        this.assignedLibrarianName = assignedLibrarianName;
    }
    
    public Long getAssignedLibrarianId() {
        return assignedLibrarianId;
    }
    
    public void setAssignedLibrarianId(Long assignedLibrarianId) {
        this.assignedLibrarianId = assignedLibrarianId;
    }
    
    public Long getBookCount() {
        return bookCount;
    }
    
    public void setBookCount(Long bookCount) {
        this.bookCount = bookCount;
    }
    
    public Long getAvailableBookCount() {
        return availableBookCount;
    }
    
    public void setAvailableBookCount(Long availableBookCount) {
        this.availableBookCount = availableBookCount;
    }
}