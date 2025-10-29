package com.librivault.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.librivault.dto.category.CategoryRequest;
import com.librivault.dto.category.CategoryResponse;
import com.librivault.entity.Category;
import com.librivault.entity.Librarian;
import com.librivault.repository.BookRepository;
import com.librivault.repository.CategoryRepository;
import com.librivault.repository.LibrarianRepository;

@Service
public class CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private LibrarianRepository librarianRepository;

    // Public category browsing (no authentication required)
    public List<CategoryResponse> getAllActiveCategories() {
        logger.info("Fetching all active categories");
        return categoryRepository.findByActiveTrue()
                .stream()
                .map(this::convertToCategoryResponse)
                .collect(Collectors.toList());
    }

    public Page<CategoryResponse> getAllActiveCategories(Pageable pageable) {
        logger.info("Fetching all active categories with pagination");
        return categoryRepository.findByActiveTrue(pageable)
                .map(this::convertToCategoryResponse);
    }

    public CategoryResponse getCategoryById(Long categoryId) {
        logger.info("Fetching category by ID: {}", categoryId);
        Category category = categoryRepository.findByIdAndActiveTrue(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));
        return convertToCategoryResponse(category);
    }

    public List<CategoryResponse> getCategoriesWithAvailableBooks() {
        logger.info("Fetching categories with available books");
        return categoryRepository.findCategoriesWithAvailableBooks()
                .stream()
                .map(this::convertToCategoryResponse)
                .collect(Collectors.toList());
    }

    public List<CategoryResponse> searchCategories(String search) {
        logger.info("Searching categories with query: {}", search);
        return categoryRepository.searchActiveCategories(search)
                .stream()
                .map(this::convertToCategoryResponse)
                .collect(Collectors.toList());
    }

    // Category management (Admin/Librarian only)
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public Page<CategoryResponse> getAllCategories(Pageable pageable) {
        logger.info("Fetching all categories (including inactive) with pagination");
        return categoryRepository.findAll(pageable)
                .map(this::convertToCategoryResponse);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Transactional
    public CategoryResponse createCategory(CategoryRequest categoryRequest) {
        logger.info("Creating new category: {}", categoryRequest.getName());

        // Check if category name already exists
        if (categoryRepository.existsByName(categoryRequest.getName())) {
            throw new RuntimeException("Category with name '" + categoryRequest.getName() + "' already exists");
        }

        Category category = new Category();
        category.setName(categoryRequest.getName());
        category.setDescription(categoryRequest.getDescription());
        category.setActive(true);

        Category savedCategory = categoryRepository.save(category);
        logger.info("Category created successfully with ID: {}", savedCategory.getId());

        return convertToCategoryResponse(savedCategory);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Transactional
    public CategoryResponse updateCategory(Long categoryId, CategoryRequest categoryRequest) {
        logger.info("Updating category: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));

        // Check if new name conflicts with existing category (excluding current one)
        if (!category.getName().equals(categoryRequest.getName())
                && categoryRepository.existsByName(categoryRequest.getName())) {
            throw new RuntimeException("Category with name '" + categoryRequest.getName() + "' already exists");
        }

        category.setName(categoryRequest.getName());
        category.setDescription(categoryRequest.getDescription());

        Category updatedCategory = categoryRepository.save(category);
        logger.info("Category updated successfully: {}", categoryId);

        return convertToCategoryResponse(updatedCategory);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Transactional
    public void deleteCategory(Long categoryId) {
        logger.info("Deleting category: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));

        // Check if category has books
        long bookCount = bookRepository.countActiveBooksInCategory(categoryId);
        if (bookCount > 0) {
            throw new RuntimeException("Cannot delete category with existing books. Please move or delete books first.");
        }

        // Remove librarian assignment if exists
        librarianRepository.findByAssignedCategory(category).ifPresent(librarian -> {
            librarian.setAssignedCategory(null);
            librarianRepository.save(librarian);
        });

        // Soft delete - mark as inactive
        category.setActive(false);
        categoryRepository.save(category);

        logger.info("Category deleted (deactivated) successfully: {}", categoryId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void permanentlyDeleteCategory(Long categoryId) {
        logger.info("Permanently deleting category: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));

        // Check if category has books
        long bookCount = bookRepository.countActiveBooksInCategory(categoryId);
        if (bookCount > 0) {
            throw new RuntimeException("Cannot permanently delete category with existing books.");
        }

        // Remove librarian assignment if exists
        librarianRepository.findByAssignedCategory(category).ifPresent(librarian -> {
            librarian.setAssignedCategory(null);
            librarianRepository.save(librarian);
        });

        // Delete from database
        categoryRepository.delete(category);

        logger.info("Category permanently deleted: {}", categoryId);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Transactional
    public void activateCategory(Long categoryId) {
        logger.info("Activating category: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));

        category.setActive(true);
        categoryRepository.save(category);

        logger.info("Category activated successfully: {}", categoryId);
    }

    // Librarian assignment management
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public CategoryResponse assignLibrarianToCategory(Long categoryId, Long librarianUserId) {
        logger.info("Assigning librarian {} to category: {}", librarianUserId, categoryId);

        Category category = categoryRepository.findByIdAndActiveTrue(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));

        // Check if category already has a librarian
        if (librarianRepository.existsByAssignedCategory(category)) {
            throw new RuntimeException("Category already has an assigned librarian");
        }

        Librarian librarian = librarianRepository.findByUserId(librarianUserId)
                .orElseThrow(() -> new RuntimeException("Librarian not found with user id: " + librarianUserId));

        // Remove existing assignment if any
        if (librarian.getAssignedCategory() != null) {
            throw new RuntimeException("Librarian is already assigned to another category");
        }

        librarian.setAssignedCategory(category);
        librarianRepository.save(librarian);

        logger.info("Librarian assigned successfully to category");
        return convertToCategoryResponse(category);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public CategoryResponse removeLibrarianFromCategory(Long categoryId) {
        logger.info("Removing librarian from category: {}", categoryId);

        Category category = categoryRepository.findByIdAndActiveTrue(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));

        librarianRepository.findByAssignedCategory(category).ifPresent(librarian -> {
            librarian.setAssignedCategory(null);
            librarianRepository.save(librarian);
        });

        logger.info("Librarian removed successfully from category");
        return convertToCategoryResponse(category);
    }

    // Statistics and reporting
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public long getTotalActiveCategories() {
        return categoryRepository.countActiveCategories();
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public long getCategoriesWithLibrarian() {
        return categoryRepository.countCategoriesWithLibrarian();
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public long getCategoriesWithoutLibrarianCount() {
        return categoryRepository.countCategoriesWithoutLibrarian();
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public List<CategoryResponse> getCategoriesWithoutLibrarian() {
        return categoryRepository.findCategoriesWithoutLibrarian()
                .stream()
                .map(this::convertToCategoryResponse)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<Object[]> getMostPopularCategories(int limit) {
        return categoryRepository.findMostPopularCategories(
                org.springframework.data.domain.PageRequest.of(0, limit));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<Object[]> getCategoriesWithBookCount() {
        return categoryRepository.findCategoriesWithBookCount();
    }

    // Librarian-specific methods
    @PreAuthorize("hasRole('LIBRARIAN')")
    public CategoryResponse getLibrarianAssignedCategory(Long librarianUserId) {
        logger.info("Fetching assigned category for librarian: {}", librarianUserId);

        Category category = categoryRepository.findByLibrarianUserId(librarianUserId)
                .orElseThrow(() -> new RuntimeException("No category assigned to librarian"));

        return convertToCategoryResponse(category);
    }

    // Helper methods
    private CategoryResponse convertToCategoryResponse(Category category) {
        String librarianName = null;
        Long librarianId = null;

        if (category.getAssignedLibrarian() != null) {
            librarianName = category.getAssignedLibrarian().getUser().getFullName();
            librarianId = category.getAssignedLibrarian().getUser().getId();
        }

        // Get book counts
        long totalBooks = bookRepository.countActiveBooksInCategory(category.getId());
        long availableBooks = bookRepository.findAvailableBooksInCategory(category.getId()).size();

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getActive(),
                category.getCreatedAt(),
                librarianName,
                librarianId,
                totalBooks,
                (long) availableBooks
        );
    }

    public Category getCategoryEntityById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));
    }

    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }
}
