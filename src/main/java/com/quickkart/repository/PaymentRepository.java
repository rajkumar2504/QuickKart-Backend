package com.quickkart.repository;

import com.quickkart.entity.Payment;
import com.quickkart.entity.PaymentStatus;
import com.quickkart.entity.PaymentMethod;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Find payment associated with an order
    Optional<Payment> findByOrderId(Long orderId);

    List<Payment> findByPaymentMethod(PaymentMethod paymentMethod);

    // Find payment using transaction ID
    Optional<Payment> findByTransactionId(String transactionId);

    // Find all payments with a particular status
    List<Payment> findByPaymentStatus(PaymentStatus paymentStatus);

    // Check whether an order already has a payment record
    boolean existsByOrderId(Long orderId);



    // Find all payments made using a particular method
    List<Payment> findByPaymentMethod(
            com.quickkart.entity.PaymentMethod paymentMethod
    );
}