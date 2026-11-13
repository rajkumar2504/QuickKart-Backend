package com.quickkart.controller;

import com.quickkart.dto.DeliveryDashboardResponse;
import com.quickkart.entity.DeliveryAssignment;
import com.quickkart.service.DeliveryAssignmentService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delivery-assignments")
public class DeliveryAssignmentController {

    private final DeliveryAssignmentService assignmentService;

    public DeliveryAssignmentController(
            DeliveryAssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    // 1. Assign a delivery partner to an order
    @PostMapping("/assign")
    public ResponseEntity<DeliveryAssignment> assignDeliveryPartner(
            @RequestParam Long orderId,
            @RequestParam Long partnerId) {

        DeliveryAssignment assignment =
                assignmentService.assignDeliveryPartner(
                        orderId, partnerId
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(assignment);
    }

    // 2. Pick up order from store
    @PatchMapping("/{assignmentId}/pickup")
    public ResponseEntity<DeliveryAssignment> pickUpOrder(
            @PathVariable Long assignmentId) {

        return ResponseEntity.ok(
                assignmentService.pickUpOrder(assignmentId)
        );
    }

    // 3. Start delivery
    @PatchMapping("/{assignmentId}/start")
    public ResponseEntity<DeliveryAssignment> startDelivery(
            @PathVariable Long assignmentId) {

        return ResponseEntity.ok(
                assignmentService.startDelivery(assignmentId)
        );
    }

    // 4. Complete delivery
    @PatchMapping("/{assignmentId}/complete")
    public ResponseEntity<DeliveryAssignment> completeDelivery(
            @PathVariable Long assignmentId,
            @RequestParam(required = false) String notes) {

        return ResponseEntity.ok(
                assignmentService.completeDelivery(
                        assignmentId, notes
                )
        );
    }

    // 5. Get assignment by ID
    @GetMapping("/{assignmentId}")
    public ResponseEntity<DeliveryAssignment> getAssignment(
            @PathVariable Long assignmentId) {

        return ResponseEntity.ok(
                assignmentService.getAssignment(assignmentId)
        );
    }

    // 6. Get assignment by order ID
    @GetMapping("/order/{orderId}")
    public ResponseEntity<DeliveryAssignment> getAssignmentByOrder(
            @PathVariable Long orderId) {

        return ResponseEntity.ok(
                assignmentService.getAssignmentByOrder(orderId)
        );
    }

    // 7. Get delivery partner's assignment history
    @GetMapping("/partner/{partnerId}")
    public ResponseEntity<List<DeliveryAssignment>> getPartnerAssignments(
            @PathVariable Long partnerId) {

        return ResponseEntity.ok(
                assignmentService.getPartnerAssignments(partnerId)
        );
    }

    // 8. Get active deliveries
    @GetMapping("/partner/{partnerId}/active")
    public ResponseEntity<List<DeliveryAssignment>> getActiveDeliveries(
            @PathVariable Long partnerId) {

        return ResponseEntity.ok(
                assignmentService.getActiveDeliveries(partnerId)
        );
    }

    // 9. Get completed deliveries
    @GetMapping("/partner/{partnerId}/completed")
    public ResponseEntity<List<DeliveryAssignment>> getCompletedDeliveries(
            @PathVariable Long partnerId) {

        return ResponseEntity.ok(
                assignmentService.getCompletedDeliveries(partnerId)
        );
    }
}
// 10. Get delivery partner dashboard
@GetMapping("/partner/{partnerId}/dashboard")
public ResponseEntity<DeliveryDashboardResponse> getDashboard(
        @PathVariable Long partnerId) {

    return ResponseEntity.ok(
            assignmentService.getDashboard(partnerId)
    );
}