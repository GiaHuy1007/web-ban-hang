package com.ecommerce.modules.catalog.dto;

import com.ecommerce.modules.catalog.entity.Brand;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandResponse {

    private Long id;
    private String name;
    private String slug;
    private String logoUrl;
    private String description;
    private Boolean isActive;

    public static BrandResponse from(Brand brand) {
        return BrandResponse.builder()
                .id(brand.getId())
                .name(brand.getName())
                .slug(brand.getSlug())
                .logoUrl(brand.getLogoUrl())
                .description(brand.getDescription())
                .isActive(brand.getIsActive())
                .build();
    }
}
