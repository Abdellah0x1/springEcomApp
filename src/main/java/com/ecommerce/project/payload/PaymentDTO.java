package com.ecommerce.project.payload;


import com.ecommerce.project.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDTO {

    private Long id;

    private String provider;

    private String paymentIntentId;

    private String paymentMethod;

    private PaymentStatus status;

    private String currency;

    private Double amount;

    private LocalDateTime paidAt;
}
