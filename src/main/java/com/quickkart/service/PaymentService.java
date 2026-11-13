package com.quickkart.service;

import com.quickkart.entity.Order;
import com.quickkart.entity.Payment;
import com.quickkart.entity.PaymentMethod;
import com.quickkart.entity.PaymentStatus;

import com.quickkart.repository.OrderRepository;
import com.quickkart.repository.PaymentRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public PaymentService(
            PaymentRepository paymentRepository,
            OrderRepository orderRepository) {

        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    // 1. Create payment for an order
    public Payment createPayment(
            Long orderId,
            PaymentMethod paymentMethod) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException("Order not found"));

        if (paymentRepository.existsByOrderId(orderId)) {
            throw new RuntimeException(
                    "Payment already exists for this order");
        }

        if (paymentMethod == null) {
            throw new RuntimeException(
                    "Payment method is required");
        }

        if (order.getTotalAmount() == null
                || order.getTotalAmount().signum() <= 0) {
            throw new RuntimeException(
                    "Invalid order amount");
        }

        Payment payment = new Payment();

        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentMethod(paymentMethod);
        payment.setPaymentStatus(PaymentStatus.PENDING);

        // Both COD and ONLINE initially remain pending.
        // Online payment requires gateway verification.
        return paymentRepository.save(payment);
    }

    // 2. Process Cash on Delivery after collection
    public Payment confirmCodPayment(Long paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new RuntimeException("Payment not found"));

        if (payment.getPaymentMethod() != PaymentMethod.COD) {
            throw new RuntimeException(
                    "This payment is not Cash on Delivery");
        }

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new RuntimeException(
                    "Only pending COD payments can be confirmed");
        }

        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());

        return paymentRepository.save(payment);
    }

    // 3. Get payment by payment ID
    @Transactional(readOnly = true)
    public Payment getPaymentById(Long paymentId) {

        return paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new RuntimeException("Payment not found"));
    }

    // 4. Get payment by order ID
    @Transactional(readOnly = true)
    public Payment getPaymentByOrder(Long orderId) {

        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Payment not found for this order"));
    }

    // 5. Get payment by transaction ID
    @Transactional(readOnly = true)
    public Payment getPaymentByTransactionId(
            String transactionId) {

        return paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Transaction not found"));
    }
}