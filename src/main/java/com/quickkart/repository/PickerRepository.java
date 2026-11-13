package com.quickkart.repository;

import com.quickkart.entity.Picker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PickerRepository extends JpaRepository<Picker, Long> {

    // Find a picker using their user account ID
    Optional<Picker> findByUserId(Long userId);

    // Find all pickers assigned to a particular store
    List<Picker> findByStoreId(Long storeId);

    // Find available pickers in a particular store
    List<Picker> findByStoreIdAndAvailableTrueAndActiveTrue(Long storeId);

    // Check whether a user already has a picker profile
    boolean existsByUserId(Long userId);
}