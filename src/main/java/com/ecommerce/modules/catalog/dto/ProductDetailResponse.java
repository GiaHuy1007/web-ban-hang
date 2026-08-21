package com.ecommerce.modules.catalog.dto;

import com.ecommerce.modules.catalog.entity.Product;
import com.ecommerce.modules.catalog.entity.ProductStatus;
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
public class ProductDetailResponse {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private String shortDescription;
    private String thumbnailUrl;
    private Long categoryId;
    private String categoryName;
    private String categorySlug;
    private Long brandId;
    private String brandName;
    private String brandSlug;
    private ProductStatus status;
    private BigDecimal ratingAverage;
    private Integer reviewCount;
    private List<AttributeValueDto> informationalAttributes;
    private List<VariantResponse> variants;
    private List<String> images;

    public static ProductDetailResponse from(Product product) {
        List<AttributeValueDto> infoAttrs = product.getAttributeValues() != null
                ? product.getAttributeValues().stream()
                    .map(attr -> AttributeValueDto.builder()
                            .templateId(attr.getAttributeTemplate().getId())
                            .code(attr.getAttributeTemplate().getCode())
                            .attributeName(attr.getAttributeTemplate().getAttributeName())
                            .value(attr.getValue())
                            .unit(attr.getAttributeTemplate().getUnit())
                            .build())
                    .collect(Collectors.toList())
                : List.of();

        List<VariantResponse> variantDtos = product.getVariants() != null
                ? product.getVariants().stream().map(VariantResponse::from).collect(Collectors.toList())
                : List.of();

        List<String> imgUrls = product.getImages() != null
                ? product.getImages().stream().map(img -> img.getImageUrl()).collect(Collectors.toList())
                : List.of();

        return ProductDetailResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .shortDescription(product.getShortDescription())
                .thumbnailUrl(product.getThumbnailUrl())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .categorySlug(product.getCategory().getSlug())
                .brandId(product.getBrand().getId())
                .brandName(product.getBrand().getName())
                .brandSlug(product.getBrand().getSlug())
                .status(product.getStatus())
                .ratingAverage(product.getRatingAverage())
                .reviewCount(product.getReviewCount())
                .informationalAttributes(infoAttrs)
                .variants(variantDtos)
                .images(imgUrls)
                .build();
    }
}
