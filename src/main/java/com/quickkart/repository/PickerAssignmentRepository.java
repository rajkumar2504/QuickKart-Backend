package com.quickkart.repository;

import com.quickkart.entity.PickerAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PickerAssignmentRepository
        extends JpaRepository<PickerAssignment, Long> {

    // Find assignment for a particular order
    Optional<PickerAssignment> findByOrderId(Long orderId);

    // Find all assignments handled by a picker
    List<PickerAssignment> findByPickerIdOrderByAssignedAtDesc(
            Long pickerId
    );

    // Find all assignments belonging to a store's pickers
    List<PickerAssignment> findByPickerStoreId(Long storeId);

    // Check whether an order already has a picker assignment
    boolean existsByOrderId(Long orderId);

    // Find assignments currently being handled by a picker
    List<PickerAssignment> findByPickerIdAndCompletedAtIsNull(
            Long pickerId
    );
}