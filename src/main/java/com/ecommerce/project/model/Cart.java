package com.ecommerce.project.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name ="carts")
@NoArgsConstructor
@AllArgsConstructor
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="cart_id")
    private Long id ;

    @OneToOne
    @JoinColumn(name="user_id")
    private User user;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name="cart_items", joinColumns = @JoinColumn(name="cart_id"), inverseJoinColumns = @JoinColumn(name="product_id"))
        private List<CartItem> cartItems = new ArrayList<>();

    private Double totalPrice = 0.0;
}
