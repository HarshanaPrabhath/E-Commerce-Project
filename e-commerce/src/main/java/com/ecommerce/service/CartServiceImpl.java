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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
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
        cartItem.setCart(cart);
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

    public List<CartDTO> getAllCarts() {

        List<Cart> carts = cartRepository.findAll();

        if (carts.isEmpty()) {
            throw new ApiException("No Cart Exist");
        }

        List<CartDTO> cartDTOS = carts
                .stream()
                .map(cart -> {
                    CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
                    System.out.println(cartDTO.toString());

                    cart.getCartItems().forEach(c -> c.getProduct().setQuantity(c.getQuantity()));
                    List<ProductDTO> productDTOList = cart.getCartItems()
                            .stream()
                            .map(item -> {
                                ProductDTO productDTO = modelMapper.map(item.getProduct(), ProductDTO.class);
                                return productDTO;
                            })
                            .collect(Collectors.toList());

                    cartDTO.setProduct(productDTOList);
                    return cartDTO;
                })
                .collect(Collectors.toList());

        return cartDTOS;
    }

    @Override
    public CartDTO getCart(String emailId, Integer cartId) {

        Cart cart = cartRepository.getCartByEmailAndCartId(emailId,cartId);

        if(cart == null){
            throw new ResourceNotFoundException("Cart","cartId",cartId.toString());
        }

        cart.getCartItems().forEach(c -> c.getProduct().setQuantity(c.getQuantity()));
        List<ProductDTO> productDTOS = cart.getCartItems()
                .stream()
                .map(cartItem -> {
                    return modelMapper.map(cartItem.getProduct(),ProductDTO.class);
                })
                .collect(Collectors.toList());


        CartDTO cartDTO = modelMapper.map(cart,CartDTO.class);
        cartDTO.setProduct(productDTOS);

        return cartDTO;
    }

    @Override
    @Transactional
    public CartDTO updateCartQuantityInCart(Integer productId, Integer quantity) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Cart cart = cartRepository.findCartByEmail(email);

        if (cart == null) {
            throw new ResourceNotFoundException("Cart", "email", email);
        }

        Product product = productRepository.findById(Long.valueOf(productId)).orElseThrow(
                ()-> new ResourceNotFoundException("Product","productId",productId.toString())
        );

        if(product.getQuantity() == 0){
            throw new ApiException(product.getProductName() + "is not available");
        }

        if(product.getQuantity() < quantity){
            throw new ApiException("please, make an order of the " + product.getProductName()+
                    "less than or equal to the quantity " + product.getQuantity());
        }

        CartItems cartItem = cartItemRepository.findCartItemsByProductIdAndCartId(cart.getCartId(),productId);
        if(cartItem == null){
            throw new ApiException("Product" + product.getProductName() + "Not available in Cart");
        }

        cartItem.setPrice(product.getSpecialPrice());
        cartItem.setQuantity(cartItem.getQuantity() + quantity);
        cartItem.setDiscount(product.getDiscount());

        Integer totalPrice = cartItem.getTotalPrice();
        int currentTotal = (totalPrice != null) ? totalPrice : 0;

        double price = cartItem.getPrice(); // assuming getPrice() returns double
        double addedAmount = price * quantity;

        int newTotal = (int) Math.round(currentTotal + addedAmount);
        cartItem.setTotalPrice(newTotal);


        CartItems updatedCartItem = cartItemRepository.save(cartItem);

        if(updatedCartItem.getQuantity() == 0){
            cartItemRepository.deleteById(updatedCartItem.getId());
        }

        if(updatedCartItem.getQuantity() > product.getQuantity()){
            throw new ApiException("Not available " + product.getProductName() + " more than " + product.getQuantity());
        }

        cartItemRepository.save(cartItem);

        CartDTO cartDTO = modelMapper.map(cart,CartDTO.class);
        List<CartItems> cartItems = cart.getCartItems();

        Stream<ProductDTO> productDTOStream = cartItems.stream().map(
                item ->{
                    ProductDTO prd = modelMapper.map(item.getProduct(), ProductDTO.class);
                    prd.setQuantity(item.getQuantity());
                    return  prd;
                });

        cartDTO.setProduct(productDTOStream.toList());
        return cartDTO;
    }

    @Override
    public String deleteProductFromCart(Integer cartId, Integer productId) {
        Cart cart = cartRepository.findById(cartId).orElseThrow(
                () -> new ResourceNotFoundException("Cart","cartId",cartId.toString()));

        CartItems cartItems = cartItemRepository.findCartItemsByProductIdAndCartId(cartId,productId);

        if(cartItems == null){
            throw new ResourceNotFoundException("Product","productId",productId.toString());
        }
        cart.setTotalPrice(cart.getTotalPrice() - (
                cartItems.getPrice() * cartItems.getQuantity()
                ));

        cartItemRepository.deleteCartItemByProductIdAndCartId(cartId,productId);
        return "Product" + cartItems.getProduct().getProductName() + "removed from the cart!!!";
    }

    @Override
    public void updateProductInCarts(Integer cartId, Long productId) {

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(()-> new ResourceNotFoundException("Cart","cartId", cartId.toString()));

        Product product = productRepository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product","productId",productId.toString()));

        CartItems cartItems = cartItemRepository.findCartItemsByProductIdAndCartId(Math.toIntExact(productId),cartId);

        if(cartItems == null){
            throw new ApiException("Product" + product.getProductName() + "is not available in cart");
        }

        double cartPrice = cart.getTotalPrice() - (cartItems.getPrice() - cartItems.getQuantity() );

        cartItems.setPrice(cartItems.getPrice());
        cart.setTotalPrice(cartPrice);

        cartItems = cartItemRepository.save(cartItems);
    }
}
