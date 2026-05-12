package com.ecommerce.service;

import com.ecommerce.exceptions.ApiException;
import com.ecommerce.exceptions.ResourceNotFoundException;
import com.ecommerce.model.*;
import com.ecommerce.payload.OrderDTO;
import com.ecommerce.payload.OrderItemDTO;
import com.ecommerce.payload.OrderRequestDTO;
import com.ecommerce.payload.PaymentDTO;
import com.ecommerce.payload.ProductDTO;
import com.ecommerce.repositories.*;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

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
    public OrderDTO placeOrder(String emailId, OrderRequestDTO request) {

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
        if (request.getAddressId() == null) {
            throw new ApiException("addressId is required.");
        }
        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", request.getAddressId().toString()));

        // Server total is source of truth; verify client-sent totalAmount to catch tampering
        if (request.getTotalAmount() != null) {
            double serverTotal = cart.getTotalPrice();
            double clientTotal = request.getTotalAmount();
            if (Math.abs(serverTotal - clientTotal) > 0.01) {
                throw new ApiException("Total amount mismatch. Server=" + serverTotal + ", Client=" + clientTotal);
            }
        }

        // Minimal payment validation (mock payment only; do not store full card data)
        if (request.getPaymentMethod() == null || request.getPaymentMethod().isBlank()) {
            throw new ApiException("paymentMethod is required.");
        }
        if ("CARD".equalsIgnoreCase(request.getPaymentMethod())) {
            if (request.getCvv() == null || request.getCvv().length() < 3) {
                throw new ApiException("cvv is required for CARD payment.");
            }
            if (request.getExpMonth() == null || request.getExpYear() == null) {
                throw new ApiException("expMonth and expYear are required for CARD payment.");
            }
        }

        // Validate stock for all items upfront before creating anything
        for (CartItems cartItem : cartItems) {
            Product product = cartItem.getProduct();
            if (product.getQuantity() < cartItem.getQuantity()) {
                throw new ApiException("Product '" + product.getProductName() +
                        "' is out of stock or insufficient quantity.");
            }
        }

        // Create Order
        Order order = new Order();
        order.setEmail(emailId);
        order.setOrderDate(LocalDate.now());
        order.setTotalAmount(cart.getTotalPrice());
        order.setOrderStatus("Order Accepted");
        order.setAddress(address);

        // Create Payment and link to order (mock/local only)
        String pgPaymentId = "MOCK-" + UUID.randomUUID();
        String last4 = last4(request.getCardNumber());
        String responseMessage = "CARD".equalsIgnoreCase(request.getPaymentMethod()) && last4 != null
                ? ("Charged card ending ****" + last4)
                : "Payment captured (mock)";

        Payment payment = new Payment();
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPgName("MOCK");
        payment.setPgPaymentId(pgPaymentId);
        payment.setPgStatus("SUCCESS");
        payment.setPgResponseMessage(responseMessage);
        payment.setOrder(order);
        payment = paymentRepository.save(payment);
        order.setPayment(payment);

        // Save Order
        Order savedOrder = orderRepository.save(order);

        // Create Order Items from Cart Items
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItems cartItem : cartItems) {
            Product product = cartItem.getProduct();

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

        // Reduce stock for each product
        for (CartItems item : cartItems) {
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() - item.getQuantity());
            productRepository.save(product);
        }

        // FIX: Clear entire cart in one shot instead of deleting items one by one in a loop.
        // cartService.clearCart() bulk-deletes all CartItems and resets the cart total to 0.
        cartService.clearCart(cart.getCartId());

        // Map to DTO
        OrderDTO orderDTO = modelMapper.map(savedOrder, OrderDTO.class);
        orderDTO.setOrderItemDTOS(new ArrayList<>());

        for (OrderItem item : orderItems) {
            orderDTO.getOrderItemDTOS().add(modelMapper.map(item, OrderItemDTO.class));
        }

        orderDTO.setAddressId(request.getAddressId());

        return orderDTO;
    }

    @Override
    @Transactional
    public List<OrderDTO> getAllOrders() {
        List<Order> orders = orderRepository.findAll(Sort.by(Sort.Direction.DESC, "orderDate", "orderId"));
        List<OrderDTO> result = new ArrayList<>(orders.size());
        for (Order order : orders) {
            result.add(toOrderDTO(order));
        }
        return result;
    }

    private OrderDTO toOrderDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setOrderId(order.getOrderId());
        dto.setEmail(order.getEmail());
        dto.setOrderDate(order.getOrderDate());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setOrderStatus(order.getOrderStatus());

        if (order.getAddress() != null && order.getAddress().getId() != null) {
            dto.setAddressId(Long.valueOf(order.getAddress().getId()));
        }

        if (order.getPayment() != null) {
            dto.setPaymentDTO(modelMapper.map(order.getPayment(), PaymentDTO.class));
        }

        List<OrderItemDTO> itemDTOS = new ArrayList<>();
        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                OrderItemDTO itemDTO = new OrderItemDTO();
                itemDTO.setOrderItemId(item.getOrderItemId());
                itemDTO.setQuantity(item.getQuantity());
                itemDTO.setDiscount(item.getDiscount());
                itemDTO.setOrderedProductPrice(item.getOrderedProductPrice());
                if (item.getProduct() != null) {
                    itemDTO.setProductDTO(modelMapper.map(item.getProduct(), ProductDTO.class));
                }
                itemDTOS.add(itemDTO);
            }
        }
        dto.setOrderItemDTOS(itemDTOS);

        return dto;
    }

    private static String last4(String cardNumber) {
        if (cardNumber == null) return null;
        String digits = cardNumber.replaceAll("\\s+", "");
        if (digits.length() < 4) return null;
        return digits.substring(digits.length() - 4);
    }
}