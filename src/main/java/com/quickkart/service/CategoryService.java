
package com.quickkart.service;

import com.quickkart.entity.Category;
import com.quickkart.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // Constructor injection
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // Create a new category
    public Category createCategory(Category category) {

        String categoryName = category.getName().trim();

        if (categoryName.isEmpty()) {
            throw new IllegalArgumentException(
                    "Category name cannot be empty"
            );
        }

        if (categoryRepository.existsByName(categoryName)) {
            throw new RuntimeException(
                    "Category already exists: " + categoryName
            );
        }

        category.setName(categoryName);

        return categoryRepository.save(category);
    }

    // Get all categories
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // Get category by ID
    public Category getCategoryById(Long id) {

        return categoryRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Category not found with ID: " + id
                        )
                );
    }

    // Delete category
    public void deleteCategory(Long id) {

        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException(
                    "Category not found with ID: " + id
            );
        }

        categoryRepository.deleteById(id);
    }
}
