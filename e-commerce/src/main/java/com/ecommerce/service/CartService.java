package com.ecommerce.service;

import com.ecommerce.payload.CartDTO;
import jakarta.transaction.Transactional;

import java.util.List;

public interface CartService {

    CartDTO addProductToCart(Integer product, Integer quantity);

    List<CartDTO> getAllCarts();
    CartDTO getCart(String emailId,Integer cartId);

    @Transactional
    CartDTO updateCartQuantityInCart(Integer productId, Integer quantity);

    String deleteProductFromCart(Integer cartId, Integer productId);

    void updateProductInCarts(Integer cartId, Long productId);
}
