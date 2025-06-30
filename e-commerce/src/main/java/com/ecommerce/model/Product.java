package com.ecommerce.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long productId;
    private String productName;
    private String description;
    private Integer quantity;
    private String image;
    private double price;
    private double discount;
    private double specialPrice;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User user;

    @ManyToOne
    @JoinColumn(name ="category_id")
    private Category category;

    @OneToMany(mappedBy = "product", cascade = {CascadeType.MERGE,CascadeType.PERSIST},fetch= FetchType.EAGER)
    private List<CartItems> cartItems = new ArrayList<>();
}
