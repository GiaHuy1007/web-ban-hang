package com.ecommerce.modules.catalog.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.modules.catalog.dto.ProductCreateRequest;
import com.ecommerce.modules.catalog.dto.ProductDetailResponse;
import com.ecommerce.modules.catalog.entity.ProductStatus;
import com.ecommerce.modules.catalog.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
@Tag(name = "Admin - Product Management", description = "Quản lý sản phẩm SPU và biến thể SKU dành cho Product Manager và Admin")
public class AdminProductController {

    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasAuthority('PRODUCT_WRITE') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Admin: Tạo mới sản phẩm (SPU) kèm các biến thể (SKUs) và thuộc tính kỹ thuật")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(productService.createProduct(request)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('PRODUCT_WRITE') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Admin: Cập nhật trạng thái sản phẩm (DRAFT, PUBLISHED, ARCHIVED)")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam ProductStatus status) {
        return ResponseEntity.ok(ApiResponse.ok(productService.updateProductStatus(id, status)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_WRITE') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Admin: Xóa sản phẩm")
    public ResponseEntity<ApiResponse<String>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa sản phẩm thành công."));
    }
}
