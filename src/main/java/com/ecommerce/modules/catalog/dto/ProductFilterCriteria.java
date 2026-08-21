package com.ecommerce.modules.catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductFilterCriteria {

    private String categorySlug;
    private String brandSlug;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String keyword;
    private String sortBy; // newest, price_asc, price_desc, rating

    @Builder.Default
    private Map<String, String> attributeFilters = new HashMap<>();
}
