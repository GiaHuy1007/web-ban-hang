package com.ecommerce.modules.catalog.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.modules.catalog.dto.*;
import com.ecommerce.modules.catalog.service.AttributeTemplateService;
import com.ecommerce.modules.catalog.service.CategoryService;
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
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Catalog - Categories & Attributes", description = "Duyệt cây danh mục và cấu hình thuộc tính kỹ thuật động")
public class CategoryController {

    private final CategoryService categoryService;
    private final AttributeTemplateService attributeTemplateService;

    @GetMapping("/tree")
    @Operation(summary = "Lấy toàn bộ cây danh mục đa cấp (Dùng cho header menu / sidebar)")
    public ResponseEntity<ApiResponse<List<CategoryTreeResponse>>> getCategoryTree() {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.getCategoryTree()));
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả danh mục (phẳng)")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.getAllCategories()));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Lấy chi tiết danh mục theo slug kèm bộ thuộc tính kỹ thuật")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.getCategoryBySlug(slug)));
    }

    @PostMapping("/admin")
    @PreAuthorize("hasAuthority('PRODUCT_WRITE') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Admin: Tạo mới danh mục sản phẩm")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(categoryService.createCategory(request)));
    }

    @PutMapping("/admin/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_WRITE') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Admin: Cập nhật thông tin danh mục")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.updateCategory(id, request)));
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_WRITE') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Admin: Xóa danh mục")
    public ResponseEntity<ApiResponse<String>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa danh mục thành công."));
    }

    // Attribute Templates Management
    @GetMapping("/{categoryId}/attributes")
    @Operation(summary = "Lấy danh sách thuộc tính kỹ thuật (EAV Templates) của một danh mục")
    public ResponseEntity<ApiResponse<List<AttributeTemplateResponse>>> getCategoryAttributes(@PathVariable Long categoryId) {
        return ResponseEntity.ok(ApiResponse.ok(attributeTemplateService.getTemplatesByCategory(categoryId)));
    }

    @PostMapping("/admin/{categoryId}/attributes")
    @PreAuthorize("hasAuthority('PRODUCT_WRITE') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Admin: Thêm thuộc tính kỹ thuật mới cho danh mục (CPU, RAM, GPU, Screen...)")
    public ResponseEntity<ApiResponse<AttributeTemplateResponse>> createAttributeTemplate(
            @PathVariable Long categoryId,
            @Valid @RequestBody AttributeTemplateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(attributeTemplateService.createTemplate(categoryId, request)));
    }
}
