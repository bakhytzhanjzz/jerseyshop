package com.jerseyshop.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private String team;

    @Column(nullable = false)
    private String color;

    @Column(nullable = false)
    private String size;

    @Column(nullable = true)
    private String imageUrl;

    @JsonIgnore
    @OneToMany(mappedBy = "product")
    private java.util.List<OrderItem> orderItems;

    @JsonIgnore
    @OneToMany(mappedBy = "product")
    private java.util.List<CartItem> cartItems;
}