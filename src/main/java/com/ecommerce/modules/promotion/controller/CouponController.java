package com.ecommerce.modules.promotion.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.modules.promotion.dto.ValidateCouponRequest;
import com.ecommerce.modules.promotion.dto.ValidateCouponResponse;
import com.ecommerce.modules.promotion.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
@Tag(name = "Coupons & Discounts (Customer)", description = "Kiểm tra và áp dụng mã giảm giá khi thanh toán")
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/validate")
    @Operation(summary = "Kiểm tra tính hợp lệ của mã giảm giá và tính toán số tiền chiết khấu")
    public ResponseEntity<ApiResponse<ValidateCouponResponse>> validateCoupon(
            @Valid @RequestBody ValidateCouponRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(couponService.validateCoupon(request)));
    }
}
