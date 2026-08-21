package com.ecommerce.modules.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockInboundRequest {

    @NotNull(message = "ID biến thể SKU không được để trống.")
    private Long variantId;

    @NotNull(message = "ID kho hàng không được để trống.")
    private Long warehouseId;

    @NotNull(message = "Số lượng nhập không được để trống.")
    @Min(value = 1, message = "Số lượng nhập phải lớn hơn 0.")
    private Integer quantity;

    private String note;
    private List<String> serialNumbers;
}
