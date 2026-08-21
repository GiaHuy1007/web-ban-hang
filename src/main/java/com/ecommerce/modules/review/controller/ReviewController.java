package com.ecommerce.modules.review.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.response.PageResponse;
import com.ecommerce.modules.review.dto.CreateReviewRequest;
import com.ecommerce.modules.review.dto.ReviewResponse;
import com.ecommerce.modules.review.service.ReviewService;
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
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews & Ratings", description = "Đánh giá sản phẩm đã mua (Verified Purchase) và xem danh sách nhận xét")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "Khách hàng viết đánh giá cho sản phẩm đã nhận thành công")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(@Valid @RequestBody CreateReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(reviewService.createReview(request)));
    }

    @GetMapping("/products/{slug}")
    @Operation(summary = "Xem danh sách các đánh giá đã duyệt của một sản phẩm theo slug")
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getProductReviews(
            @PathVariable String slug,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(reviewService.getProductReviews(slug, pageable))));
    }
}
