package com.ecommerce.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDTO {

    private Long addressId;

    // Client-side expected total for sanity check (server still calculates from cart)
    private Double totalAmount;

    // Payment info (mock/local only; do not store full card data)
    private String paymentMethod; // e.g. "CARD"
    private String cardNumber;
    private String cardHolderName;
    private Integer expMonth;
    private Integer expYear;
    private String cvv;
}
