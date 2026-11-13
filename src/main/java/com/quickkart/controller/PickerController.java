package com.quickkart.controller;

import com.quickkart.entity.Picker;
import com.quickkart.service.PickerService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pickers")
public class PickerController {

    private final PickerService pickerService;

    public PickerController(PickerService pickerService) {
        this.pickerService = pickerService;
    }

    // 1. Register a picker and assign them to a store
    @PostMapping
    public ResponseEntity<Picker> createPicker(
            @RequestParam Long userId,
            @RequestParam Long storeId) {

        Picker picker = pickerService.createPicker(userId, storeId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(picker);
    }

    // 2. Get all pickers
    @GetMapping
    public ResponseEntity<List<Picker>> getAllPickers() {

        return ResponseEntity.ok(
                pickerService.getAllPickers()
        );
    }

    // 3. Get picker by ID
    @GetMapping("/{pickerId}")
    public ResponseEntity<Picker> getPickerById(
            @PathVariable Long pickerId) {

        return ResponseEntity.ok(
                pickerService.getPickerById(pickerId)
        );
    }

    // 4. Get pickers belonging to a store
    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<Picker>> getPickersByStore(
            @PathVariable Long storeId) {

        return ResponseEntity.ok(
                pickerService.getPickersByStore(storeId)
        );
    }

    // 5. Get available pickers in a store
    @GetMapping("/store/{storeId}/available")
    public ResponseEntity<List<Picker>> getAvailablePickers(
            @PathVariable Long storeId) {

        return ResponseEntity.ok(
                pickerService.getAvailablePickers(storeId)
        );
    }

    // 6. Update picker availability
    @PatchMapping("/{pickerId}/availability")
    public ResponseEntity<Picker> updateAvailability(
            @PathVariable Long pickerId,
            @RequestParam boolean available) {

        return ResponseEntity.ok(
                pickerService.updateAvailability(pickerId, available)
        );
    }
}