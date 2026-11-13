package com.quickkart.service;

import com.quickkart.entity.DeliveryPartner;
import com.quickkart.entity.Store;
import com.quickkart.entity.User;
import com.quickkart.repository.DeliveryPartnerRepository;
import com.quickkart.repository.StoreRepository;
import com.quickkart.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DeliveryPartnerService {

    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;

    public DeliveryPartnerService(
            DeliveryPartnerRepository deliveryPartnerRepository,
            UserRepository userRepository,
            StoreRepository storeRepository) {

        this.deliveryPartnerRepository = deliveryPartnerRepository;
        this.userRepository = userRepository;
        this.storeRepository = storeRepository;
    }

    // 1. Register a delivery partner and assign them to a store
    public DeliveryPartner createDeliveryPartner(
            Long userId,
            Long storeId,
            String phoneNumber) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() ->
                        new RuntimeException("Store not found"));

        if (!store.isActive()) {
            throw new RuntimeException("Store is inactive");
        }

        if (deliveryPartnerRepository.existsByUserId(userId)) {
            throw new RuntimeException(
                    "Delivery partner profile already exists");
        }

        if (!"DELIVERY_PARTNER".equalsIgnoreCase(user.getRole())) {
            throw new RuntimeException(
                    "User must have DELIVERY_PARTNER role");
        }

        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new RuntimeException(
                    "Phone number is required");
        }

        DeliveryPartner partner = new DeliveryPartner();

        partner.setUser(user);
        partner.setStore(store);
        partner.setPhoneNumber(phoneNumber);
        partner.setAvailable(true);
        partner.setActive(true);

        return deliveryPartnerRepository.save(partner);
    }

    // 2. Get all delivery partners
    @Transactional(readOnly = true)
    public List<DeliveryPartner> getAllDeliveryPartners() {
        return deliveryPartnerRepository.findAll();
    }

    // 3. Get delivery partner by ID
    @Transactional(readOnly = true)
    public DeliveryPartner getDeliveryPartnerById(Long partnerId) {

        return deliveryPartnerRepository.findById(partnerId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Delivery partner not found"));
    }

    // 4. Get delivery partners belonging to a store
    @Transactional(readOnly = true)
    public List<DeliveryPartner> getDeliveryPartnersByStore(
            Long storeId) {

        if (!storeRepository.existsById(storeId)) {
            throw new RuntimeException("Store not found");
        }

        return deliveryPartnerRepository.findByStoreId(storeId);
    }

    // 5. Get available delivery partners in a store
    @Transactional(readOnly = true)
    public List<DeliveryPartner> getAvailableDeliveryPartners(
            Long storeId) {

        if (!storeRepository.existsById(storeId)) {
            throw new RuntimeException("Store not found");
        }

        return deliveryPartnerRepository
                .findByStoreIdAndAvailableTrueAndActiveTrue(storeId);
    }

    // 6. Update delivery partner availability
    public DeliveryPartner updateAvailability(
            Long partnerId,
            boolean available) {

        DeliveryPartner partner =
                getDeliveryPartnerById(partnerId);

        if (!partner.isActive()) {
            throw new RuntimeException(
                    "Delivery partner is inactive");
        }

        partner.setAvailable(available);

        return deliveryPartnerRepository.save(partner);
    }
}