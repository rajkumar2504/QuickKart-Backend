
package com.quickkart.service;

import com.quickkart.entity.Cart;
import com.quickkart.entity.CartItem;
import com.quickkart.entity.Order;
import com.quickkart.entity.OrderItem;
import com.quickkart.entity.OrderStatus;
import com.quickkart.entity.PaymentMethod;
import com.quickkart.entity.Product;
import com.quickkart.entity.Store;

import com.quickkart.repository.OrderRepository;
import com.quickkart.repository.ProductRepository;
import com.quickkart.repository.StoreRepository;
import com.quickkart.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final CartService cartService;
    private final PaymentService paymentService;

    public OrderService(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            StoreRepository storeRepository,
            UserRepository userRepository,
            CartService cartService,
            PaymentService paymentService) {

        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
        this.cartService = cartService;
        this.paymentService = paymentService;
    }

    // 1. Checkout and place an order
    public Order checkout(
            Long userId,
            String deliveryAddress,
            PaymentMethod paymentMethod) {

        var user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (deliveryAddress == null || deliveryAddress.isBlank()) {
            throw new RuntimeException(
                    "Delivery address is required");
        }

        if (paymentMethod == null) {
            throw new RuntimeException(
                    "Payment method is required");
        }

        Cart cart = cartService.getOrCreateCart(userId);
        List<CartItem> cartItems = cart.getItems();

        if (cartItems == null || cartItems.isEmpty()) {
            throw new RuntimeException("Your cart is empty");
        }

        Store store = cartItems.get(0).getProduct().getStore();

        if (store == null || !store.isActive()) {
            throw new RuntimeException("Store is not available");
        }

        Order order = new Order();

        order.setUser(user);
        order.setStore(store);
        order.setDeliveryAddress(deliveryAddress);
        order.setStatus(OrderStatus.PENDING);

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {

            Product product = productRepository.findById(
                    cartItem.getProduct().getId()
            ).orElseThrow(() ->
                    new RuntimeException("Product not found"));

            Integer quantity = cartItem.getQuantity();

            if (!product.isActive()) {
                throw new RuntimeException(
                        product.getName() + " is unavailable");
            }

            if (!product.getStore().getId().equals(store.getId())) {
                throw new RuntimeException(
                        "All products must belong to the same store");
            }

            if (quantity == null || quantity <= 0) {
                throw new RuntimeException("Invalid quantity");
            }

            if (product.getStockQuantity() < quantity) {
                throw new RuntimeException(
                        "Insufficient stock for " + product.getName());
            }

            BigDecimal unitPrice = product.getPrice();

            BigDecimal subtotal = unitPrice.multiply(
                    BigDecimal.valueOf(quantity));

            OrderItem orderItem = new OrderItem();

            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setProductName(product.getName());
            orderItem.setUnitPrice(unitPrice);
            orderItem.setQuantity(quantity);
            orderItem.setSubtotal(subtotal);

            order.getItems().add(orderItem);

            totalAmount = totalAmount.add(subtotal);

            // Reduce stock
            product.setStockQuantity(
                    product.getStockQuantity() - quantity);

            productRepository.save(product);
        }

        order.setTotalAmount(totalAmount);

        // Save order
        Order savedOrder = orderRepository.save(order);

        // Automatically create payment record
        paymentService.createPayment(
                savedOrder.getId(),
                paymentMethod
        );

        // Clear cart after successful order and payment creation
        cartService.clearCart(userId);

        return savedOrder;
    }

    // 2. Get all customer orders
    @Transactional(readOnly = true)
    public List<Order> getCustomerOrders(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }

        return orderRepository
                .findByUserIdOrderByCreatedAtDesc(userId);
    }

    // 3. Get a particular customer order
    @Transactional(readOnly = true)
    public Order getCustomerOrder(Long orderId, Long userId) {

        return orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() ->
                        new RuntimeException("Order not found"));
    }

    // 4. Get store orders
    @Transactional(readOnly = true)
    public List<Order> getStoreOrders(Long storeId) {

        if (!storeRepository.existsById(storeId)) {
            throw new RuntimeException("Store not found");
        }

        return orderRepository
                .findByStoreIdOrderByCreatedAtDesc(storeId);
    }

    // 5. Update order status
    public Order updateOrderStatus(
            Long orderId,
            OrderStatus newStatus) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException("Order not found"));

        OrderStatus currentStatus = order.getStatus();

        if (newStatus == null) {
            throw new RuntimeException("New status is required");
        }

        boolean allowed = switch (currentStatus) {
            case PENDING -> List.of(
                    OrderStatus.CONFIRMED,
                    OrderStatus.CANCELLED,
                    OrderStatus.FAILED
            ).contains(newStatus);

            case CONFIRMED -> List.of(
                    OrderStatus.PICKER_ASSIGNED,
                    OrderStatus.CANCELLED,
                    OrderStatus.FAILED
            ).contains(newStatus);

            case PICKER_ASSIGNED -> List.of(
                    OrderStatus.PICKING,
                    OrderStatus.CANCELLED,
                    OrderStatus.FAILED
            ).contains(newStatus);

            case PICKING -> List.of(
                    OrderStatus.PACKED,
                    OrderStatus.CANCELLED,
                    OrderStatus.FAILED
            ).contains(newStatus);

            case PACKED -> List.of(
                    OrderStatus.DELIVERY_ASSIGNED,
                    OrderStatus.FAILED
            ).contains(newStatus);

            case DELIVERY_ASSIGNED -> List.of(
                    OrderStatus.OUT_FOR_DELIVERY,
                    OrderStatus.FAILED
            ).contains(newStatus);

            case OUT_FOR_DELIVERY -> List.of(
                    OrderStatus.DELIVERED,
                    OrderStatus.FAILED
            ).contains(newStatus);

            case DELIVERED, CANCELLED, FAILED -> false;
        };

        if (!allowed) {
            throw new RuntimeException(
                    "Invalid order status transition from "
                            + currentStatus + " to " + newStatus);
        }

        order.setStatus(newStatus);

        return orderRepository.save(order);
    }
}
