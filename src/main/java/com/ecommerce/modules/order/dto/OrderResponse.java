package com.ecommerce.modules.order.dto;

import com.ecommerce.modules.order.entity.Order;
import com.ecommerce.modules.order.entity.OrderStatus;
import com.ecommerce.modules.order.entity.PaymentMethod;
import com.ecommerce.modules.order.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long id;
    private String orderNo;
    private Long userId;
    private String userEmail;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private BigDecimal subtotalAmount;
    private BigDecimal discountAmount;
    private BigDecimal shippingFee;
    private BigDecimal totalAmount;
    private String shippingAddressSnapshot;
    private String notes;
    private List<OrderItemResponse> items;
    private List<OrderStatusLogResponse> statusLogs;
    private LocalDateTime createdAt;

    public static OrderResponse from(Order order) {
        List<OrderItemResponse> itemDtos = order.getItems() != null
                ? order.getItems().stream().map(OrderItemResponse::from).collect(Collectors.toList())
                : List.of();

        List<OrderStatusLogResponse> logDtos = order.getStatusLogs() != null
                ? order.getStatusLogs().stream().map(OrderStatusLogResponse::from).collect(Collectors.toList())
                : List.of();

        return OrderResponse.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .userId(order.getUser().getId())
                .userEmail(order.getUser().getEmail())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .subtotalAmount(order.getSubtotalAmount())
                .discountAmount(order.getDiscountAmount())
                .shippingFee(order.getShippingFee())
                .totalAmount(order.getTotalAmount())
                .shippingAddressSnapshot(order.getShippingAddressSnapshot())
                .notes(order.getNotes())
                .items(itemDtos)
                .statusLogs(logDtos)
                .createdAt(order.getCreatedAt())
                .build();
    }
}
