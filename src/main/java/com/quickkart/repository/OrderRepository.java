package com.quickkart.repository;

import com.quickkart.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Get all orders placed by a customer
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Get all orders received by a store
    List<Order> findByStoreIdOrderByCreatedAtDesc(Long storeId);

    // Get a particular order belonging to a customer
    Optional<Order> findByIdAndUserId(Long orderId, Long userId);
}