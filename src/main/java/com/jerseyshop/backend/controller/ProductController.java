package com.jerseyshop.backend.controller;

import com.jerseyshop.backend.entity.Product;
import com.jerseyshop.backend.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Optional<Product> product = productService.getProductById(id);
        return product.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{categoryId}")
    public List<Product> getProductsByCategory(@PathVariable Long categoryId) {
        return productService.getProductsByCategory(categoryId);
    }

    @GetMapping("/team/{team}")
    public List<Product> getProductsByTeam(@PathVariable String team) {
        return productService.getProductsByTeam(team);
    }

    @GetMapping("/color/{color}")
    public List<Product> getProductsByColor(@PathVariable String color) {
        return productService.getProductsByColor(color);
    }

    @GetMapping("/size/{size}")
    public List<Product> getProductsBySize(@PathVariable String size) {
        return productService.getProductsBySize(size);
    }

    @GetMapping("/search")
    public List<Product> searchProducts(@RequestParam String keyword) {
        return productService.searchProducts(keyword);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createProduct(
            @RequestPart("product") Product product,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        try {
            Product createdProduct = productService.createProduct(product, image);
            return ResponseEntity.ok(createdProduct);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create product: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @RequestPart(value = "product", required = false) Product productDetails,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestBody(required = false) Product jsonProduct) {
        try {
            if (jsonProduct != null) {
                Product updatedProduct = productService.updateProduct(id, jsonProduct, null);
                return ResponseEntity.ok(updatedProduct);
            } else {
                Product updatedProduct = productService.updateProduct(id, productDetails, image);
                return ResponseEntity.ok(updatedProduct);
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update product: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/image")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateProductImage(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String imageUrl = request.get("imageUrl");
            if (imageUrl == null || imageUrl.isBlank()) {
                return ResponseEntity.badRequest().body("imageUrl is required");
            }
            Product updatedProduct = productService.updateProductImage(id, imageUrl);
            return ResponseEntity.ok(updatedProduct);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}