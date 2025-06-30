package com.ecommerce.controller;


import com.ecommerce.model.Cart;
import com.ecommerce.payload.CartDTO;
import com.ecommerce.repositories.CartItemRepository;
import com.ecommerce.repositories.CartRepository;
import com.ecommerce.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final CartRepository cartRepository;

    @PostMapping(value = "/carts/products/{product}/quantity/{quantity}")
    public ResponseEntity<CartDTO> addProductToCart(@PathVariable Integer product,
                                                    @PathVariable Integer quantity){

       CartDTO cartDTO = cartService.addProductToCart(product,quantity);

        return new ResponseEntity<CartDTO>(cartDTO,HttpStatus.CREATED);
    }

    @GetMapping(value="/carts")
    public ResponseEntity<List<CartDTO>> getAllCarts(){
        List<CartDTO> cartDTOS = cartService.getAllCarts();

        return new ResponseEntity<List<CartDTO>>(cartDTOS,HttpStatus.OK);
    }

    @GetMapping(value="/carts/users/cart")
    public ResponseEntity<CartDTO> getCart(){
       Authentication authentication =  SecurityContextHolder.getContext().getAuthentication();
       String emailId = authentication.getName();

        Cart cart = cartRepository.findCartByEmail(emailId);
        Integer cartId = cart.getCartId();

        CartDTO cartDTO = cartService.getCart(emailId,cartId);
        return new ResponseEntity<CartDTO>(cartDTO,HttpStatus.OK);
    }

    @GetMapping(value ="carts/products/{productId}/quantity/{operation}")
    public ResponseEntity<CartDTO> updateCartProduct(@PathVariable Integer productId,
                                                     @PathVariable String operation){
        CartDTO cartDTO = cartService.updateCartQuantityInCart(productId,operation.equalsIgnoreCase("delete")?-1 : 1);

        return new ResponseEntity<CartDTO>(cartDTO,HttpStatus.OK);
    }

    @DeleteMapping(value="/carts/{cartId}/product/{productId}")
    public ResponseEntity<String> deleteProductFromCart(@PathVariable Integer cartId,
                                                        @PathVariable Integer productId){
        String status = cartService.deleteProductFromCart(cartId,productId);

        return new ResponseEntity<String>(status,HttpStatus.OK);
    }
}
