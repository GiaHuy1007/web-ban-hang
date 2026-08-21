package com.ecommerce.modules.order.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.response.PageResponse;
import com.ecommerce.modules.order.dto.CheckoutRequest;
import com.ecommerce.modules.order.dto.OrderCancelRequest;
import com.ecommerce.modules.order.dto.OrderResponse;
import com.ecommerce.modules.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order Management (Customer)", description = "Đặt hàng (Checkout), xem lịch sử đơn hàng, timeline trạng thái và hủy đơn")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Đặt hàng / Checkout (Hỗ trợ Idempotency-Key chống tạo đơn lặp lại)")
    public ResponseEntity<ApiResponse<OrderResponse>> checkout(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody CheckoutRequest request) {
        OrderResponse response = orderService.checkout(request, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách lịch sử đơn hàng của người dùng hiện tại")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getUserOrders(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(orderService.getUserOrders(pageable))));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Xem chi tiết đơn hàng kèm timeline trạng thái và thông tin snapshot")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderDetails(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getOrderDetails(orderId)));
    }

    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "Khách hàng yêu cầu hủy đơn hàng (kèm lý do)")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderCancelRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.cancelOrder(orderId, request)));
    }
}
