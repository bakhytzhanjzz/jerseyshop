package com.jerseyshop.backend.controller;

import com.jerseyshop.backend.entity.Cart;
import com.jerseyshop.backend.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    @Autowired
    private CartService cartService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<Cart> getCartByUserId(@PathVariable Long userId) {
        return cartService.getCartByUserId(userId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/user/{userId}/add")
    public ResponseEntity<Cart> addItemToCart(@PathVariable Long userId,
                                              @RequestParam Long productId,
                                              @RequestParam Integer quantity) {
        return ResponseEntity.ok(cartService.addItemToCart(userId, productId, quantity));
    }

    @PutMapping("/user/{userId}/update")
    public ResponseEntity<Cart> updateCartItemQuantity(@PathVariable Long userId,
                                                       @RequestParam Long productId,
                                                       @RequestParam Integer quantity) {
        return ResponseEntity.ok(cartService.updateCartItemQuantity(userId, productId, quantity));
    }

    @DeleteMapping("/user/{userId}/remove")
    public ResponseEntity<Cart> removeItemFromCart(@PathVariable Long userId, @RequestParam Long productId) {
        return ResponseEntity.ok(cartService.removeItemFromCart(userId, productId));
    }

    @DeleteMapping("/user/{userId}/clear")
    public ResponseEntity<Void> clearCart(@PathVariable Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}