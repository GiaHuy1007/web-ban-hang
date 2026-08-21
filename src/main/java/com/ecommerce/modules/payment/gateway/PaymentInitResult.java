package com.ecommerce.modules.payment.gateway;

import com.ecommerce.modules.order.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitResult {

    private String paymentUrl;
    private String transactionId;
    private PaymentStatus status;
    private String message;
}
