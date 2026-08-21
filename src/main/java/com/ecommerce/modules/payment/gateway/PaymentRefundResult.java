package com.ecommerce.modules.payment.gateway;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRefundResult {

    private boolean success;
    private String refundTransactionId;
    private BigDecimal refundedAmount;
    private String message;
}
