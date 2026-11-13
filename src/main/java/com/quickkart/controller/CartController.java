
package com.quickkart.controller;

import com.quickkart.entity.Cart;
import com.quickkart.service.CartService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // Get customer cart
    @GetMapping("/{userId}")
    public ResponseEntity<Cart> getCart(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                cartService.getOrCreateCart(userId)
        );
    }

    // Add product to cart
    @PostMapping("/{userId}/items")
    public ResponseEntity<Cart> addToCart(
            @PathVariable Long userId,
            @RequestParam Long productId,
            @RequestParam Integer quantity) {

        return ResponseEntity.ok(
                cartService.addToCart(
                        userId,
                        productId,
                        quantity
                )
        );
    }

    // Update product quantity
    @PatchMapping("/{userId}/items/{productId}")
    public ResponseEntity<Cart> updateQuantity(
            @PathVariable Long userId,
            @PathVariable Long productId,
            @RequestParam Integer quantity) {

        return ResponseEntity.ok(
                cartService.updateQuantity(
                        userId,
                        productId,
                        quantity
                )
        );
    }

    // Remove product from cart
    @DeleteMapping("/{userId}/items/{productId}")
    public ResponseEntity<Cart> removeFromCart(
            @PathVariable Long userId,
            @PathVariable Long productId) {

        return ResponseEntity.ok(
                cartService.removeFromCart(
                        userId,
                        productId
                )
        );
    }

    // Calculate cart subtotal
    @GetMapping("/{userId}/subtotal")
    public ResponseEntity<BigDecimal> calculateSubtotal(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                cartService.calculateSubtotal(userId)
        );
    }

    // Clear entire cart
    @DeleteMapping("/{userId}/items")
    public ResponseEntity<String> clearCart(
            @PathVariable Long userId) {

        cartService.clearCart(userId);

        return ResponseEntity.ok(
                "Cart cleared successfully"
        );
    }
}
