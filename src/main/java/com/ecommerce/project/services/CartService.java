package com.ecommerce.project.services;

import com.ecommerce.project.payload.CartDTO;
import jakarta.transaction.Transactional;

import java.util.List;

public interface CartService {
    CartDTO addProductToCart(Long ProductId, Integer quantity);
    List<CartDTO> getAllCarts();

    CartDTO getCart(String emailId, Long id);

    @Transactional
    CartDTO updateProductQuantityInCart(Long productId, Integer operation);

    String deleteProductFromCart(Long cartId,Long productId);

    void updateProductsInCart(Long cartId, Long productId);
}
