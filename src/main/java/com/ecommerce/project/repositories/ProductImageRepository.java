package com.ecommerce.project.repositories;

import com.ecommerce.project.model.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductImageRepository extends JpaRepository<ProductImage,Long> {

}
