
package com.quickkart.service;

import com.quickkart.entity.Product;
import com.quickkart.entity.Store;
import com.quickkart.entity.Category;

import com.quickkart.repository.ProductRepository;
import com.quickkart.repository.StoreRepository;
import com.quickkart.repository.CategoryRepository;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;

    // Constructor injection
    public ProductService(
            ProductRepository productRepository,
            StoreRepository storeRepository,
            CategoryRepository categoryRepository) {

        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
        this.categoryRepository = categoryRepository;
    }

    // Create product
    public Product createProduct(Product product) {

        if (product.getName() == null ||
                product.getName().isBlank()) {
            throw new IllegalArgumentException(
                    "Product name is required"
            );
        }

        if (product.getPrice() == null ||
                product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Product price must be greater than zero"
            );
        }

        if (product.getStockQuantity() == null ||
                product.getStockQuantity() < 0) {
            throw new IllegalArgumentException(
                    "Stock quantity cannot be negative"
            );
        }

        if (product.getStore() == null ||
                product.getStore().getId() == null) {
            throw new IllegalArgumentException(
                    "Store ID is required"
            );
        }

        if (product.getCategory() == null ||
                product.getCategory().getId() == null) {
            throw new IllegalArgumentException(
                    "Category ID is required"
            );
        }

        Long storeId = product.getStore().getId();
        Long categoryId = product.getCategory().getId();

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Store not found with ID: " + storeId
                        )
                );

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Category not found with ID: " + categoryId
                        )
                );

        product.setStore(store);
        product.setCategory(category);

        product.setName(product.getName().trim());

        return productRepository.save(product);
    }

    // Get all products
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Get product by ID
    public Product getProductById(Long id) {

        return productRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Product not found with ID: " + id
                        )
                );
    }

    // Get products by store
    public List<Product> getProductsByStore(Long storeId) {

        if (!storeRepository.existsById(storeId)) {
            throw new RuntimeException(
                    "Store not found with ID: " + storeId
            );
        }

        return productRepository.findByStoreId(storeId);
    }

    // Get products by category
    public List<Product> getProductsByCategory(Long categoryId) {

        if (!categoryRepository.existsById(categoryId)) {
            throw new RuntimeException(
                    "Category not found with ID: " + categoryId
            );
        }

        return productRepository.findByCategoryId(categoryId);
    }

    // Search products by name
    public List<Product> searchProducts(String name) {

        return productRepository
                .findByNameContainingIgnoreCase(name);
    }

    // Update product stock
    public Product updateStock(Long id, Integer quantity) {

        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException(
                    "Stock quantity cannot be negative"
            );
        }

        Product product = getProductById(id);

        product.setStockQuantity(quantity);

        return productRepository.save(product);
    }

    // Delete product
    public void deleteProduct(Long id) {

        Product product = getProductById(id);

        productRepository.delete(product);
    }
}
