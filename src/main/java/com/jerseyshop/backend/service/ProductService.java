package com.jerseyshop.backend.service;

import com.cloudinary.Cloudinary;
import com.jerseyshop.backend.entity.Category;
import com.jerseyshop.backend.entity.Product;
import com.jerseyshop.backend.repository.CategoryRepository;
import com.jerseyshop.backend.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private Cloudinary cloudinary;

    @Cacheable(value = "products", key = "'all'")
    @Transactional
    public List<Product> getAllProducts() {
        List<Product> products = productRepository.findAll();
        products.forEach(product -> Hibernate.initialize(product.getCategory()));
        return products;
    }

    @Cacheable(value = "products", key = "#id")
    @Transactional
    public Optional<Product> getProductById(Long id) {
        Optional<Product> product = productRepository.findById(id);
        product.ifPresent(p -> Hibernate.initialize(p.getCategory()));
        return product;
    }

    @Cacheable(value = "products", key = "'category_' + #categoryId")
    @Transactional
    public List<Product> getProductsByCategory(Long categoryId) {
        List<Product> products = productRepository.findByCategoryId(categoryId);
        products.forEach(product -> Hibernate.initialize(product.getCategory()));
        return products;
    }

    @Cacheable(value = "products", key = "'team_' + #team")
    public List<Product> getProductsByTeam(String team) {
        return productRepository.findByTeam(team);
    }

    @Cacheable(value = "products", key = "'color_' + #color")
    public List<Product> getProductsByColor(String color) {
        return productRepository.findByColor(color);
    }

    @Cacheable(value = "products", key = "'size_' + #size")
    public List<Product> getProductsBySize(String size) {
        return productRepository.findBySize(size);
    }

    @Cacheable(value = "products", key = "'search_' + #keyword")
    public List<Product> searchProducts(String keyword) {
        return productRepository.searchByKeyword(keyword);
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public Product createProduct(Product product, MultipartFile image) {
        validateProduct(product);
        Category category = categoryRepository.findById(product.getCategory().getId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        product.setCategory(category);

        if (image != null && !image.isEmpty()) {
            try {
                Map uploadResult = cloudinary.uploader().upload(image.getBytes(), Map.of());
                product.setImageUrl((String) uploadResult.get("url"));
            } catch (IOException e) {
                throw new RuntimeException("Image upload failed: " + e.getMessage());
            }
        }

        return productRepository.save(product);
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public Product updateProduct(Long id, Product productDetails, MultipartFile image) {
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

        if (image != null && !image.isEmpty()) {
            try {
                Map uploadResult = cloudinary.uploader().upload(image.getBytes(), Map.of());
                product.setImageUrl((String) uploadResult.get("url"));
            } catch (IOException e) {
                throw new RuntimeException("Image upload failed: " + e.getMessage());
            }
        } else {
            product.setImageUrl(productDetails.getImageUrl());
        }

        return productRepository.save(product);
    }

    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public Product updateProductImage(Long id, String imageUrl) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        product.setImageUrl(imageUrl); // Fixed: Set the imageUrl
        return productRepository.save(product);
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
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