package com.ecommerce.project.model;

import com.ecommerce.project.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name="payments")
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String provider; // STRIPE For main payment provider

    private String paymentIntentId;

    private String paymentMethod;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String currency;

    private Double amount;

    private LocalDateTime paidAt;

    @OneToOne(mappedBy = "payment", cascade ={CascadeType.PERSIST, CascadeType.MERGE})
    private Order order;
}