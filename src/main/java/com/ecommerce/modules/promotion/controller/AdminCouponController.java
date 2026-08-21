package com.ecommerce.modules.promotion.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.modules.promotion.dto.CouponRequest;
import com.ecommerce.modules.promotion.dto.CouponResponse;
import com.ecommerce.modules.promotion.service.CouponService;
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
@RequestMapping("/api/v1/admin/coupons")
@RequiredArgsConstructor
@Tag(name = "Admin - Coupons Management", description = "Quản lý và phát hành voucher khuyến mãi")
public class AdminCouponController {

    private final CouponService couponService;

    @GetMapping
    @PreAuthorize("hasAuthority('PROMOTION_WRITE') or hasRole('SUPER_ADMIN') or hasRole('MARKETING')")
    @Operation(summary = "Admin: Danh sách tất cả mã giảm giá")
    public ResponseEntity<ApiResponse<List<CouponResponse>>> getAllCoupons() {
        return ResponseEntity.ok(ApiResponse.ok(couponService.getAllCoupons()));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PROMOTION_WRITE') or hasRole('SUPER_ADMIN') or hasRole('MARKETING')")
    @Operation(summary = "Admin: Tạo mới mã voucher giảm giá")
    public ResponseEntity<ApiResponse<CouponResponse>> createCoupon(@Valid @RequestBody CouponRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(couponService.createCoupon(request)));
    }
}
