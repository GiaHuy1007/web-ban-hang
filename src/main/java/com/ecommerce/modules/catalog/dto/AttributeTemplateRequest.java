package com.ecommerce.modules.catalog.dto;

import com.ecommerce.modules.catalog.entity.AttributeDataType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttributeTemplateRequest {

    @NotBlank(message = "Tên thuộc tính không được để trống.")
    private String attributeName;

    @NotBlank(message = "Mã code thuộc tính không được để trống (vd: ram, storage, cpu).")
    private String code;

    @NotNull(message = "Kiểu dữ liệu (NUMBER, TEXT, ENUM, BOOLEAN) không được để trống.")
    private AttributeDataType dataType;

    private String unit;

    @Builder.Default
    private Boolean isVariantDefining = false;

    @Builder.Default
    private Boolean isFilterable = true;

    @Builder.Default
    private Boolean isRequired = false;

    @Builder.Default
    private Integer sortOrder = 0;
}
