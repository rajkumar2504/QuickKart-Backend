package com.quickkart.controller;

import com.quickkart.entity.DeliveryPartner;
import com.quickkart.service.DeliveryPartnerService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delivery-partners")
public class DeliveryPartnerController {

    private final DeliveryPartnerService deliveryPartnerService;

    public DeliveryPartnerController(
            DeliveryPartnerService deliveryPartnerService) {
        this.deliveryPartnerService = deliveryPartnerService;
    }

    // 1. Register a delivery partner
    @PostMapping
    public ResponseEntity<DeliveryPartner> createDeliveryPartner(
            @RequestParam Long userId,
            @RequestParam Long storeId,
            @RequestParam String phoneNumber) {

        DeliveryPartner partner =
                deliveryPartnerService.createDeliveryPartner(
                        userId, storeId, phoneNumber
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(partner);
    }

    // 2. Get all delivery partners
    @GetMapping
    public ResponseEntity<List<DeliveryPartner>> getAllDeliveryPartners() {

        return ResponseEntity.ok(
                deliveryPartnerService.getAllDeliveryPartners()
        );
    }

    // 3. Get delivery partner by ID
    @GetMapping("/{partnerId}")
    public ResponseEntity<DeliveryPartner> getDeliveryPartnerById(
            @PathVariable Long partnerId) {

        return ResponseEntity.ok(
                deliveryPartnerService.getDeliveryPartnerById(partnerId)
        );
    }

    // 4. Get delivery partners belonging to a store
    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<DeliveryPartner>> getDeliveryPartnersByStore(
            @PathVariable Long storeId) {

        return ResponseEntity.ok(
                deliveryPartnerService.getDeliveryPartnersByStore(storeId)
        );
    }

    // 5. Get available delivery partners in a store
    @GetMapping("/store/{storeId}/available")
    public ResponseEntity<List<DeliveryPartner>> getAvailableDeliveryPartners(
            @PathVariable Long storeId) {

        return ResponseEntity.ok(
                deliveryPartnerService.getAvailableDeliveryPartners(storeId)
        );
    }

    // 6. Update delivery partner availability
    @PatchMapping("/{partnerId}/availability")
    public ResponseEntity<DeliveryPartner> updateAvailability(
            @PathVariable Long partnerId,
            @RequestParam boolean available) {

        return ResponseEntity.ok(
                deliveryPartnerService.updateAvailability(
                        partnerId, available
                )
        );
    }
}