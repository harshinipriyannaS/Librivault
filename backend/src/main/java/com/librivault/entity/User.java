package com.librivault.entity;
import com.librivault.entity.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;
    
    @Column(nullable = false)
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    
    @Column(name = "first_name", nullable = false)
    @NotBlank(message = "First name is required")
    private String firstName;
    
    @Column(name = "last_name", nullable = false)
    @NotBlank(message = "Last name is required")
    private String lastName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.READER;
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @Column(name = "reader_credits")
    private Integer readerCredits = 0;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    // Relationships
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Subscription subscription;
    
    @OneToMany(mappedBy = "reader", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BorrowRecord> borrowHistory = new ArrayList<>();
    
    @OneToMany(mappedBy = "reader", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Fine> fines = new ArrayList<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Payment> payments = new ArrayList<>();
    
    @OneToMany(mappedBy = "reader", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BorrowRequest> borrowRequests = new ArrayList<>();
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Librarian librarianProfile;
    
    // Constructors
    public User() {}
    
    public User(String email, String password, String firstName, String lastName) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = Role.READER;
        this.active = true;
        this.readerCredits = 0;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public Role getRole() {
        return role;
    }
    
    public void setRole(Role role) {
        this.role = role;
    }
    
    public Boolean getActive() {
        return active;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    public Integer getReaderCredits() {
        return readerCredits;
    }
    
    public void setReaderCredits(Integer readerCredits) {
        this.readerCredits = readerCredits;
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
    
    public LocalDateTime getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }
    
    public Subscription getSubscription() {
        return subscription;
    }
    
    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }
    
    public List<BorrowRecord> getBorrowHistory() {
        return borrowHistory;
    }
    
    public void setBorrowHistory(List<BorrowRecord> borrowHistory) {
        this.borrowHistory = borrowHistory;
    }
    
    public List<Fine> getFines() {
        return fines;
    }
    
    public void setFines(List<Fine> fines) {
        this.fines = fines;
    }
    
    public List<Payment> getPayments() {
        return payments;
    }
    
    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }
    
    public List<BorrowRequest> getBorrowRequests() {
        return borrowRequests;
    }
    
    public void setBorrowRequests(List<BorrowRequest> borrowRequests) {
        this.borrowRequests = borrowRequests;
    }
    
    public Librarian getLibrarianProfile() {
        return librarianProfile;
    }
    
    public void setLibrarianProfile(Librarian librarianProfile) {
        this.librarianProfile = librarianProfile;
    }
    
    // Utility methods
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public boolean isLibrarian() {
        return role == Role.LIBRARIAN;
    }
    
    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
    
    public boolean isReader() {
        return role == Role.READER;
    }
}