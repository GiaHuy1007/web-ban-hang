package com.ecommerce.modules.order.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.response.PageResponse;
import com.ecommerce.common.util.SecurityUtils;
import com.ecommerce.modules.order.dto.*;
import com.ecommerce.modules.order.entity.OrderStatus;
import com.ecommerce.modules.order.service.OrderService;
import com.ecommerce.modules.order.service.ShipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
@Tag(name = "Admin - Order Management", description = "Xử lý đơn hàng, đổi trạng thái và tạo vận đơn dành cho Warehouse, CS và Admin")
public class AdminOrderController {

    private final OrderService orderService;
    private final ShipmentService shipmentService;

    @GetMapping
    @PreAuthorize("hasAuthority('ORDER_READ') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Admin: Danh sách tất cả đơn hàng hệ thống theo trạng thái")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(orderService.adminGetAllOrders(status, pageable))));
    }

    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasAuthority('ORDER_WRITE') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Admin: Cập nhật trạng thái đơn hàng (CONFIRMED, PREPARING, SHIPPING, DELIVERED, CANCELLED...)")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        String adminEmail = SecurityUtils.getAuthentication().getName();
        return ResponseEntity.ok(ApiResponse.ok(orderService.adminUpdateOrderStatus(orderId, request, adminEmail)));
    }

    @PostMapping("/{orderId}/shipments")
    @PreAuthorize("hasAuthority('ORDER_WRITE') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Admin: Tạo vận đơn giao hàng (Shipment) và xuất kho")
    public ResponseEntity<ApiResponse<ShipmentResponse>> createShipment(
            @PathVariable Long orderId,
            @Valid @RequestBody CreateShipmentRequest request) {
        String adminEmail = SecurityUtils.getAuthentication().getName();
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(shipmentService.createShipment(orderId, request, adminEmail)));
    }

    @GetMapping("/{orderId}/shipments")
    @PreAuthorize("hasAuthority('ORDER_READ') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Admin: Xem chi tiết vận đơn của đơn hàng")
    public ResponseEntity<ApiResponse<ShipmentResponse>> getShipment(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.ok(shipmentService.getShipmentByOrder(orderId)));
    }
}
