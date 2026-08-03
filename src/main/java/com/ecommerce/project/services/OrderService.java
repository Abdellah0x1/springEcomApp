package com.ecommerce.project.services;

import com.ecommerce.project.payload.OrderDTO;

public interface OrderService {
    OrderDTO createOrder(String email, Long addressId);
}
