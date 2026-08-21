package com.ecommerce.modules.catalog.repository;

import com.ecommerce.modules.catalog.entity.VariantAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VariantAttributeValueRepository extends JpaRepository<VariantAttributeValue, Long> {

    List<VariantAttributeValue> findByVariantId(Long variantId);
}
