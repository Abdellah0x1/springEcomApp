package com.ecommerce.project.controller;


import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.payload.OrderDTO;
import com.ecommerce.project.services.AddressService;
import com.ecommerce.project.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    public AddressService addressService;

    @Autowired
    public OrderService orderService;



    public ResponseEntity<List<AddressDTO>> getMyAddresses(){
        return ResponseEntity.ok(addressService.getMyAddresses());
    }

    public ResponseEntity<List<OrderDTO>> getMyOrders(){
        return ResponseEntity.ok(orderService.getMyOrders());
    }

}
