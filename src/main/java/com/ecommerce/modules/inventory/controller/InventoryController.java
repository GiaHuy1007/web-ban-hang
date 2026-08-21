package com.ecommerce.modules.inventory.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.modules.inventory.dto.*;
import com.ecommerce.modules.inventory.service.InventoryService;
import com.ecommerce.modules.inventory.service.ProductSerialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory & Stock Control", description = "Quản lý tồn kho, nhập/xuất/điều chỉnh và tra cứu bảo hành Serial/IMEI")
public class InventoryController {

    private final InventoryService inventoryService;
    private final ProductSerialService serialService;

    @GetMapping("/variants/{variantId}/stock")
    @Operation(summary = "Lấy số lượng tồn kho khả dụng của một biến thể SKU (Public)")
    public ResponseEntity<ApiResponse<Integer>> getAvailableStock(@PathVariable Long variantId) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getAvailableStock(variantId)));
    }

    @GetMapping("/admin/variants/{variantId}")
    @PreAuthorize("hasAuthority('INVENTORY_READ') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Admin: Chi tiết tồn kho theo từng kho hàng của biến thể SKU")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getInventoriesByVariant(@PathVariable Long variantId) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getInventoriesByVariant(variantId)));
    }

    @PostMapping("/admin/inbound")
    @PreAuthorize("hasAuthority('INVENTORY_WRITE') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Admin: Nhập hàng vào kho (Inbound)")
    public ResponseEntity<ApiResponse<InventoryResponse>> inboundStock(@Valid @RequestBody StockInboundRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(inventoryService.inboundStock(request)));
    }

    @PostMapping("/admin/adjustment")
    @PreAuthorize("hasAuthority('INVENTORY_WRITE') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Admin: Điều chỉnh tồn kho sau kiểm kê (Adjustment)")
    public ResponseEntity<ApiResponse<InventoryResponse>> adjustStock(@Valid @RequestBody StockAdjustmentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.adjustStock(request)));
    }

    @GetMapping("/warranty/{serialNumber}")
    @Operation(summary = "Tra cứu thông tin bảo hành điện tử theo số Serial/IMEI")
    public ResponseEntity<ApiResponse<SerialResponse>> checkWarranty(@PathVariable String serialNumber) {
        return ResponseEntity.ok(ApiResponse.ok(serialService.getSerialInfo(serialNumber)));
    }
}
