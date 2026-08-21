package com.ecommerce.modules.order.dto;

import com.ecommerce.modules.order.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {

    private Long id;
    private Long variantId;
    private String productName;
    private String sku;
    private String attributesJson;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal totalPrice;

    public static OrderItemResponse from(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .variantId(item.getVariant() != null ? item.getVariant().getId() : null)
                .productName(item.getProductNameSnapshot())
                .sku(item.getSkuSnapshot())
                .attributesJson(item.getAttributesSnapshot())
                .unitPrice(item.getUnitPriceSnapshot())
                .quantity(item.getQuantity())
                .totalPrice(item.getTotalPrice())
                .build();
    }
}
