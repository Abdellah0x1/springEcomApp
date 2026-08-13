package com.ecommerce.project.services;

import com.ecommerce.project.payload.OrderDTO;

import java.util.List;

public interface OrderService {
    OrderDTO createOrder(String email, Long addressId);
    List<OrderDTO> getCurrentSellerOrders();
}
