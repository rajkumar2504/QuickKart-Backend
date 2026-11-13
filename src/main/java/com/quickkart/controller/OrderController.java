
package com.quickkart.controller;

import com.quickkart.entity.Order;
import com.quickkart.entity.OrderStatus;
import com.quickkart.entity.PaymentMethod;
import com.quickkart.service.OrderService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // 1. Checkout and place an order
    @PostMapping("/checkout")
    public ResponseEntity<Order> checkout(
            @RequestParam Long userId,
            @RequestParam String deliveryAddress,
            @RequestParam PaymentMethod paymentMethod) {

        Order order = orderService.checkout(
                userId,
                deliveryAddress,
                paymentMethod
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(order);
    }

    // 2. Get customer orders
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getCustomerOrders(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                orderService.getCustomerOrders(userId)
        );
    }

    // 3. Get a particular customer order
    @GetMapping("/{orderId}/user/{userId}")
    public ResponseEntity<Order> getCustomerOrder(
            @PathVariable Long orderId,
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                orderService.getCustomerOrder(orderId, userId)
        );
    }

    // 4. Get store orders
    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<Order>> getStoreOrders(
            @PathVariable Long storeId) {

        return ResponseEntity.ok(
                orderService.getStoreOrders(storeId)
        );
    }

    // 5. Update order status
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {

        return ResponseEntity.ok(
                orderService.updateOrderStatus(orderId, status)
        );
    }
}
