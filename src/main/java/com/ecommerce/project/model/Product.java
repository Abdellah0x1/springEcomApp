package com.ecommerce.project.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long productId;
    @Column(unique = true)
    @NotBlank
    @Size(min=3, message = "Product name must contain at least 3 characters")
    private String productName;
    @NotBlank
    @Size(min=6, message = "Product description must contain at least 6 characters")
    private String description;
    private int quantity;
    private double price;
    private Double specialPrice;
    private double discount;


    @OneToMany(mappedBy = "product", cascade = {CascadeType.MERGE, CascadeType.PERSIST},orphanRemoval = true)
    private List<ProductImage> productImages = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;


    @ManyToOne
    @JoinColumn(name="seller_id")
    private User user;

    @OneToMany(mappedBy = "product", fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<CartItem> products = new ArrayList<>();
}
