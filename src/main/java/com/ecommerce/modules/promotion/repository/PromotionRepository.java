package com.ecommerce.modules.promotion.repository;

import com.ecommerce.modules.promotion.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    Optional<Promotion> findBySlug(String slug);

    @Query("SELECT p FROM Promotion p WHERE p.isActive = true AND :now BETWEEN p.startTime AND p.endTime")
    List<Promotion> findActivePromotions(@Param("now") LocalDateTime now);
}
