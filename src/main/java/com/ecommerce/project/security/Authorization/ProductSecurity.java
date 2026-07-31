package com.ecommerce.project.security.Authorization;


import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductSecurity {

    private final ProductRepository productRepository;

    public boolean isOwner(Long productId, Authentication authentication){
        Product product = productRepository.findById(productId).orElseThrow(()-> new ResourceNotFoundException("Product" , "ProductId",productId ));
        return product.getUser().getEmail().equals(authentication.getName());
    }

}
