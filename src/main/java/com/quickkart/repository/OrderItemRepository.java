package com.quickkart.repository;

import com.quickkart.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // Get all items belonging to a particular order
    List<OrderItem> findByOrderId(Long orderId);
}