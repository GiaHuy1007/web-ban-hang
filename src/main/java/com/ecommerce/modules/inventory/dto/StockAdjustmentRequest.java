package com.ecommerce.modules.inventory.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAdjustmentRequest {

    @NotNull(message = "ID biến thể SKU không được để trống.")
    private Long variantId;

    @NotNull(message = "ID kho hàng không được để trống.")
    private Long warehouseId;

    @NotNull(message = "Số lượng điều chỉnh (dương để tăng, âm để giảm) không được để trống.")
    private Integer quantityChange;

    @NotNull(message = "Lý do điều chỉnh không được để trống.")
    private String reason;
}
