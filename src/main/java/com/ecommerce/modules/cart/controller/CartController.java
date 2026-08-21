package com.ecommerce.modules.cart.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.modules.cart.dto.AddToCartRequest;
import com.ecommerce.modules.cart.dto.CartResponse;
import com.ecommerce.modules.cart.dto.UpdateCartItemRequest;
import com.ecommerce.modules.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Shopping Cart", description = "Quản lý giỏ hàng của khách hàng đã đăng nhập")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Xem giỏ hàng hiện tại kèm giá và trạng thái tồn kho thực tế")
    public ResponseEntity<ApiResponse<CartResponse>> getCart() {
        return ResponseEntity.ok(ApiResponse.ok(cartService.getCart()));
    }

    @PostMapping("/items")
    @Operation(summary = "Thêm sản phẩm biến thể (SKU) vào giỏ hàng")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(@Valid @RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(cartService.addToCart(request)));
    }

    @PatchMapping("/items/{itemId}")
    @Operation(summary = "Cập nhật số lượng sản phẩm trong giỏ")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(cartService.updateCartItem(itemId, request)));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Xóa một sản phẩm khỏi giỏ hàng")
    public ResponseEntity<ApiResponse<CartResponse>> removeCartItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(ApiResponse.ok(cartService.removeCartItem(itemId)));
    }
}
