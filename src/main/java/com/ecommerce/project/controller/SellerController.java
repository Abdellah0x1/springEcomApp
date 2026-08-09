package com.ecommerce.project.controller;


import com.ecommerce.project.config.AppConstants;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.OrderDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.services.OrderService;
import com.ecommerce.project.services.ProductService;
import com.ecommerce.project.utils.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/api/seller")
@RestController
public class SellerController {

    @Autowired
    private AuthUtils authUtils;

    @Autowired
    private ProductService productService;
    @Autowired
    private OrderService orderService;

    @GetMapping("/products")
    public ResponseEntity<ProductResponse> getMyProducts(
            @RequestParam(name="page" , required = false, defaultValue = AppConstants.PAGE_NUMER) Integer pageNumber,
            @RequestParam (name="size" , required = false, defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
            @RequestParam (name="sortBy" , required = false, defaultValue = AppConstants.SORT_PRODUCTS_BY) String sortBy,
            @RequestParam (name="sortOrder" , required = false, defaultValue = AppConstants.SORT_ORDER) String sortOrder
    ){
        User seller = authUtils.loggedInUser();

        ProductResponse productResponse = productService.getProductsBySeller(seller.getUserId(),pageNumber, pageSize, sortBy, sortOrder);
        return ResponseEntity.ok().body(productResponse);
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderDTO>> getMyOrders(){
        List<OrderDTO> sellerOrders = orderService.getCurrentSellerOrders();
        return ResponseEntity.ok().body(sellerOrders);
    }
}
