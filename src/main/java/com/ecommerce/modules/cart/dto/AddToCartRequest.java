package com.ecommerce.modules.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddToCartRequest {

    @NotNull(message = "ID biến thể SKU không được để trống.")
    private Long variantId;

    @NotNull(message = "Số lượng không được để trống.")
    @Min(value = 1, message = "Số lượng mua tối thiểu là 1.")
    @Builder.Default
    private Integer quantity = 1;
}
