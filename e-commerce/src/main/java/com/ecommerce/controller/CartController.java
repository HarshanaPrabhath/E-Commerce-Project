package com.ecommerce.controller;


import com.ecommerce.payload.CartDTO;
import com.ecommerce.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping(value = "/carts/products/{product}/quantity/{quantity}")
    public ResponseEntity<CartDTO> addProductToCart(@PathVariable Integer product,
                                                    @PathVariable Integer quantity){

       CartDTO cartDTO = cartService.addProductToCart(product,quantity);

        return new ResponseEntity<CartDTO>(cartDTO,HttpStatus.CREATED);
    }
}
