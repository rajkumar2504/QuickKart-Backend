package com.quickkart.service;

import com.quickkart.entity.DeliveryAssignment;
import com.quickkart.entity.DeliveryPartner;
import com.quickkart.entity.Order;
import com.quickkart.entity.OrderStatus;

import com.quickkart.repository.DeliveryAssignmentRepository;
import com.quickkart.repository.DeliveryPartnerRepository;
import com.quickkart.repository.OrderRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class DeliveryAssignmentService {

    private final DeliveryAssignmentRepository assignmentRepository;
    private final DeliveryPartnerRepository partnerRepository;
    private final OrderRepository orderRepository;

    public DeliveryAssignmentService(
            DeliveryAssignmentRepository assignmentRepository,
            DeliveryPartnerRepository partnerRepository,
            OrderRepository orderRepository) {

        this.assignmentRepository = assignmentRepository;
        this.partnerRepository = partnerRepository;
        this.orderRepository = orderRepository;
    }

    // 1. Assign a delivery partner to a packed order
    public DeliveryAssignment assignDeliveryPartner(
            Long orderId,
            Long partnerId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException("Order not found"));

        DeliveryPartner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() ->
                        new RuntimeException("Delivery partner not found"));

        // Validate partner
        if (!partner.isActive()) {
            throw new RuntimeException(
                    "Delivery partner is inactive");
        }

        if (!partner.isAvailable()) {
            throw new RuntimeException(
                    "Delivery partner is not available");
        }

        // Ensure partner belongs to the order's store
        if (!partner.getStore().getId()
                .equals(order.getStore().getId())) {

            throw new RuntimeException(
                    "Delivery partner does not belong to this order's store");
        }

        // Only packed orders can be assigned
        if (order.getStatus() != OrderStatus.PACKED) {
            throw new RuntimeException(
                    "Only packed orders can be assigned for delivery");
        }

        // Prevent duplicate delivery assignments
        if (assignmentRepository.existsByOrderId(orderId)) {
            throw new RuntimeException(
                    "This order already has a delivery assignment");
        }

        // Prevent multiple unfinished deliveries for one partner
        List<DeliveryAssignment> activeAssignments =
                assignmentRepository
                        .findByDeliveryPartnerIdAndDeliveredAtIsNull(
                                partnerId
                        );

        if (!activeAssignments.isEmpty()) {
            throw new RuntimeException(
                    "Delivery partner already has an unfinished delivery");
        }

        // Create delivery assignment
        DeliveryAssignment assignment = new DeliveryAssignment();

        assignment.setOrder(order);
        assignment.setDeliveryPartner(partner);

        // Mark partner unavailable
        partner.setAvailable(false);

        // Update order status
        order.setStatus(OrderStatus.DELIVERY_ASSIGNED);

        partnerRepository.save(partner);
        orderRepository.save(order);

        return assignmentRepository.save(assignment);
    }

    // 2. Pick up order from store
    public DeliveryAssignment pickUpOrder(Long assignmentId) {

        DeliveryAssignment assignment =
                getAssignment(assignmentId);

        Order order = assignment.getOrder();

        if (order.getStatus() != OrderStatus.DELIVERY_ASSIGNED) {
            throw new RuntimeException(
                    "Order is not ready for pickup");
        }

        if (assignment.getPickedUpAt() != null) {
            throw new RuntimeException(
                    "Order has already been picked up");
        }

        assignment.setPickedUpAt(LocalDateTime.now());

        return assignmentRepository.save(assignment);
    }

    // 3. Start delivery
    public DeliveryAssignment startDelivery(Long assignmentId) {

        DeliveryAssignment assignment =
                getAssignment(assignmentId);

        Order order = assignment.getOrder();

        if (order.getStatus() != OrderStatus.DELIVERY_ASSIGNED) {
            throw new RuntimeException(
                    "Order is not assigned for delivery");
        }

        if (assignment.getPickedUpAt() == null) {
            throw new RuntimeException(
                    "Pick up the order before starting delivery");
        }

        if (assignment.getStartedAt() != null) {
            throw new RuntimeException(
                    "Delivery has already started");
        }

        assignment.setStartedAt(LocalDateTime.now());

        order.setStatus(OrderStatus.OUT_FOR_DELIVERY);

        orderRepository.save(order);

        return assignmentRepository.save(assignment);
    }

    // 4. Complete delivery
    public DeliveryAssignment completeDelivery(
            Long assignmentId,
            String notes) {

        DeliveryAssignment assignment =
                getAssignment(assignmentId);

        Order order = assignment.getOrder();

        if (order.getStatus() != OrderStatus.OUT_FOR_DELIVERY) {
            throw new RuntimeException(
                    "Order is not out for delivery");
        }

        if (assignment.getStartedAt() == null) {
            throw new RuntimeException(
                    "Delivery has not started");
        }

        if (assignment.getDeliveredAt() != null) {
            throw new RuntimeException(
                    "Order has already been delivered");
        }

        // Record delivery completion
        assignment.setDeliveredAt(LocalDateTime.now());
        assignment.setNotes(notes);

        // Update order status
        order.setStatus(OrderStatus.DELIVERED);

        // Make delivery partner available again
        DeliveryPartner partner = assignment.getDeliveryPartner();
        partner.setAvailable(true);

        orderRepository.save(order);
        partnerRepository.save(partner);

        return assignmentRepository.save(assignment);
    }

    // 5. Get assignment by ID
    @Transactional(readOnly = true)
    public DeliveryAssignment getAssignment(Long assignmentId) {

        return assignmentRepository.findById(assignmentId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Delivery assignment not found"));
    }

    // 6. Get assignment by order ID
    @Transactional(readOnly = true)
    public DeliveryAssignment getAssignmentByOrder(Long orderId) {

        return assignmentRepository.findByOrderId(orderId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "No delivery assignment found for this order"));
    }

    // 7. Get delivery partner's assignment history
    @Transactional(readOnly = true)
    public List<DeliveryAssignment> getPartnerAssignments(
            Long partnerId) {

        if (!partnerRepository.existsById(partnerId)) {
            throw new RuntimeException(
                    "Delivery partner not found");
        }

        return assignmentRepository
                .findByDeliveryPartnerIdOrderByAssignedAtDesc(partnerId);
    }

    // 8. Get unfinished deliveries
    @Transactional(readOnly = true)
    public List<DeliveryAssignment> getActiveDeliveries(
            Long partnerId) {

        if (!partnerRepository.existsById(partnerId)) {
            throw new RuntimeException(
                    "Delivery partner not found");
        }

        return assignmentRepository
                .findByDeliveryPartnerIdAndDeliveredAtIsNull(partnerId);
    }

    // 9. Get completed deliveries
    @Transactional(readOnly = true)
    public List<DeliveryAssignment> getCompletedDeliveries(
            Long partnerId) {

        if (!partnerRepository.existsById(partnerId)) {
            throw new RuntimeException(
                    "Delivery partner not found");
        }

        return assignmentRepository
                .findByDeliveryPartnerIdAndDeliveredAtIsNotNull(partnerId);
    }
}

// 10. Get delivery partner dashboard summary
@Transactional(readOnly = true)
public DeliveryDashboardResponse getDashboard(Long partnerId) {

    DeliveryPartner partner = partnerRepository.findById(partnerId)
            .orElseThrow(() ->
                    new RuntimeException("Delivery partner not found"));

    List<DeliveryAssignment> allAssignments =
            assignmentRepository
                    .findByDeliveryPartnerIdOrderByAssignedAtDesc(
                            partnerId
                    );

    List<DeliveryAssignment> activeAssignments =
            assignmentRepository
                    .findByDeliveryPartnerIdAndDeliveredAtIsNull(
                            partnerId
                    );

    List<DeliveryAssignment> completedAssignments =
            assignmentRepository
                    .findByDeliveryPartnerIdAndDeliveredAtIsNotNull(
                            partnerId
                    );

    return new DeliveryDashboardResponse(
            partnerId,
            allAssignments.size(),
            activeAssignments.size(),
            completedAssignments.size(),
            partner.isAvailable()
    );
}