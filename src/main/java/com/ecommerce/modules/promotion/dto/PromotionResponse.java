package com.ecommerce.modules.promotion.dto;

import com.ecommerce.modules.promotion.entity.Promotion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionResponse {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private String bannerUrl;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean isActive;
    private Boolean isRunning;
    private List<PromotionProductDto> products;

    public static PromotionResponse from(Promotion promotion) {
        List<PromotionProductDto> productDtos = promotion.getProducts() != null
                ? promotion.getProducts().stream()
                    .map(pp -> PromotionProductDto.builder()
                            .variantId(pp.getVariant().getId())
                            .variantSku(pp.getVariant().getSku())
                            .variantName(pp.getVariant().getName())
                            .originalPrice(pp.getVariant().getBasePrice())
                            .promotionalPrice(pp.getPromotionalPrice())
                            .quantityLimit(pp.getQuantityLimit())
                            .quantitySold(pp.getQuantitySold())
                            .build())
                    .collect(Collectors.toList())
                : List.of();

        return PromotionResponse.builder()
                .id(promotion.getId())
                .name(promotion.getName())
                .slug(promotion.getSlug())
                .description(promotion.getDescription())
                .bannerUrl(promotion.getBannerUrl())
                .startTime(promotion.getStartTime())
                .endTime(promotion.getEndTime())
                .isActive(promotion.getIsActive())
                .isRunning(promotion.isRunning())
                .products(productDtos)
                .build();
    }
}
