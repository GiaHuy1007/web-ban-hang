package com.ecommerce.modules.catalog.repository;

import com.ecommerce.modules.catalog.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    Optional<ProductVariant> findBySku(String sku);

    boolean existsBySku(String sku);

    List<ProductVariant> findByProductIdAndIsActiveTrue(Long productId);

    @Query("SELECT v FROM ProductVariant v WHERE v.product.slug = :productSlug AND v.sku = :sku AND v.isActive = true")
    Optional<ProductVariant> findByProductSlugAndSku(@Param("productSlug") String productSlug, @Param("sku") String sku);
}
