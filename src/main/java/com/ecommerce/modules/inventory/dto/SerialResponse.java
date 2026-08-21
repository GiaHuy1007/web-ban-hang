package com.ecommerce.modules.inventory.dto;

import com.ecommerce.modules.inventory.entity.ProductSerial;
import com.ecommerce.modules.inventory.entity.SerialStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SerialResponse {

    private Long id;
    private Long variantId;
    private String variantSku;
    private Long warehouseId;
    private String warehouseCode;
    private String serialNumber;
    private SerialStatus status;
    private Long orderItemId;
    private LocalDateTime warrantyExpiresAt;

    public static SerialResponse from(ProductSerial serial) {
        return SerialResponse.builder()
                .id(serial.getId())
                .variantId(serial.getVariant().getId())
                .variantSku(serial.getVariant().getSku())
                .warehouseId(serial.getWarehouse().getId())
                .warehouseCode(serial.getWarehouse().getCode())
                .serialNumber(serial.getSerialNumber())
                .status(serial.getStatus())
                .orderItemId(serial.getOrderItemId())
                .warrantyExpiresAt(serial.getWarrantyExpiresAt())
                .build();
    }
}
