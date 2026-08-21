package com.ecommerce.modules.payment.gateway;

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
public class PaymentCallbackResult {

    private boolean isSignatureValid;
    private String orderNo;
    private String transactionId;
    private PaymentStatus status;
    private BigDecimal amount;
    private String message;
    private String rawData;
}
