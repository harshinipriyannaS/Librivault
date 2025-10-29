package com.librivault.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.librivault.dto.category.CategoryRequest;
import com.librivault.dto.category.CategoryResponse;
import com.librivault.service.CategoryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/categories")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CategoryController {

    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

    @Autowired
    private CategoryService categoryService;

    // Public endpoints (no authentication required)
    @GetMapping
    public ResponseEntity<?> getAllActiveCategories() {
        try {
            logger.info("Fetching all active categories");
            List<CategoryResponse> categories = categoryService.getAllActiveCategories();
            return ResponseEntity.ok(categories);

        } catch (Exception e) {
            logger.error("Failed to fetch categories", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to fetch categories", e.getMessage()));
        }
    }

    @GetMapping("/paginated")
    public ResponseEntity<?> getAllActiveCategoriesPaginated(Pageable pageable) {
        try {
            logger.info("Fetching all active categories with pagination");
            Page<CategoryResponse> categories = categoryService.getAllActiveCategories(pageable);
            return ResponseEntity.ok(categories);

        } catch (Exception e) {
            logger.error("Failed to fetch categories with pagination", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to fetch categories", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable Long id) {
        try {
            logger.info("Fetching category by ID: {}", id);
            CategoryResponse category = categoryService.getCategoryById(id);
            return ResponseEntity.ok(category);

        } catch (Exception e) {
            logger.error("Failed to fetch category by ID: {}", id, e);
            return ResponseEntity.badRequest().body(createErrorResponse("Category not found", e.getMessage()));
        }
    }

    @GetMapping("/with-books")
    public ResponseEntity<?> getCategoriesWithAvailableBooks() {
        try {
            logger.info("Fetching categories with available books");
            List<CategoryResponse> categories = categoryService.getCategoriesWithAvailableBooks();
            return ResponseEntity.ok(categories);

        } catch (Exception e) {
            logger.error("Failed to fetch categories with available books", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to fetch categories", e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchCategories(@RequestParam String query) {
        try {
            logger.info("Searching categories with query: {}", query);
            List<CategoryResponse> categories = categoryService.searchCategories(query);
            return ResponseEntity.ok(categories);

        } catch (Exception e) {
            logger.error("Failed to search categories with query: {}", query, e);
            return ResponseEntity.badRequest().body(createErrorResponse("Search failed", e.getMessage()));
        }
    }

    // Admin/Librarian endpoints
    @GetMapping("/all")
    public ResponseEntity<?> getAllCategories(Pageable pageable) {
        try {
            logger.info("Fetching all categories (including inactive) with pagination");
            Page<CategoryResponse> categories = categoryService.getAllCategories(pageable);
            return ResponseEntity.ok(categories);

        } catch (Exception e) {
            logger.error("Failed to fetch all categories", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to fetch categories", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createCategory(@Valid @RequestBody CategoryRequest categoryRequest) {
        try {
            logger.info("Creating new category: {}", categoryRequest.getName());
            CategoryResponse category = categoryService.createCategory(categoryRequest);
            return ResponseEntity.ok(category);

        } catch (Exception e) {
            logger.error("Failed to create category: {}", categoryRequest.getName(), e);
            return ResponseEntity.badRequest().body(createErrorResponse("Category creation failed", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest categoryRequest) {
        try {
            logger.info("Updating category: {}", id);
            CategoryResponse category = categoryService.updateCategory(id, categoryRequest);
            return ResponseEntity.ok(category);

        } catch (Exception e) {
            logger.error("Failed to update category: {}", id, e);
            return ResponseEntity.badRequest().body(createErrorResponse("Category update failed", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        try {
            logger.info("Deleting category: {}", id);
            categoryService.deleteCategory(id);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Category deleted successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to delete category: {}", id, e);
            return ResponseEntity.badRequest().body(createErrorResponse("Category deletion failed", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<?> permanentlyDeleteCategory(@PathVariable Long id) {
        try {
            logger.info("Permanently deleting category: {}", id);
            categoryService.permanentlyDeleteCategory(id);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Category permanently deleted successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to permanently delete category: {}", id, e);
            return ResponseEntity.badRequest().body(createErrorResponse("Permanent deletion failed", e.getMessage()));
        }
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activateCategory(@PathVariable Long id) {
        try {
            logger.info("Activating category: {}", id);
            categoryService.activateCategory(id);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Category activated successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to activate category: {}", id, e);
            return ResponseEntity.badRequest().body(createErrorResponse("Category activation failed", e.getMessage()));
        }
    }

    // Librarian assignment endpoints (Admin only)
    @PutMapping("/{id}/assign-librarian")
    public ResponseEntity<?> assignLibrarianToCategory(@PathVariable Long id, @RequestBody AssignLibrarianRequest request) {
        try {
            logger.info("Assigning librarian {} to category: {}", request.librarianUserId, id);
            CategoryResponse category = categoryService.assignLibrarianToCategory(id, request.librarianUserId);
            return ResponseEntity.ok(category);

        } catch (Exception e) {
            logger.error("Failed to assign librarian to category: {}", id, e);
            return ResponseEntity.badRequest().body(createErrorResponse("Librarian assignment failed", e.getMessage()));
        }
    }

    @PutMapping("/{id}/remove-librarian")
    public ResponseEntity<?> removeLibrarianFromCategory(@PathVariable Long id) {
        try {
            logger.info("Removing librarian from category: {}", id);
            CategoryResponse category = categoryService.removeLibrarianFromCategory(id);
            return ResponseEntity.ok(category);

        } catch (Exception e) {
            logger.error("Failed to remove librarian from category: {}", id, e);
            return ResponseEntity.badRequest().body(createErrorResponse("Librarian removal failed", e.getMessage()));
        }
    }

    @GetMapping("/without-librarian")
    public ResponseEntity<?> getCategoriesWithoutLibrarian() {
        try {
            logger.info("Fetching categories without librarian");
            List<CategoryResponse> categories = categoryService.getCategoriesWithoutLibrarian();
            return ResponseEntity.ok(categories);

        } catch (Exception e) {
            logger.error("Failed to fetch categories without librarian", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to fetch categories", e.getMessage()));
        }
    }

    // Statistics endpoints
    @GetMapping("/stats/total")
    public ResponseEntity<?> getTotalActiveCategories() {
        try {
            long totalCategories = categoryService.getTotalActiveCategories();

            Map<String, Object> response = new HashMap<>();
            response.put("totalActiveCategories", totalCategories);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to get total active categories", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to get statistics", e.getMessage()));
        }
    }

    @GetMapping("/stats/with-librarian")
    public ResponseEntity<?> getCategoriesWithLibrarian() {
        try {
            long categoriesWithLibrarian = categoryService.getCategoriesWithLibrarian();

            Map<String, Object> response = new HashMap<>();
            response.put("categoriesWithLibrarian", categoriesWithLibrarian);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to get categories with librarian count", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to get statistics", e.getMessage()));
        }
    }

    @GetMapping("/stats/without-librarian")
    public ResponseEntity<?> getCategoriesWithoutLibrarianCount() {
        try {
            long categoriesWithoutLibrarian = categoryService.getCategoriesWithoutLibrarianCount();

            Map<String, Object> response = new HashMap<>();
            response.put("categoriesWithoutLibrarian", categoriesWithoutLibrarian);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to get categories without librarian count", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to get statistics", e.getMessage()));
        }
    }

    @GetMapping("/stats/most-popular")
    public ResponseEntity<?> getMostPopularCategories(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<Object[]> mostPopular = categoryService.getMostPopularCategories(limit);

            Map<String, Object> response = new HashMap<>();
            response.put("limit", limit);
            response.put("mostPopularCategories", mostPopular);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to get most popular categories", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to get statistics", e.getMessage()));
        }
    }

    @GetMapping("/stats/book-count")
    public ResponseEntity<?> getCategoriesWithBookCount() {
        try {
            List<Object[]> categoriesWithBookCount = categoryService.getCategoriesWithBookCount();

            Map<String, Object> response = new HashMap<>();
            response.put("categoriesWithBookCount", categoriesWithBookCount);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to get categories with book count", e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to get statistics", e.getMessage()));
        }
    }

    // Helper method to create error response
    private Map<String, String> createErrorResponse(String error, String message) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", error);
        errorResponse.put("message", message);
        return errorResponse;
    }

    // Request DTOs
    public static class AssignLibrarianRequest {

        public Long librarianUserId;
    }
}
