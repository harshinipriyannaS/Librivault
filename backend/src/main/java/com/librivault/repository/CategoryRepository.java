package com.librivault.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.librivault.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    // Basic queries
    List<Category> findByActiveTrue();
    
    Page<Category> findByActiveTrue(Pageable pageable);
    
    Optional<Category> findByIdAndActiveTrue(Long id);
    
    Optional<Category> findByName(String name);
    
    Optional<Category> findByNameAndActiveTrue(String name);
    
    boolean existsByName(String name);
    
    boolean existsByNameAndActiveTrue(String name);
    
    // Search queries
    @Query("SELECT c FROM Category c WHERE " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND c.active = true")
    List<Category> searchActiveCategories(@Param("search") String search);
    
    @Query("SELECT c FROM Category c WHERE " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND c.active = true")
    Page<Category> searchActiveCategories(@Param("search") String search, Pageable pageable);
    
    // Librarian assignment queries
    @Query("SELECT c FROM Category c WHERE c.assignedLibrarian IS NULL AND c.active = true")
    List<Category> findCategoriesWithoutLibrarian();
    
    @Query("SELECT c FROM Category c WHERE c.assignedLibrarian IS NOT NULL AND c.active = true")
    List<Category> findCategoriesWithLibrarian();
    
    @Query("SELECT c FROM Category c WHERE c.assignedLibrarian.user.id = :librarianUserId AND c.active = true")
    Optional<Category> findByLibrarianUserId(@Param("librarianUserId") Long librarianUserId);
    
    // Book-related queries
    @Query("SELECT c FROM Category c WHERE SIZE(c.books) > 0 AND c.active = true")
    List<Category> findCategoriesWithBooks();
    
    @Query("SELECT c FROM Category c WHERE SIZE(c.books) = 0 AND c.active = true")
    List<Category> findEmptyCategories();
    
    @Query("SELECT c, COUNT(b) as bookCount FROM Category c " +
           "LEFT JOIN c.books b " +
           "WHERE c.active = true AND (b.active = true OR b IS NULL) " +
           "GROUP BY c " +
           "ORDER BY bookCount DESC")
    List<Object[]> findCategoriesWithBookCount();
    
    @Query("SELECT c, COUNT(b) as bookCount FROM Category c " +
           "LEFT JOIN c.books b " +
           "WHERE c.active = true AND (b.active = true OR b IS NULL) " +
           "GROUP BY c " +
           "HAVING COUNT(b) >= :minBooks " +
           "ORDER BY bookCount DESC")
    List<Object[]> findCategoriesWithMinimumBooks(@Param("minBooks") long minBooks);
    
    // Statistics queries
    @Query("SELECT COUNT(c) FROM Category c WHERE c.active = true")
    long countActiveCategories();
    
    @Query("SELECT COUNT(c) FROM Category c WHERE c.assignedLibrarian IS NOT NULL AND c.active = true")
    long countCategoriesWithLibrarian();
    
    @Query("SELECT COUNT(c) FROM Category c WHERE c.assignedLibrarian IS NULL AND c.active = true")
    long countCategoriesWithoutLibrarian();
    
    @Query("SELECT COUNT(c) FROM Category c WHERE SIZE(c.books) > 0 AND c.active = true")
    long countCategoriesWithBooks();
    
    // Popular categories (based on borrow records)
    @Query("SELECT c, COUNT(br) as borrowCount FROM Category c " +
           "LEFT JOIN c.books b " +
           "LEFT JOIN b.borrowRecords br " +
           "WHERE c.active = true " +
           "GROUP BY c " +
           "ORDER BY borrowCount DESC")
    List<Object[]> findMostPopularCategories(Pageable pageable);
    
    // Categories ordered by name
    @Query("SELECT c FROM Category c WHERE c.active = true ORDER BY c.name ASC")
    List<Category> findActiveCategoriesOrderedByName();
    
    // Categories with available books
    @Query("SELECT DISTINCT c FROM Category c " +
           "JOIN c.books b " +
           "WHERE b.availableCopies > 0 AND b.active = true AND c.active = true " +
           "ORDER BY c.name")
    List<Category> findCategoriesWithAvailableBooks();
    
    // Categories for specific librarian
    @Query("SELECT c FROM Category c WHERE c.assignedLibrarian.id = :librarianId AND c.active = true")
    Optional<Category> findByLibrarianId(@Param("librarianId") Long librarianId);
}