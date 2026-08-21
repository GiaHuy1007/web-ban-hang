package com.ecommerce.modules.catalog.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.response.PageResponse;
import com.ecommerce.modules.catalog.dto.*;
import com.ecommerce.modules.catalog.service.ProductQueryService;
import com.ecommerce.modules.catalog.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Catalog - Products (Public)", description = "Duyệt danh sách sản phẩm, lọc theo giá/thương hiệu/thuộc tính và xem chi tiết SPU/SKU")
public class ProductController {

    private final ProductService productService;
    private final ProductQueryService productQueryService;

    @GetMapping
    @Operation(summary = "Duyệt danh sách sản phẩm với bộ lọc đa tiêu chí (Category, Brand, Price, Sorting)")
    public ResponseEntity<ApiResponse<PageResponse<ProductSummaryResponse>>> getProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String q,
            @RequestParam(required = false, defaultValue = "newest") String sort,
            @PageableDefault(size = 20) Pageable pageable) {

        ProductFilterCriteria criteria = ProductFilterCriteria.builder()
                .categorySlug(category)
                .brandSlug(brand)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .keyword(q)
                .sortBy(sort)
                .build();

        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(productQueryService.filterProducts(criteria, pageable))));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Xem chi tiết sản phẩm (SPU) kèm thông số kỹ thuật và các biến thể (SKUs)")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductDetail(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.ok(productService.getProductDetailBySlug(slug)));
    }

    @GetMapping("/{slug}/variants/{sku}")
    @Operation(summary = "Xem chi tiết 1 biến thể SKU cụ thể của sản phẩm")
    public ResponseEntity<ApiResponse<VariantResponse>> getVariantDetail(
            @PathVariable String slug,
            @PathVariable String sku) {
        return ResponseEntity.ok(ApiResponse.ok(productService.getVariantByProductSlugAndSku(slug, sku)));
    }
}
