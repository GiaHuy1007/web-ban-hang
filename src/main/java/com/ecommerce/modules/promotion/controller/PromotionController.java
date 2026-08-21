package com.ecommerce.modules.promotion.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.modules.promotion.dto.PromotionRequest;
import com.ecommerce.modules.promotion.dto.PromotionResponse;
import com.ecommerce.modules.promotion.service.PromotionService;
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
@RequestMapping("/api/v1/promotions")
@RequiredArgsConstructor
@Tag(name = "Promotions & Flash Sales", description = "Các chiến dịch Flash Sale và sự kiện khuyến mãi")
public class PromotionController {

    private final PromotionService promotionService;

    @GetMapping
    @Operation(summary = "Lấy danh sách các chương trình khuyến mãi và Flash Sale đang diễn ra")
    public ResponseEntity<ApiResponse<List<PromotionResponse>>> getActivePromotions() {
        return ResponseEntity.ok(ApiResponse.ok(promotionService.getActivePromotions()));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Xem chi tiết chiến dịch khuyến mãi theo slug kèm danh sách sản phẩm Flash Sale")
    public ResponseEntity<ApiResponse<PromotionResponse>> getPromotionBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.ok(promotionService.getPromotionBySlug(slug)));
    }

    @PostMapping("/admin")
    @PreAuthorize("hasAuthority('PROMOTION_WRITE') or hasRole('SUPER_ADMIN') or hasRole('MARKETING')")
    @Operation(summary = "Admin: Tạo mới chiến dịch Flash Sale")
    public ResponseEntity<ApiResponse<PromotionResponse>> createPromotion(@Valid @RequestBody PromotionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(promotionService.createPromotion(request)));
    }
}
