
package com.quickkart.controller;

import com.quickkart.entity.Store;
import com.quickkart.service.StoreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    // Create a new store
    @PostMapping
    public ResponseEntity<Store> createStore(
            @RequestBody Store store) {

        Store savedStore = storeService.createStore(store);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedStore);
    }

    // Get all stores
    @GetMapping
    public ResponseEntity<List<Store>> getAllStores() {

        return ResponseEntity.ok(
                storeService.getAllStores()
        );
    }

    // Get store by ID
    @GetMapping("/{id}")
    public ResponseEntity<Store> getStoreById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                storeService.getStoreById(id)
        );
    }

    // Delete store
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteStore(
            @PathVariable Long id) {

        storeService.deleteStore(id);

        return ResponseEntity.ok("Store deleted successfully");
    }
}
