package com.ecommerce.modules.payment.dto;

import com.ecommerce.modules.order.entity.PaymentMethod;
import com.ecommerce.modules.order.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitResponse {

    private Long paymentId;
    private String orderNo;
    private PaymentMethod paymentMethod;
    private BigDecimal amount;
    private String paymentUrl;
    private PaymentStatus status;
    private String message;
}
