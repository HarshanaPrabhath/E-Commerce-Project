package com.ecommerce.service;

import com.ecommerce.exceptions.ApiException;
import com.ecommerce.exceptions.ResourceNotFoundException;
import com.ecommerce.model.*;
import com.ecommerce.payload.OrderDTO;
import com.ecommerce.payload.OrderItemDTO;
import com.ecommerce.repositories.*;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService{

    @Autowired
    UserRepository userRepository;
    @Autowired
    CartRepository cartRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    AddressRepository addressRepository;
    @Autowired
    PaymentRepository paymentRepository;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    OrderItemRepository orderItemRepository;
    @Autowired
    private CartService cartService;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    @Transactional
    public OrderDTO placeOrder(String emailId, Long addressId, String paymentMethod, String pgName, String pgPaymentId, String pgStatus, String pgResponseMessage) {

        // Get User Cart
        Cart cart = cartRepository.findCartByEmail(emailId);
        if (cart == null) {
            throw new ResourceNotFoundException("Cart", "email", emailId);
        }

        // Check if cart is empty
        List<CartItems> cartItems = cart.getCartItems();
        if (cartItems.isEmpty()) {
            throw new ApiException("Cart is empty.");
        }

        // Get Address
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId.toString()));

        // Create Order
        Order order = new Order();
        order.setEmail(emailId);
        order.setOrderDate(LocalDate.now());
        order.setTotalAmount(cart.getTotalPrice());
        order.setOrderStatus("Order Accepted");
        order.setAddress(address);

        // Create Payment and link to order
        Payment payment = new Payment(paymentMethod, pgPaymentId, pgStatus, pgName);
        payment.setOrder(order);
        payment = paymentRepository.save(payment);
        order.setPayment(payment);

        // Save Order
        Order savedOrder = orderRepository.save(order);

        // Create Order Items from Cart Items
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItems cartItem : cartItems) {
            Product product = cartItem.getProduct();

            // Validate stock
            if (product.getQuantity() < cartItem.getQuantity()) {
                throw new ApiException("Product '" + product.getProductName() + "' is out of stock or insufficient quantity.");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setDiscount(cartItem.getDiscount());
            orderItem.setOrderedProductPrice(cartItem.getPrice());
            orderItem.setOrder(savedOrder);
            orderItems.add(orderItem);
        }

        // Save all Order Items
        orderItems = orderItemRepository.saveAll(orderItems);

        // Update Product Stock and Clear Cart
        for (CartItems item : cartItems) {
            Product product = item.getProduct();
            int quantity = item.getQuantity();

            // Reduce stock
            product.setQuantity(product.getQuantity() - quantity);
            productRepository.save(product);

            // Remove item from cart
            cartService.deleteProductFromCart(cart.getCartId(), Math.toIntExact(product.getProductId()));
        }

        // Map to DTO
        OrderDTO orderDTO = modelMapper.map(savedOrder, OrderDTO.class);
        orderDTO.setOrderItemDTOS(new ArrayList<>());

        for (OrderItem item : orderItems) {
            orderDTO.getOrderItemDTOS().add(modelMapper.map(item, OrderItemDTO.class));
        }

        orderDTO.setAddressId(addressId);

        return orderDTO;
    }

}
