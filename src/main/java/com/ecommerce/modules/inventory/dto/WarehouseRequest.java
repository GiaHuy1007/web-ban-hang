package com.ecommerce.modules.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseRequest {

    @NotBlank(message = "Mã kho không được để trống.")
    private String code;

    @NotBlank(message = "Tên kho không được để trống.")
    private String name;

    @NotBlank(message = "Địa chỉ không được để trống.")
    private String address;

    @NotBlank(message = "Thành phố/Khu vực không được để trống.")
    private String city;

    @Builder.Default
    private Boolean isActive = true;
}
