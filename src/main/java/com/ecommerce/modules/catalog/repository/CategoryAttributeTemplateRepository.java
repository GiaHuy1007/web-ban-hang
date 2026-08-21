package com.ecommerce.modules.catalog.repository;

import com.ecommerce.modules.catalog.entity.CategoryAttributeTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryAttributeTemplateRepository extends JpaRepository<CategoryAttributeTemplate, Long> {

    List<CategoryAttributeTemplate> findByCategoryIdOrderBySortOrderAsc(Long categoryId);

    List<CategoryAttributeTemplate> findByCategoryIdAndIsVariantDefiningTrueOrderBySortOrderAsc(Long categoryId);

    List<CategoryAttributeTemplate> findByCategoryIdAndIsFilterableTrueOrderBySortOrderAsc(Long categoryId);

    Optional<CategoryAttributeTemplate> findByCategoryIdAndCode(Long categoryId, String code);
}
