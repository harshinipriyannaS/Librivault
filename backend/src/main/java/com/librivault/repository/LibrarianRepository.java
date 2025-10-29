package com.librivault.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.librivault.entity.Category;
import com.librivault.entity.Librarian;
import com.librivault.entity.User;

@Repository
public interface LibrarianRepository extends JpaRepository<Librarian, Long> {
    
    // Basic queries
    Optional<Librarian> findByUser(User user);
    
    Optional<Librarian> findByUserId(Long userId);
    
    Optional<Librarian> findByAssignedCategory(Category category);
    
    Optional<Librarian> findByAssignedCategoryId(Long categoryId);
    
    // Category assignment queries
    List<Librarian> findByAssignedCategoryIsNull();
    
    List<Librarian> findByAssignedCategoryIsNotNull();
    
    boolean existsByAssignedCategory(Category category);
    
    boolean existsByAssignedCategoryId(Long categoryId);
    
    // User-related queries
    @Query("SELECT l FROM Librarian l WHERE l.user.active = true")
    List<Librarian> findActiveLibrarians();
    
    @Query("SELECT l FROM Librarian l WHERE l.user.active = true AND l.assignedCategory IS NOT NULL")
    List<Librarian> findActiveLibrariansWithAssignedCategory();
    
    @Query("SELECT l FROM Librarian l WHERE l.user.active = true AND l.assignedCategory IS NULL")
    List<Librarian> findActiveLibrariansWithoutAssignedCategory();
    
    // Search queries
    @Query("SELECT l FROM Librarian l WHERE " +
           "(LOWER(l.user.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(l.user.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(l.user.email) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND l.user.active = true")
    List<Librarian> searchActiveLibrarians(@Param("search") String search);
    
    // Statistics queries
    @Query("SELECT COUNT(l) FROM Librarian l WHERE l.user.active = true")
    long countActiveLibrarians();
    
    @Query("SELECT COUNT(l) FROM Librarian l WHERE l.user.active = true AND l.assignedCategory IS NOT NULL")
    long countLibrariansWithAssignedCategory();
    
    @Query("SELECT COUNT(l) FROM Librarian l WHERE l.user.active = true AND l.assignedCategory IS NULL")
    long countLibrariansWithoutAssignedCategory();
    
    // Recent activity queries
    @Query("SELECT l FROM Librarian l WHERE l.assignedAt >= :since ORDER BY l.assignedAt DESC")
    List<Librarian> findRecentlyAssignedLibrarians(@Param("since") LocalDateTime since);
    
    // Category-related queries
    @Query("SELECT l.assignedCategory FROM Librarian l WHERE l.user.active = true AND l.assignedCategory IS NOT NULL")
    List<Category> findAssignedCategories();
    
    @Query("SELECT c FROM Category c WHERE c NOT IN " +
           "(SELECT l.assignedCategory FROM Librarian l WHERE l.assignedCategory IS NOT NULL AND l.user.active = true)")
    List<Category> findUnassignedCategories();
}