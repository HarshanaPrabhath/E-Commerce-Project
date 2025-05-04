package com.ecommerce.payload;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {

    private Long productId;

    @NotBlank
    @Size(min =3, message = "product name must have 3 characters as min")
    private String productName;

    private String image;

    @NotBlank
    @Size(min = 10, message = "product description must have 10 characters as min")
    private String description;

    @NotNull
    @Positive
    private Integer quantity;

    @NotNull
    @Positive
    private double price;
    private double discount;
    private double specialPrice;
}
