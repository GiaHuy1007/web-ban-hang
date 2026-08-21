package com.ecommerce.modules.review.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.modules.review.dto.ReviewResponse;
import com.ecommerce.modules.review.entity.ReviewStatus;
import com.ecommerce.modules.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/reviews")
@RequiredArgsConstructor
@Tag(name = "Admin - Review Moderation", description = "Kiểm duyệt đánh giá của khách hàng (Duyệt/Ẩn)")
public class AdminReviewController {

    private final ReviewService reviewService;

    @PatchMapping("/{reviewId}/status")
    @PreAuthorize("hasAuthority('PRODUCT_WRITE') or hasRole('SUPER_ADMIN') or hasRole('CS')")
    @Operation(summary = "Admin: Cập nhật trạng thái kiểm duyệt đánh giá (APPROVED, REJECTED)")
    public ResponseEntity<ApiResponse<ReviewResponse>> moderateReview(
            @PathVariable Long reviewId,
            @RequestParam ReviewStatus status) {
        return ResponseEntity.ok(ApiResponse.ok(reviewService.adminModerateReview(reviewId, status)));
    }
}
