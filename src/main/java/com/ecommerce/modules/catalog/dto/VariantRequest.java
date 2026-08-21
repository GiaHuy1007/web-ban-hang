package com.ecommerce.modules.catalog.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariantRequest {

    @NotBlank(message = "Mã SKU không được để trống.")
    private String sku;

    @NotBlank(message = "Tên biến thể không được để trống.")
    private String name;

    @NotNull(message = "Giá gốc không được để trống.")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá gốc phải lớn hơn 0.")
    private BigDecimal basePrice;

    private BigDecimal salePrice;

    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private List<AttributeValueDto> attributeValues = new ArrayList<>();

    @Builder.Default
    private List<String> imageUrls = new ArrayList<>();
}
