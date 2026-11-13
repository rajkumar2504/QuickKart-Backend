
package com.quickkart.repository;

import com.quickkart.entity.Cart;
import com.quickkart.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    // Find a customer's cart
    Optional<Cart> findByUser(User user);

    // Find cart using customer ID
    Optional<Cart> findByUserId(Long userId);

    // Check whether customer already has a cart
    boolean existsByUserId(Long userId);
}
