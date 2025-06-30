package com.ecommerce.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItems {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private double discount;
    private double price;
    private Integer quantity;
    private Integer totalPrice;

    @ManyToOne
    @JoinColumn(name = "cart_id")  // ✅ Correct FK to Cart
    private Cart cart;             // ✅ This is what mappedBy refers to

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}


