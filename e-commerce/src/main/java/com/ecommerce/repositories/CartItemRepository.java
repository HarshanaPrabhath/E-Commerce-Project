package com.ecommerce.repositories;

import com.ecommerce.model.CartItems;
import com.ecommerce.payload.CartDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CartItemRepository extends JpaRepository<CartItems,Integer> {


    @Query("SELECT ci FROM CartItems ci WHERE ci.cart.cartId = ?1 AND ci.product.productId=?2")
    CartItems findCartItemsByProductIdAndCartId(Integer cartId, Integer productId);

    @Modifying
    @Query("DELETE FROM CartItems ci WHERE ci.cart.cartId = ?1 AND ci.product.productId = ?2")
    void deleteCartItemByProductIdAndCartId(Integer cartId, Integer productId);

}
