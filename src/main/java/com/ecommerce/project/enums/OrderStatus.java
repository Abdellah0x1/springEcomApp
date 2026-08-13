package com.ecommerce.project.enums;

import java.time.LocalDate;

public enum OrderStatus {
    PENDING_PAYMENT,
    PAID,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    REFUNDED,
    PAYMENT_FAILED;
}
