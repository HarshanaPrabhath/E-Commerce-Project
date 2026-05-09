package com.ecommerce.service;

import com.ecommerce.payload.OrderDTO;
import com.ecommerce.payload.OrderRequestDTO;
import jakarta.transaction.Transactional;

import java.util.List;

public interface OrderService {
    @Transactional
    OrderDTO placeOrder(String emailId, OrderRequestDTO request);

    @Transactional
    List<OrderDTO> getAllOrders();
}
