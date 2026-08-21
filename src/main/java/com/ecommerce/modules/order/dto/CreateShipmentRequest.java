package com.ecommerce.modules.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateShipmentRequest {

    @NotNull(message = "ID kho xuất hàng không được để trống.")
    private Long warehouseId;

    @NotBlank(message = "Đơn vị vận chuyển không được để trống.")
    private String carrier;

    private String trackingNo;
    private BigDecimal shippingFee;
    private LocalDateTime estimatedDeliveryAt;
}
