package com.ecommerce.modules.cart.dto;

import com.ecommerce.modules.cart.entity.Wishlist;
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
public class WishlistResponse {

    private Long id;
    private Long variantId;
    private String sku;
    private String productName;
    private String variantName;
    private String productSlug;
    private String thumbnailUrl;
    private BigDecimal basePrice;
    private BigDecimal salePrice;
    private BigDecimal effectivePrice;

    public static WishlistResponse from(Wishlist wishlist) {
        ProductVariant variant = wishlist.getVariant();
        return WishlistResponse.builder()
                .id(wishlist.getId())
                .variantId(variant.getId())
                .sku(variant.getSku())
                .productName(variant.getProduct().getName())
                .variantName(variant.getName())
                .productSlug(variant.getProduct().getSlug())
                .thumbnailUrl(variant.getProduct().getThumbnailUrl())
                .basePrice(variant.getBasePrice())
                .salePrice(variant.getSalePrice())
                .effectivePrice(variant.getEffectivePrice())
                .build();
    }
}
