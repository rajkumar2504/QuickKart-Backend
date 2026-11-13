
package com.quickkart.service;

import com.quickkart.entity.*;
import com.quickkart.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            UserRepository userRepository,
            ProductRepository productRepository) {

        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    // Get existing cart or create a new cart
    public Cart getOrCreateCart(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUser(user);
                    return cartRepository.save(cart);
                });
    }

    // Add product to cart
    public Cart addToCart(
            Long userId,
            Long productId,
            Integer quantity) {

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero");
        }

        Cart cart = getOrCreateCart(userId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));

        if (!product.isActive()) {
            throw new IllegalArgumentException(
                    "Product is not available");
        }

        if (product.getStockQuantity() < quantity) {
            throw new IllegalArgumentException(
                    "Insufficient product stock");
        }

        // Keep all products in one cart from the same store
        for (CartItem item : cart.getItems()) {
            if (!item.getProduct().getStore().getId()
                    .equals(product.getStore().getId())) {
                throw new IllegalArgumentException(
                        "Products from different stores cannot be " +
                                "added to the same cart");
            }
        }

        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId()
                        .equals(productId))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {

            int newQuantity =
                    existingItem.getQuantity() + quantity;

            if (newQuantity > product.getStockQuantity()) {
                throw new IllegalArgumentException(
                        "Insufficient product stock");
            }

            existingItem.setQuantity(newQuantity);

        } else {

            CartItem cartItem = new CartItem(
                    cart,
                    product,
                    quantity
            );

            cart.getItems().add(cartItem);
        }

        return cartRepository.save(cart);
    }

    // Update cart item quantity
    public Cart updateQuantity(
            Long userId,
            Long productId,
            Integer quantity) {

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero");
        }

        Cart cart = getOrCreateCart(userId);

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(
                        cart.getId(),
                        productId
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Product not found in cart"));

        Product product = item.getProduct();

        if (quantity > product.getStockQuantity()) {
            throw new IllegalArgumentException(
                    "Insufficient product stock");
        }

        item.setQuantity(quantity);

        return cartRepository.save(cart);
    }

    // Remove product from cart
    public Cart removeFromCart(
            Long userId,
            Long productId) {

        Cart cart = getOrCreateCart(userId);

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(
                        cart.getId(),
                        productId
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Product not found in cart"));

        cart.getItems().remove(item);

        return cartRepository.save(cart);
    }

    // Calculate cart subtotal
    @Transactional(readOnly = true)
    public BigDecimal calculateSubtotal(Long userId) {

        Cart cart = getOrCreateCart(userId);

        return cart.getItems().stream()
                .map(item ->
                        item.getProduct().getPrice()
                                .multiply(BigDecimal.valueOf(
                                        item.getQuantity()
                                ))
                )
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Clear all cart items
    public void clearCart(Long userId) {

        Cart cart = getOrCreateCart(userId);

        cart.getItems().clear();

        cartRepository.save(cart);
    }
}
