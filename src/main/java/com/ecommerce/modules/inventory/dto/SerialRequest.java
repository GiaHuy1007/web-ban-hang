package com.ecommerce.modules.inventory.dto;

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
public class SerialRequest {

    @NotNull(message = "ID biến thể SKU không được để trống.")
    private Long variantId;

    @NotNull(message = "ID kho không được để trống.")
    private Long warehouseId;

    @NotBlank(message = "Số Serial/IMEI không được để trống.")
    private String serialNumber;
}
