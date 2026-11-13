
package com.quickkart.repository;

import com.quickkart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository
        extends JpaRepository<CartItem, Long> {

    // Get all items in a cart
    List<CartItem> findByCartId(Long cartId);

    // Find a particular product in a cart
    Optional<CartItem> findByCartIdAndProductId(
            Long cartId,
            Long productId
    );

    // Delete a product from a cart
    void deleteByCartIdAndProductId(
            Long cartId,
            Long productId
    );
}
