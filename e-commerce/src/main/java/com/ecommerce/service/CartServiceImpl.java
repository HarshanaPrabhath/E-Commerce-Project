package com.ecommerce.service;

import com.ecommerce.exceptions.ApiException;
import com.ecommerce.exceptions.ResourceNotFoundException;
import com.ecommerce.model.Cart;
import com.ecommerce.model.CartItems;
import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.payload.CartDTO;
import com.ecommerce.payload.ProductDTO;
import com.ecommerce.repositories.CartItemRepository;
import com.ecommerce.repositories.CartRepository;
import com.ecommerce.repositories.ProductRepository;
import com.ecommerce.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;


@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService{

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final ModelMapper modelMapper;
    @Override
    public CartDTO addProductToCart(Integer productId, Integer quantity) {

        //find logged-in user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        //find actual user in user table
        User user = userRepository.findByEmail(userEmail).
                orElseThrow(() -> new RuntimeException("User Not Found"));

        //if user hasn't a cart then create a cart for user
        Cart cart = cartRepository.findByUser(user).orElseGet(
                () -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setTotalPrice(0.0);
                    return cartRepository.save(newCart);
                });

        //find the correct product from db
        Product product = productRepository.findById(Long.valueOf(productId))
                .orElseThrow(() -> new ResourceNotFoundException("Product","Product", String.valueOf(productId)));

        //validations
        CartItems cartItems = cartItemRepository.findCartItemsByProductIdAndCartId(
                cart.getCartId(),
                productId
        );

        if(cartItems != null){
            throw new ApiException("Product" + product.getProductName() + "already exist in the cart !!!" );
        }

        if(product.getQuantity() ==0){
            throw new ApiException(product.getProductName() + "is not available");
        }

        if(product.getQuantity() < quantity){
            throw new ApiException("please, make an order of the " + product.getProductName()+
                    "less than or equal to the quantity " + product.getQuantity());
        }

        // create cart item
        CartItems cartItem = new CartItems();
        cartItem.setProduct(product);
        cartItem.setCart_id(cart.getCartId());
        cartItem.setDiscount(product.getDiscount());
        cartItem.setPrice(product.getSpecialPrice());
        cartItem.setQuantity(quantity);


        //save cart item
        cartItemRepository.save(cartItem);

        //waiting for order placement to confirm this
        product.setQuantity(product.getQuantity());

        cart.setTotalPrice(cart.getTotalPrice() + product.getSpecialPrice()*quantity);

        cartRepository.save(cart);

        CartDTO cartDTO = modelMapper.map(cart,CartDTO.class);

        List<CartItems> cartItemsList = cart.getCartItems();

        Stream<ProductDTO> productDTOStream = cartItemsList.stream().map(item -> {
            ProductDTO map =  modelMapper.map(item.getProduct(),ProductDTO.class);
            map.setQuantity(item.getQuantity());
            return map;
        });

        cartDTO.setProduct(productDTOStream.toList());

        return cartDTO;
    }
}
