package com.ecommerce.modules.order.dto;

import com.ecommerce.modules.order.entity.OrderStatus;
import com.ecommerce.modules.order.entity.OrderStatusLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusLogResponse {

    private Long id;
    private OrderStatus previousStatus;
    private OrderStatus newStatus;
    private String reason;
    private String changedBy;
    private LocalDateTime createdAt;

    public static OrderStatusLogResponse from(OrderStatusLog log) {
        return OrderStatusLogResponse.builder()
                .id(log.getId())
                .previousStatus(log.getPreviousStatus())
                .newStatus(log.getNewStatus())
                .reason(log.getReason())
                .changedBy(log.getChangedBy())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
