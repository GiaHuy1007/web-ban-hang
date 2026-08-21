package com.ecommerce.modules.catalog.dto;

import com.ecommerce.modules.catalog.entity.ProductVariant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariantResponse {

    private Long id;
    private Long productId;
    private String sku;
    private String name;
    private BigDecimal basePrice;
    private BigDecimal salePrice;
    private BigDecimal effectivePrice;
    private Boolean isActive;
    private Integer availableStock;
    private List<AttributeValueDto> attributeValues;
    private List<String> images;

    public static VariantResponse from(ProductVariant variant) {
        List<AttributeValueDto> attrDtos = variant.getAttributeValues() != null
                ? variant.getAttributeValues().stream()
                    .map(v -> AttributeValueDto.builder()
                            .templateId(v.getAttributeTemplate().getId())
                            .code(v.getAttributeTemplate().getCode())
                            .attributeName(v.getAttributeTemplate().getAttributeName())
                            .value(v.getValue())
                            .unit(v.getAttributeTemplate().getUnit())
                            .build())
                    .collect(Collectors.toList())
                : List.of();

        List<String> imgUrls = variant.getImages() != null
                ? variant.getImages().stream().map(img -> img.getImageUrl()).collect(Collectors.toList())
                : List.of();

        return VariantResponse.builder()
                .id(variant.getId())
                .productId(variant.getProduct().getId())
                .sku(variant.getSku())
                .name(variant.getName())
                .basePrice(variant.getBasePrice())
                .salePrice(variant.getSalePrice())
                .effectivePrice(variant.getEffectivePrice())
                .isActive(variant.getIsActive())
                .attributeValues(attrDtos)
                .images(imgUrls)
                .build();
    }
}
