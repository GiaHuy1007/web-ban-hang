package com.ecommerce.modules.cart.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.modules.cart.dto.WishlistResponse;
import com.ecommerce.modules.cart.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
@Tag(name = "Wishlist", description = "Quản lý danh sách sản phẩm yêu thích của người dùng")
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    @Operation(summary = "Lấy danh sách sản phẩm yêu thích của tài khoản")
    public ResponseEntity<ApiResponse<List<WishlistResponse>>> getWishlist() {
        return ResponseEntity.ok(ApiResponse.ok(wishlistService.getWishlist()));
    }

    @PostMapping("/{variantId}")
    @Operation(summary = "Thêm biến thể sản phẩm vào danh sách yêu thích")
    public ResponseEntity<ApiResponse<String>> addToWishlist(@PathVariable Long variantId) {
        wishlistService.addToWishlist(variantId);
        return ResponseEntity.ok(ApiResponse.ok("Đã thêm vào danh sách yêu thích."));
    }

    @DeleteMapping("/{variantId}")
    @Operation(summary = "Xóa biến thể sản phẩm khỏi danh sách yêu thích")
    public ResponseEntity<ApiResponse<String>> removeFromWishlist(@PathVariable Long variantId) {
        wishlistService.removeFromWishlist(variantId);
        return ResponseEntity.ok(ApiResponse.ok("Đã xóa khỏi danh sách yêu thích."));
    }
}
