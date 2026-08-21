package com.ecommerce.modules.order.entity;

public enum OrderStatus {
    PENDING_CONFIRMATION,
    PENDING_PAYMENT,
    CONFIRMED,
    PREPARING,
    SHIPPING,
    DELIVERED,
    CANCELLED,
    RETURN_REQUESTED,
    REFUNDED
}
