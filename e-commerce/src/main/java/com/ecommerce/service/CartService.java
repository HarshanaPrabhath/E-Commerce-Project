package com.ecommerce.service;

import com.ecommerce.payload.CartDTO;

public interface CartService {

    CartDTO addProductToCart(Integer product, Integer quantity);
}
