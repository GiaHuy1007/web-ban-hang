package com.ecommerce.modules.search.repository;

import com.ecommerce.modules.search.document.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, String> {

    Page<ProductDocument> findByNameContainingOrDescriptionContaining(String name, String description, Pageable pageable);

    List<ProductDocument> findTop10ByNameStartingWithIgnoreCase(String prefix);

    Page<ProductDocument> findByCategorySlug(String categorySlug, Pageable pageable);

    Page<ProductDocument> findByBrandSlug(String brandSlug, Pageable pageable);
}
