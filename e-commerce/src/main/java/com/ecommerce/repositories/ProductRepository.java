package com.ecommerce.repositories;

import com.ecommerce.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategory_CategoryIdOrderByPriceAsc(Long categoryId);
    List<Product> findByProductNameContainingIgnoreCase(String keyword);
}
