package com.ecommerce.modules.order.dto;

import com.ecommerce.modules.order.entity.Shipment;
import com.ecommerce.modules.order.entity.ShipmentStatus;
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
public class ShipmentResponse {

    private Long id;
    private Long orderId;
    private Long warehouseId;
    private String warehouseName;
    private String carrier;
    private String trackingNo;
    private ShipmentStatus status;
    private BigDecimal shippingFee;
    private LocalDateTime estimatedDeliveryAt;
    private LocalDateTime createdAt;

    public static ShipmentResponse from(Shipment shipment) {
        return ShipmentResponse.builder()
                .id(shipment.getId())
                .orderId(shipment.getOrder().getId())
                .warehouseId(shipment.getWarehouse().getId())
                .warehouseName(shipment.getWarehouse().getName())
                .carrier(shipment.getCarrier())
                .trackingNo(shipment.getTrackingNo())
                .status(shipment.getStatus())
                .shippingFee(shipment.getShippingFee())
                .estimatedDeliveryAt(shipment.getEstimatedDeliveryAt())
                .createdAt(shipment.getCreatedAt())
                .build();
    }
}
