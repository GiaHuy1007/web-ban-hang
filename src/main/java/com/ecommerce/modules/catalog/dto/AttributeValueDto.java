package com.ecommerce.modules.catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttributeValueDto {

    private Long templateId;
    private String code;
    private String attributeName;
    private String value;
    private String unit;
}
