package com.ecommerce.modules.cart.dto;

import com.ecommerce.modules.cart.entity.CartItem;
import com.ecommerce.modules.catalog.entity.ProductVariant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {

    private Long id;
    private Long variantId;
    private String sku;
    private String productName;
    private String variantName;
    private String productSlug;
    private String thumbnailUrl;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal totalPrice;
    private Boolean inStock;
    private Integer availableStock;

    public static CartItemResponse from(CartItem item, int availableStock) {
        ProductVariant variant = item.getVariant();
        BigDecimal effectivePrice = variant.getEffectivePrice();
        BigDecimal total = effectivePrice.multiply(BigDecimal.valueOf(item.getQuantity()));

        return CartItemResponse.builder()
                .id(item.getId())
                .variantId(variant.getId())
                .sku(variant.getSku())
                .productName(variant.getProduct().getName())
                .variantName(variant.getName())
                .productSlug(variant.getProduct().getSlug())
                .thumbnailUrl(variant.getProduct().getThumbnailUrl())
                .unitPrice(effectivePrice)
                .quantity(item.getQuantity())
                .totalPrice(total)
                .inStock(availableStock >= item.getQuantity())
                .availableStock(availableStock)
                .build();
    }
}
