package com.quickkart.service;

import com.quickkart.entity.Picker;
import com.quickkart.entity.Store;
import com.quickkart.entity.User;
import com.quickkart.repository.PickerRepository;
import com.quickkart.repository.StoreRepository;
import com.quickkart.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PickerService {

    private final PickerRepository pickerRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;

    public PickerService(
            PickerRepository pickerRepository,
            UserRepository userRepository,
            StoreRepository storeRepository) {

        this.pickerRepository = pickerRepository;
        this.userRepository = userRepository;
        this.storeRepository = storeRepository;
    }

    // 1. Register a picker and assign them to a store
    public Picker createPicker(Long userId, Long storeId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() ->
                        new RuntimeException("Store not found"));

        if (!store.isActive()) {
            throw new RuntimeException("Store is inactive");
        }

        if (pickerRepository.existsByUserId(userId)) {
            throw new RuntimeException(
                    "Picker profile already exists for this user");
        }

        if (!"PICKER".equalsIgnoreCase(user.getRole())) {
            throw new RuntimeException(
                    "User must have PICKER role");
        }

        Picker picker = new Picker();
        picker.setUser(user);
        picker.setStore(store);
        picker.setAvailable(true);
        picker.setActive(true);

        return pickerRepository.save(picker);
    }

    // 2. Get all pickers
    @Transactional(readOnly = true)
    public List<Picker> getAllPickers() {
        return pickerRepository.findAll();
    }

    // 3. Get picker by ID
    @Transactional(readOnly = true)
    public Picker getPickerById(Long pickerId) {

        return pickerRepository.findById(pickerId)
                .orElseThrow(() ->
                        new RuntimeException("Picker not found"));
    }

    // 4. Get pickers belonging to a store
    @Transactional(readOnly = true)
    public List<Picker> getPickersByStore(Long storeId) {

        if (!storeRepository.existsById(storeId)) {
            throw new RuntimeException("Store not found");
        }

        return pickerRepository.findByStoreId(storeId);
    }

    // 5. Get available pickers in a store
    @Transactional(readOnly = true)
    public List<Picker> getAvailablePickers(Long storeId) {

        if (!storeRepository.existsById(storeId)) {
            throw new RuntimeException("Store not found");
        }

        return pickerRepository
                .findByStoreIdAndAvailableTrueAndActiveTrue(storeId);
    }

    // 6. Update picker availability
    public Picker updateAvailability(
            Long pickerId,
            boolean available) {

        Picker picker = getPickerById(pickerId);

        if (!picker.isActive()) {
            throw new RuntimeException("Picker is inactive");
        }

        picker.setAvailable(available);

        return pickerRepository.save(picker);
    }
}