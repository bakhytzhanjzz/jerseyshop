package com.jerseyshop.backend.service;

import com.jerseyshop.backend.entity.Category;
import com.jerseyshop.backend.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    @Transactional
    public List<Category> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        categories.forEach(category -> Hibernate.initialize(category.getProducts()));
        return categories;
    }

    @Transactional
    public Optional<Category> getCategoryById(Long id) {
        Optional<Category> category = categoryRepository.findById(id);
        category.ifPresent(c -> Hibernate.initialize(c.getProducts()));
        return category;
    }

    public Optional<Category> getCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }

    @Transactional
    public Category createCategory(Category category) {
        validateCategory(category);
        if (categoryRepository.findByName(category.getName()).isPresent()) {
            throw new IllegalArgumentException("Category name already exists");
        }
        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(Long id, Category categoryDetails) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        validateCategory(categoryDetails);
        if (!category.getName().equals(categoryDetails.getName()) &&
                categoryRepository.findByName(categoryDetails.getName()).isPresent()) {
            throw new IllegalArgumentException("Category name already exists");
        }
        category.setName(categoryDetails.getName());
        category.setDescription(categoryDetails.getDescription());
        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        if (!category.getProducts().isEmpty()) {
            throw new IllegalStateException("Cannot delete category with associated products");
        }
        categoryRepository.deleteById(id);
    }

    private void validateCategory(Category category) {
        if (category.getName() == null || category.getName().isBlank()) {
            throw new IllegalArgumentException("Category name is required");
        }
    }
}