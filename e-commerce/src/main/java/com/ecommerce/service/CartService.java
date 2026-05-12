package com.ecommerce.service;

import com.ecommerce.payload.CartDTO;

import java.util.List;

public interface CartService {

    CartDTO addProductToCart(Integer productId, Integer quantity);

    List<CartDTO> getAllCarts();

    CartDTO getCart(String emailId, Integer cartId);

    CartDTO updateCartQuantityInCart(Integer productId, Integer quantity);

    String deleteProductFromCart(Integer cartId, Integer productId);

    void updateProductInCarts(Integer cartId, Long productId);

    // NEW: clears all items from a cart and resets total to 0
    void clearCart(Integer cartId);
}