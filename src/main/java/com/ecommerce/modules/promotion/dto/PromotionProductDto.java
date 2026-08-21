package com.ecommerce.modules.promotion.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionProductDto {

    @NotNull(message = "ID biến thể SKU không được để trống.")
    private Long variantId;

    private String variantSku;
    private String variantName;
    private BigDecimal originalPrice;

    @NotNull(message = "Giá Flash Sale không được để trống.")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá khuyến mãi phải lớn hơn 0.")
    private BigDecimal promotionalPrice;

    @Builder.Default
    private Integer quantityLimit = 0;

    @Builder.Default
    private Integer quantitySold = 0;
}
