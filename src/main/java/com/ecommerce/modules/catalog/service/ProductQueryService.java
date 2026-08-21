package com.ecommerce.modules.catalog.service;

import com.ecommerce.modules.catalog.dto.ProductFilterCriteria;
import com.ecommerce.modules.catalog.dto.ProductSummaryResponse;
import com.ecommerce.modules.catalog.entity.Product;
import com.ecommerce.modules.catalog.entity.ProductStatus;
import com.ecommerce.modules.catalog.entity.ProductVariant;
import com.ecommerce.modules.catalog.repository.ProductRepository;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductQueryService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public Page<ProductSummaryResponse> filterProducts(ProductFilterCriteria criteria, Pageable pageable) {
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Only PUBLISHED products
            predicates.add(cb.equal(root.get("status"), ProductStatus.PUBLISHED));

            // 2. Filter by Category Slug (including parent/child)
            if (criteria.getCategorySlug() != null && !criteria.getCategorySlug().isBlank()) {
                Path<String> catSlug = root.join("category", JoinType.LEFT).get("slug");
                predicates.add(cb.equal(catSlug, criteria.getCategorySlug()));
            }

            // 3. Filter by Brand Slug
            if (criteria.getBrandSlug() != null && !criteria.getBrandSlug().isBlank()) {
                Path<String> brandSlug = root.join("brand", JoinType.LEFT).get("slug");
                predicates.add(cb.equal(brandSlug, criteria.getBrandSlug()));
            }

            // 4. Filter by Keyword (search in name)
            if (criteria.getKeyword() != null && !criteria.getKeyword().isBlank()) {
                String pattern = "%" + criteria.getKeyword().trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), pattern));
            }

            // 5. Price Filter on Variants
            if (criteria.getMinPrice() != null || criteria.getMaxPrice() != null) {
                Join<Product, ProductVariant> variantJoin = root.join("variants", JoinType.INNER);
                if (criteria.getMinPrice() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(variantJoin.get("basePrice"), criteria.getMinPrice()));
                }
                if (criteria.getMaxPrice() != null) {
                    predicates.add(cb.lessThanOrEqualTo(variantJoin.get("basePrice"), criteria.getMaxPrice()));
                }
            }

            if (query != null) {
                query.distinct(true);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // Determine Sort
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        if ("price_asc".equalsIgnoreCase(criteria.getSortBy())) {
            sort = Sort.by(Sort.Direction.ASC, "ratingAverage");
        } else if ("rating".equalsIgnoreCase(criteria.getSortBy())) {
            sort = Sort.by(Sort.Direction.DESC, "ratingAverage");
        }

        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        return productRepository.findAll(spec, sortedPageable).map(ProductSummaryResponse::from);
    }
}
