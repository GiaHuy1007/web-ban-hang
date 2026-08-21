package com.ecommerce.modules.search.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.response.PageResponse;
import com.ecommerce.modules.search.document.ProductDocument;
import com.ecommerce.modules.search.service.ProductSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Tag(name = "Elasticsearch Search Engine", description = "Tìm kiếm toàn văn (Full-Text Search) và gợi ý từ khóa (Autocomplete)")
public class SearchController {

    private final ProductSearchService searchService;

    @GetMapping
    @Operation(summary = "Tìm kiếm sản phẩm full-text qua Elasticsearch")
    public ResponseEntity<ApiResponse<PageResponse<ProductDocument>>> search(
            @RequestParam String q,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(searchService.search(q, pageable))));
    }

    @GetMapping("/autocomplete")
    @Operation(summary = "Gợi ý từ khóa tự động (Autocomplete) khi người dùng gõ tìm kiếm")
    public ResponseEntity<ApiResponse<List<String>>> autocomplete(@RequestParam String q) {
        return ResponseEntity.ok(ApiResponse.ok(searchService.autocomplete(q)));
    }
}
