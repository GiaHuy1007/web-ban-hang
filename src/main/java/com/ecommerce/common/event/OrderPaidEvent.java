package com.ecommerce.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPaidEvent {

    private Long orderId;
    private String orderNo;
    private Long userId;
    private String userEmail;
    private BigDecimal amount;
    private String transactionId;
}
