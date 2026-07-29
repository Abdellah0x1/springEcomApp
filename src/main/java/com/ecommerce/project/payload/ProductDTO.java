package com.ecommerce.project.payload;

import com.ecommerce.project.model.Product;
import com.ecommerce.project.model.ProductImage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long productId;
    private String productName;
    private String description;
    private Integer quantity;
    private double price;
    private Double specialPrice;
    private double discount;
    private List<ProductImageDTO> images = new ArrayList<>();
}
