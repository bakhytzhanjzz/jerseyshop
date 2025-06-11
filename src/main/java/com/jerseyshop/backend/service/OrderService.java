package com.jerseyshop.backend.service;

import com.jerseyshop.backend.entity.*;
import com.jerseyshop.backend.repository.*;
import jakarta.transaction.Transactional;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public List<Order> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        orders.forEach(order -> Hibernate.initialize(order.getUser()));
        return orders;
    }

    @Transactional
    public Optional<Order> getOrderById(Long id) {
        Optional<Order> order = orderRepository.findById(id);
        order.ifPresent(o -> Hibernate.initialize(o.getUser()));
        return order;
    }

    @Transactional
    public List<Order> getOrdersByUser(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        orders.forEach(order -> Hibernate.initialize(order.getUser()));
        return orders;
    }

    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    @Transactional
    public Order createOrder(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found"));
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        Order order = new Order();
        order.setUser(cart.getUser());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");
        order.setTotalAmount(BigDecimal.ZERO);

        order = orderRepository.save(order);

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem cartItem : cartItems) {
            Product product = productRepository.findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));
            if (product.getStock() < cartItem.getQuantity()) {
                throw new IllegalStateException("Insufficient stock for product: " + product.getName());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(product.getPrice());
            orderItemRepository.save(orderItem);

            total = total.add(product.getPrice().multiply(new BigDecimal(cartItem.getQuantity())));
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);
        }

        order.setTotalAmount(total);
        orderRepository.save(order);

        // Clear cart
        cartItemRepository.deleteAll(cartItems);
        return order;
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (!List.of("NEW", "PENDING", "SHIPPING", "SHIPPED", "DELIVERED", "CANCELLED").contains(status)) {
            throw new IllegalArgumentException("Invalid status");
        }
        order.setStatus(status);
        return orderRepository.save(order);
    }
}