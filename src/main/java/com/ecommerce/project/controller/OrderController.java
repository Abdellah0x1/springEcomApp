package com.ecommerce.project.controller;

import com.ecommerce.project.payload.OrderDTO;
import com.ecommerce.project.payload.OrderRequestDTO;
import com.ecommerce.project.services.OrderService;
import com.ecommerce.project.utils.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class OrderController {

    @Autowired
    private AuthUtils authUtils;
    @Autowired
    private OrderService orderService;

//    @PostMapping("/order/user/payments/{paymentMethod}")
//    public ResponseEntity<OrderDTO> orderProducts(@PathVariable String paymentMethod , @RequestBody OrderRequestDTO orderRequest){
//        String email = authUtils.loggedInEmail();
//
//        OrderDTO orderDTO = orderService.placeOrder(
//                email,
//                orderRequest.getAddressId(),
//                paymentMethod,
//                orderRequest.getPgName(),
//                orderRequest.getPgPaymentId(),
//                orderRequest.getPgStatus(),
//                orderRequest.getPgResponseMessage()
//        );
//        return new ResponseEntity<>(orderDTO, HttpStatus.CREATED);
//    }

    @PostMapping("/orders")
    public ResponseEntity<OrderDTO> createOrder(
            @RequestBody OrderRequestDTO request
    ){
        String email = authUtils.loggedInEmail();

        OrderDTO order = orderService.createOrder(
                email,
                request.getAddressId());

        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }
}
