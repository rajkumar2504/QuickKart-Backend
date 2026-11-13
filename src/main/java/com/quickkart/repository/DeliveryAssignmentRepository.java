package com.quickkart.repository;

import com.quickkart.entity.DeliveryAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryAssignmentRepository
        extends JpaRepository<DeliveryAssignment, Long> {

    // Find the delivery assignment for an order
    Optional<DeliveryAssignment> findByOrderId(Long orderId);

    // Get all assignments handled by a delivery partner
    List<DeliveryAssignment> findByDeliveryPartnerIdOrderByAssignedAtDesc(
            Long deliveryPartnerId
    );

    // Get all assignments belonging to a store's delivery partners
    List<DeliveryAssignment> findByDeliveryPartnerStoreId(Long storeId);

    // Check whether an order already has a delivery assignment
    boolean existsByOrderId(Long orderId);

    // Find unfinished deliveries for a delivery partner
    List<DeliveryAssignment> findByDeliveryPartnerIdAndDeliveredAtIsNull(
            Long deliveryPartnerId
    );

    // Find completed deliveries for a delivery partner
    List<DeliveryAssignment> findByDeliveryPartnerIdAndDeliveredAtIsNotNull(
            Long deliveryPartnerId
    );
}