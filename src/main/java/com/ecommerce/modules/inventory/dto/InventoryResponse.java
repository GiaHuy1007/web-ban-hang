package com.ecommerce.modules.inventory.dto;

import com.ecommerce.modules.inventory.entity.Inventory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {

    private Long id;
    private Long variantId;
    private String variantSku;
    private String variantName;
    private Long warehouseId;
    private String warehouseName;
    private String warehouseCode;
    private Integer quantityOnHand;
    private Integer quantityReserved;
    private Integer quantityAvailable;

    public static InventoryResponse from(Inventory inventory) {
        return InventoryResponse.builder()
                .id(inventory.getId())
                .variantId(inventory.getVariant().getId())
                .variantSku(inventory.getVariant().getSku())
                .variantName(inventory.getVariant().getName())
                .warehouseId(inventory.getWarehouse().getId())
                .warehouseName(inventory.getWarehouse().getName())
                .warehouseCode(inventory.getWarehouse().getCode())
                .quantityOnHand(inventory.getQuantityOnHand())
                .quantityReserved(inventory.getQuantityReserved())
                .quantityAvailable(inventory.getAvailableQuantity())
                .build();
    }
}
