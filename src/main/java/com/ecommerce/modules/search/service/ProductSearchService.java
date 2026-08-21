package com.ecommerce.modules.search.service;

import com.ecommerce.modules.catalog.entity.Product;
import com.ecommerce.modules.catalog.entity.ProductVariant;
import com.ecommerce.modules.catalog.repository.ProductRepository;
import com.ecommerce.modules.inventory.service.InventoryService;
import com.ecommerce.modules.search.document.ProductDocument;
import com.ecommerce.modules.search.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSearchService {

    private final ProductSearchRepository searchRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;

    @Transactional(readOnly = true)
    public void syncProductToElasticsearch(Long productId) {
        try {
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null || !"PUBLISHED".equals(product.getStatus().name())) {
                searchRepository.deleteById(String.valueOf(productId));
                return;
            }

            List<ProductVariant> variants = product.getVariants();
            Double minPrice = 0.0;
            Double maxPrice = 0.0;
            boolean hasStock = false;

            if (variants != null && !variants.isEmpty()) {
                minPrice = variants.stream()
                        .map(ProductVariant::getEffectivePrice)
                        .min(Comparator.naturalOrder())
                        .map(BigDecimal::doubleValue)
                        .orElse(0.0);

                maxPrice = variants.stream()
                        .map(ProductVariant::getEffectivePrice)
                        .max(Comparator.naturalOrder())
                        .map(BigDecimal::doubleValue)
                        .orElse(0.0);

                for (ProductVariant v : variants) {
                    if (inventoryService.getAvailableStock(v.getId()) > 0) {
                        hasStock = true;
                        break;
                    }
                }
            }

            Map<String, Object> attributesMap = new HashMap<>();
            if (product.getAttributeValues() != null) {
                product.getAttributeValues().forEach(pav ->
                        attributesMap.put(pav.getAttributeTemplate().getCode(), pav.getValue()));
            }

            ProductDocument doc = ProductDocument.builder()
                    .id(String.valueOf(product.getId()))
                    .name(product.getName())
                    .slug(product.getSlug())
                    .categoryId(product.getCategory().getId())
                    .categoryName(product.getCategory().getName())
                    .categorySlug(product.getCategory().getSlug())
                    .brandId(product.getBrand().getId())
                    .brandName(product.getBrand().getName())
                    .brandSlug(product.getBrand().getSlug())
                    .description(product.getDescription())
                    .shortDescription(product.getShortDescription())
                    .thumbnailUrl(product.getThumbnailUrl())
                    .minPrice(minPrice)
                    .maxPrice(maxPrice)
                    .ratingAverage(product.getRatingAverage() != null ? product.getRatingAverage().doubleValue() : 0.0)
                    .reviewCount(product.getReviewCount())
                    .inStock(hasStock)
                    .status(product.getStatus().name())
                    .attributes(attributesMap)
                    .build();

            searchRepository.save(doc);
            log.info("Synchronized product to Elasticsearch: id={}, name={}", product.getId(), product.getName());
        } catch (Exception e) {
            log.warn("Could not sync product {} to Elasticsearch (ES may be offline in dev): {}", productId, e.getMessage());
        }
    }

    public Page<ProductDocument> search(String query, Pageable pageable) {
        return searchRepository.findByNameContainingOrDescriptionContaining(query, query, pageable);
    }

    public List<String> autocomplete(String prefix) {
        return searchRepository.findTop10ByNameStartingWithIgnoreCase(prefix).stream()
                .map(ProductDocument::getName)
                .collect(Collectors.toList());
    }
}
