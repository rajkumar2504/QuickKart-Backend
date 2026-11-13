package com.quickkart.controller;

import com.quickkart.entity.Payment;
import com.quickkart.entity.PaymentMethod;
import com.quickkart.service.PaymentService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // 1. Create payment for an order
    @PostMapping
    public ResponseEntity<Payment> createPayment(
            @RequestParam Long orderId,
            @RequestParam PaymentMethod paymentMethod) {

        Payment payment = paymentService.createPayment(
                orderId, paymentMethod
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(payment);
    }

    // 2. Confirm COD payment after cash collection
    @PatchMapping("/{paymentId}/confirm-cod")
    public ResponseEntity<Payment> confirmCodPayment(
            @PathVariable Long paymentId) {

        return ResponseEntity.ok(
                paymentService.confirmCodPayment(paymentId)
        );
    }

    // 3. Get payment by ID
    @GetMapping("/{paymentId}")
    public ResponseEntity<Payment> getPaymentById(
            @PathVariable Long paymentId) {

        return ResponseEntity.ok(
                paymentService.getPaymentById(paymentId)
        );
    }

    // 4. Get payment by order ID
    @GetMapping("/order/{orderId}")
    public ResponseEntity<Payment> getPaymentByOrder(
            @PathVariable Long orderId) {

        return ResponseEntity.ok(
                paymentService.getPaymentByOrder(orderId)
        );
    }

    // 5. Get payment by transaction ID
    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<Payment> getPaymentByTransactionId(
            @PathVariable String transactionId) {

        return ResponseEntity.ok(
                paymentService.getPaymentByTransactionId(transactionId)
        );
    }
}