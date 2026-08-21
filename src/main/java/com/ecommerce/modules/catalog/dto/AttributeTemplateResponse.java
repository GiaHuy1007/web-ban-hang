package com.ecommerce.modules.catalog.dto;

import com.ecommerce.modules.catalog.entity.AttributeDataType;
import com.ecommerce.modules.catalog.entity.CategoryAttributeTemplate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttributeTemplateResponse {

    private Long id;
    private Long categoryId;
    private String attributeName;
    private String code;
    private AttributeDataType dataType;
    private String unit;
    private Boolean isVariantDefining;
    private Boolean isFilterable;
    private Boolean isRequired;
    private Integer sortOrder;

    public static AttributeTemplateResponse from(CategoryAttributeTemplate template) {
        return AttributeTemplateResponse.builder()
                .id(template.getId())
                .categoryId(template.getCategory().getId())
                .attributeName(template.getAttributeName())
                .code(template.getCode())
                .dataType(template.getDataType())
                .unit(template.getUnit())
                .isVariantDefining(template.getIsVariantDefining())
                .isFilterable(template.getIsFilterable())
                .isRequired(template.getIsRequired())
                .sortOrder(template.getSortOrder())
                .build();
    }
}
