package com.ecommerce.modules.payment.dto;

import com.ecommerce.modules.order.entity.PaymentMethod;
import com.ecommerce.modules.order.entity.PaymentStatus;
import com.ecommerce.modules.payment.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private Long id;
    private Long orderId;
    private String orderNo;
    private PaymentMethod paymentMethod;
    private String provider;
    private String transactionId;
    private PaymentStatus status;
    private BigDecimal amount;
    private String currency;
    private String idempotencyKey;
    private LocalDateTime createdAt;

    public static PaymentResponse from(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .orderNo(payment.getOrder().getOrderNo())
                .paymentMethod(payment.getPaymentMethod())
                .provider(payment.getProvider())
                .transactionId(payment.getTransactionId())
                .status(payment.getStatus())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .idempotencyKey(payment.getIdempotencyKey())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
