package com.ecommerce.modules.catalog.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.modules.catalog.dto.BrandRequest;
import com.ecommerce.modules.catalog.dto.BrandResponse;
import com.ecommerce.modules.catalog.service.BrandService;
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
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
@Tag(name = "Catalog - Brands", description = "Quản lý và tra cứu các thương hiệu điện tử")
public class BrandController {

    private final BrandService brandService;

    @GetMapping
    @Operation(summary = "Lấy danh sách thương hiệu đang hoạt động")
    public ResponseEntity<ApiResponse<List<BrandResponse>>> getBrands() {
        return ResponseEntity.ok(ApiResponse.ok(brandService.getActiveBrands()));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Lấy thông tin thương hiệu theo slug")
    public ResponseEntity<ApiResponse<BrandResponse>> getBrandBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.ok(brandService.getBrandBySlug(slug)));
    }

    @PostMapping("/admin")
    @PreAuthorize("hasAuthority('PRODUCT_WRITE') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Admin: Thêm thương hiệu mới")
    public ResponseEntity<ApiResponse<BrandResponse>> createBrand(@Valid @RequestBody BrandRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(brandService.createBrand(request)));
    }

    @PutMapping("/admin/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_WRITE') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Admin: Cập nhật thương hiệu")
    public ResponseEntity<ApiResponse<BrandResponse>> updateBrand(
            @PathVariable Long id,
            @Valid @RequestBody BrandRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(brandService.updateBrand(id, request)));
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_WRITE') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Admin: Xóa thương hiệu")
    public ResponseEntity<ApiResponse<String>> deleteBrand(@PathVariable Long id) {
        brandService.deleteBrand(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa thương hiệu thành công."));
    }
}
