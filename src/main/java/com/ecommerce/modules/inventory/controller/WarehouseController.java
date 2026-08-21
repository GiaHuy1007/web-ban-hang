package com.ecommerce.modules.inventory.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.modules.inventory.dto.WarehouseRequest;
import com.ecommerce.modules.inventory.dto.WarehouseResponse;
import com.ecommerce.modules.inventory.service.WarehouseService;
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
@RequestMapping("/api/v1/admin/warehouses")
@RequiredArgsConstructor
@Tag(name = "Admin - Warehouse Management", description = "Quản lý hệ thống các kho hàng phân tán")
public class WarehouseController {

    private final WarehouseService warehouseService;

    @GetMapping
    @PreAuthorize("hasAuthority('INVENTORY_READ') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Admin: Danh sách tất cả kho hàng")
    public ResponseEntity<ApiResponse<List<WarehouseResponse>>> getAllWarehouses() {
        return ResponseEntity.ok(ApiResponse.ok(warehouseService.getAllWarehouses()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('INVENTORY_READ') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Admin: Chi tiết kho hàng")
    public ResponseEntity<ApiResponse<WarehouseResponse>> getWarehouseById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(warehouseService.getWarehouseById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('INVENTORY_WRITE') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Admin: Tạo mới kho hàng")
    public ResponseEntity<ApiResponse<WarehouseResponse>> createWarehouse(@Valid @RequestBody WarehouseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(warehouseService.createWarehouse(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('INVENTORY_WRITE') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Admin: Cập nhật thông tin kho hàng")
    public ResponseEntity<ApiResponse<WarehouseResponse>> updateWarehouse(
            @PathVariable Long id,
            @Valid @RequestBody WarehouseRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(warehouseService.updateWarehouse(id, request)));
    }
}
