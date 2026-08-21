package com.ecommerce.modules.promotion.repository;

import com.ecommerce.modules.promotion.entity.PromotionProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionProductRepository extends JpaRepository<PromotionProduct, Long> {

    List<PromotionProduct> findByPromotionId(Long promotionId);

    Optional<PromotionProduct> findByPromotionIdAndVariantId(Long promotionId, Long variantId);
}
