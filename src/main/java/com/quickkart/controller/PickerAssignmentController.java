package com.quickkart.controller;

import com.quickkart.entity.PickerAssignment;
import com.quickkart.service.PickerAssignmentService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/picker-assignments")
public class PickerAssignmentController {

    private final PickerAssignmentService assignmentService;

    public PickerAssignmentController(
            PickerAssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    // 1. Assign a picker to an order
    @PostMapping("/assign")
    public ResponseEntity<PickerAssignment> assignPicker(
            @RequestParam Long orderId,
            @RequestParam Long pickerId) {

        PickerAssignment assignment =
                assignmentService.assignPicker(orderId, pickerId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(assignment);
    }

    // 2. Start picking products
    @PatchMapping("/{assignmentId}/start")
    public ResponseEntity<PickerAssignment> startPicking(
            @PathVariable Long assignmentId) {

        return ResponseEntity.ok(
                assignmentService.startPicking(assignmentId)
        );
    }

    // 3. Complete packing
    @PatchMapping("/{assignmentId}/complete")
    public ResponseEntity<PickerAssignment> completePacking(
            @PathVariable Long assignmentId,
            @RequestParam(required = false) String notes) {

        return ResponseEntity.ok(
                assignmentService.completePacking(
                        assignmentId, notes
                )
        );
    }

    // 4. Get assignment by ID
    @GetMapping("/{assignmentId}")
    public ResponseEntity<PickerAssignment> getAssignment(
            @PathVariable Long assignmentId) {

        return ResponseEntity.ok(
                assignmentService.getAssignment(assignmentId)
        );
    }

    // 5. Get assignment by order ID
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PickerAssignment> getAssignmentByOrder(
            @PathVariable Long orderId) {

        return ResponseEntity.ok(
                assignmentService.getAssignmentByOrder(orderId)
        );
    }

    // 6. Get a picker's assignment history
    @GetMapping("/picker/{pickerId}")
    public ResponseEntity<List<PickerAssignment>> getPickerAssignments(
            @PathVariable Long pickerId) {

        return ResponseEntity.ok(
                assignmentService.getPickerAssignments(pickerId)
        );
    }
}
// 7. Get all active assignments
@GetMapping("/picker/{pickerId}/active")
public ResponseEntity<List<PickerAssignment>> getActiveAssignments(
        @PathVariable Long pickerId) {

    return ResponseEntity.ok(
            assignmentService.getActiveAssignments(pickerId)
    );
}

// 8. Get completed assignments
@GetMapping("/picker/{pickerId}/completed")
public ResponseEntity<List<PickerAssignment>> getCompletedAssignments(
        @PathVariable Long pickerId) {

    return ResponseEntity.ok(
            assignmentService.getCompletedAssignments(pickerId)
    );
}

// 9. Get pending assignments
@GetMapping("/picker/{pickerId}/pending")
public ResponseEntity<List<PickerAssignment>> getPendingAssignments(
        @PathVariable Long pickerId) {

    return ResponseEntity.ok(
            assignmentService.getPendingAssignments(pickerId)
    );
}