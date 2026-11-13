
package com.quickkart.service;

import com.quickkart.entity.Store;
import com.quickkart.repository.StoreRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StoreService {

    private final StoreRepository storeRepository;

    // Constructor injection
    public StoreService(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    // Create a new store
    public Store createStore(Store store) {
        return storeRepository.save(store);
    }

    // Get all stores
    public List<Store> getAllStores() {
        return storeRepository.findAll();
    }

    // Get store by ID
    public Store getStoreById(Long id) {
        return storeRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Store not found with ID: " + id));
    }

    // Delete store
    public void deleteStore(Long id) {

        if (!storeRepository.existsById(id)) {
            throw new RuntimeException("Store not found with ID: " + id);
        }

        storeRepository.deleteById(id);
    }
}
