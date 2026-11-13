package com.quickkart.service;

import com.quickkart.entity.Order;
import com.quickkart.entity.OrderStatus;
import com.quickkart.entity.Picker;
import com.quickkart.entity.PickerAssignment;
import com.quickkart.repository.OrderRepository;
import com.quickkart.repository.PickerAssignmentRepository;
import com.quickkart.repository.PickerRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PickerAssignmentService {

    private final PickerAssignmentRepository assignmentRepository;
    private final PickerRepository pickerRepository;
    private final OrderRepository orderRepository;

    public PickerAssignmentService(
            PickerAssignmentRepository assignmentRepository,
            PickerRepository pickerRepository,
            OrderRepository orderRepository) {

        this.assignmentRepository = assignmentRepository;
        this.pickerRepository = pickerRepository;
        this.orderRepository = orderRepository;
    }

    // 1. Assign an order to an available picker
    public PickerAssignment assignPicker(
            Long orderId,
            Long pickerId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException("Order not found"));

        Picker picker = pickerRepository.findById(pickerId)
                .orElseThrow(() ->
                        new RuntimeException("Picker not found"));

        // Validate picker
        if (!picker.isActive()) {
            throw new RuntimeException("Picker is inactive");
        }

        if (!picker.isAvailable()) {
            throw new RuntimeException("Picker is not available");
        }

        // Ensure picker belongs to the order's store
        if (!picker.getStore().getId()
                .equals(order.getStore().getId())) {

            throw new RuntimeException(
                    "Picker does not belong to this order's store");
        }

        // Validate order status
        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new RuntimeException(
                    "Only confirmed orders can be assigned to a picker");
        }

        // Prevent duplicate assignments
        if (assignmentRepository.existsByOrderId(orderId)) {
            throw new RuntimeException(
                    "This order already has a picker assignment");
        }

        // Prevent assigning another unfinished order
        List<PickerAssignment> activeAssignments =
                assignmentRepository
                        .findByPickerIdAndCompletedAtIsNull(pickerId);

        if (!activeAssignments.isEmpty()) {
            throw new RuntimeException(
                    "Picker already has an unfinished assignment");
        }

        // Create assignment
        PickerAssignment assignment = new PickerAssignment();

        assignment.setOrder(order);
        assignment.setPicker(picker);

        // Mark picker unavailable
        picker.setAvailable(false);

        // Update order status
        order.setStatus(OrderStatus.PICKER_ASSIGNED);

        pickerRepository.save(picker);
        orderRepository.save(order);

        return assignmentRepository.save(assignment);
    }

    // 2. Start picking products
    public PickerAssignment startPicking(Long assignmentId) {

        PickerAssignment assignment =
                getAssignment(assignmentId);

        Order order = assignment.getOrder();

        if (order.getStatus() != OrderStatus.PICKER_ASSIGNED) {
            throw new RuntimeException(
                    "Order is not ready for picking");
        }

        if (assignment.getStartedAt() != null) {
            throw new RuntimeException(
                    "Picking has already started");
        }

        assignment.setStartedAt(LocalDateTime.now());

        order.setStatus(OrderStatus.PICKING);

        orderRepository.save(order);

        return assignmentRepository.save(assignment);
    }

    // 3. Complete packing
    public PickerAssignment completePacking(
            Long assignmentId,
            String notes) {

        PickerAssignment assignment =
                getAssignment(assignmentId);

        Order order = assignment.getOrder();

        if (order.getStatus() != OrderStatus.PICKING) {
            throw new RuntimeException(
                    "Order must be in PICKING status before packing");
        }

        if (assignment.getStartedAt() == null) {
            throw new RuntimeException(
                    "Picking has not started");
        }

        if (assignment.getCompletedAt() != null) {
            throw new RuntimeException(
                    "Packing is already completed");
        }

        assignment.setCompletedAt(LocalDateTime.now());
        assignment.setNotes(notes);

        // Update order status
        order.setStatus(OrderStatus.PACKED);

        // Make picker available again
        Picker picker = assignment.getPicker();
        picker.setAvailable(true);

        orderRepository.save(order);
        pickerRepository.save(picker);

        return assignmentRepository.save(assignment);
    }

    // 4. Get assignment by ID
    @Transactional(readOnly = true)
    public PickerAssignment getAssignment(Long assignmentId) {

        return assignmentRepository.findById(assignmentId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Picker assignment not found"));
    }

    // 5. Get assignment by order ID
    @Transactional(readOnly = true)
    public PickerAssignment getAssignmentByOrder(Long orderId) {

        return assignmentRepository.findByOrderId(orderId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "No picker assignment found for this order"));
    }

    // 6. Get a picker's assignment history
    @Transactional(readOnly = true)
    public List<PickerAssignment> getPickerAssignments(
            Long pickerId) {

        if (!pickerRepository.existsById(pickerId)) {
            throw new RuntimeException("Picker not found");
        }

        return assignmentRepository
                .findByPickerIdOrderByAssignedAtDesc(pickerId);
    }
}
// 7. Get all active assignments for a picker
@Transactional(readOnly = true)
public List<PickerAssignment> getActiveAssignments(Long pickerId) {

    if (!pickerRepository.existsById(pickerId)) {
        throw new RuntimeException("Picker not found");
    }

    return assignmentRepository
            .findByPickerIdAndCompletedAtIsNull(pickerId);
}

// 8. Get completed assignments for a picker
@Transactional(readOnly = true)
public List<PickerAssignment> getCompletedAssignments(Long pickerId) {

    if (!pickerRepository.existsById(pickerId)) {
        throw new RuntimeException("Picker not found");
    }

    return assignmentRepository
            .findByPickerIdOrderByAssignedAtDesc(pickerId)
            .stream()
            .filter(assignment -> assignment.getCompletedAt() != null)
            .toList();
}

// 9. Get assignments waiting to be picked
@Transactional(readOnly = true)
public List<PickerAssignment> getPendingAssignments(Long pickerId) {

    if (!pickerRepository.existsById(pickerId)) {
        throw new RuntimeException("Picker not found");
    }

    return assignmentRepository
            .findByPickerIdAndCompletedAtIsNull(pickerId)
            .stream()
            .filter(assignment -> assignment.getStartedAt() == null)
            .toList();
}