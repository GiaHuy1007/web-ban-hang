package com.ecommerce.modules.catalog.dto;

import com.ecommerce.modules.catalog.entity.Product;
import com.ecommerce.modules.catalog.entity.ProductStatus;
import com.ecommerce.modules.catalog.entity.ProductVariant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSummaryResponse {

    private Long id;
    private String name;
    private String slug;
    private String categoryName;
    private String categorySlug;
    private String brandName;
    private String brandSlug;
    private String thumbnailUrl;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal ratingAverage;
    private Integer reviewCount;
    private ProductStatus status;

    public static ProductSummaryResponse from(Product product) {
        List<ProductVariant> variants = product.getVariants();
        BigDecimal min = BigDecimal.ZERO;
        BigDecimal max = BigDecimal.ZERO;

        if (variants != null && !variants.isEmpty()) {
            min = variants.stream()
                    .filter(ProductVariant::getIsActive)
                    .map(ProductVariant::getEffectivePrice)
                    .min(Comparator.naturalOrder())
                    .orElse(BigDecimal.ZERO);

            max = variants.stream()
                    .filter(ProductVariant::getIsActive)
                    .map(ProductVariant::getEffectivePrice)
                    .max(Comparator.naturalOrder())
                    .orElse(BigDecimal.ZERO);
        }

        return ProductSummaryResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .categoryName(product.getCategory().getName())
                .categorySlug(product.getCategory().getSlug())
                .brandName(product.getBrand().getName())
                .brandSlug(product.getBrand().getSlug())
                .thumbnailUrl(product.getThumbnailUrl())
                .minPrice(min)
                .maxPrice(max)
                .ratingAverage(product.getRatingAverage())
                .reviewCount(product.getReviewCount())
                .status(product.getStatus())
                .build();
    }
}
