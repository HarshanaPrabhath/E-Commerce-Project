package com.ecommerce.repositories;

import com.ecommerce.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByCategory_CategoryId(Long categoryId, Pageable pageable);
    Page<Product> findByProductNameContainingIgnoreCase(String keyword,Pageable pageDetails);
    Product findByProductName(String name);
}
