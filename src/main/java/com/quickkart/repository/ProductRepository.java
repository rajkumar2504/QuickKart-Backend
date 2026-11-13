
package com.quickkart.repository;

import com.quickkart.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository
        extends JpaRepository<Product, Long> {

    // Find products by store
    List<Product> findByStoreId(Long storeId);

    // Find products by category
    List<Product> findByCategoryId(Long categoryId);

    // Find products by store and category
    List<Product> findByStoreIdAndCategoryId(
            Long storeId,
            Long categoryId
    );

    // Search products by name
    List<Product> findByNameContainingIgnoreCase(String name);

    // Find active products
    List<Product> findByActiveTrue();
}
