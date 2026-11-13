package com.quickkart.repository;

import com.quickkart.entity.DeliveryPartner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryPartnerRepository
        extends JpaRepository<DeliveryPartner, Long> {

    // Find a delivery partner using their user account ID
    Optional<DeliveryPartner> findByUserId(Long userId);

    // Find all delivery partners assigned to a store
    List<DeliveryPartner> findByStoreId(Long storeId);

    // Find available and active delivery partners in a store
    List<DeliveryPartner> findByStoreIdAndAvailableTrueAndActiveTrue(
            Long storeId
    );

    // Check whether a user already has a delivery partner profile
    boolean existsByUserId(Long userId);
}