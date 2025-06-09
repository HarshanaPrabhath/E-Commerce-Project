package com.ecommerce.repositories;

import com.ecommerce.model.CartItems;
import com.ecommerce.payload.CartDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface CartItemRepository extends JpaRepository<CartItems,Integer> {

    CartDTO addProductToCart(Integer product,Integer quantity);

    @Query("SELECT ci FROM CartItems ci WHERE ci.cart.cartId = ?1 AND ci.product.productId=?2")
    CartItems findCartItemsByProductIdAndCartId(Integer cartId, Integer productId);
}
