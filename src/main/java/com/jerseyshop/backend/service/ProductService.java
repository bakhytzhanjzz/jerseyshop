package com.jerseyshop.backend.service;

import com.jerseyshop.backend.entity.Category;
import com.jerseyshop.backend.entity.Product;
import com.jerseyshop.backend.repository.CategoryRepository;
import com.jerseyshop.backend.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    public List<Product> getProductsByTeam(String team) {
        return productRepository.findByTeam(team);
    }

    public List<Product> getProductsByColor(String color) {
        return productRepository.findByColor(color);
    }

    public List<Product> getProductsBySize(String size) {
        return productRepository.findBySize(size);
    }

    public List<Product> searchProducts(String keyword) {
        return productRepository.searchByKeyword(keyword);
    }

    @Transactional
    public Product createProduct(Product product) {
        validateProduct(product);
        Category category = categoryRepository.findById(product.getCategory().getId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        product.setCategory(category);
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, Product productDetails) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        validateProduct(productDetails);
        Category category = categoryRepository.findById(productDetails.getCategory().getId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setStock(productDetails.getStock());
        product.setCategory(category);
        product.setTeam(productDetails.getTeam());
        product.setColor(productDetails.getColor());
        product.setSize(productDetails.getSize());
        product.setImageUrl(productDetails.getImageUrl());
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("Product not found");
        }
        productRepository.deleteById(id);
    }

    private void validateProduct(Product product) {
        if (product.getName() == null || product.getName().isBlank()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (product.getPrice() == null || product.getPrice().doubleValue() <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }
        if (product.getStock() == null || product.getStock() < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
        if (product.getCategory() == null || product.getCategory().getId() == null) {
            throw new IllegalArgumentException("Category is required");
        }
        if (product.getTeam() == null || product.getTeam().isBlank()) {
            throw new IllegalArgumentException("Team is required");
        }
        if (product.getColor() == null || product.getColor().isBlank()) {
            throw new IllegalArgumentException("Color is required");
        }
        if (product.getSize() == null || product.getSize().isBlank()) {
            throw new IllegalArgumentException("Size is required");
        }
    }
}