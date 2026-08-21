package com.ecommerce.modules.review.repository;

import com.ecommerce.modules.review.entity.Review;
import com.ecommerce.modules.review.entity.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByUserIdAndOrderItemId(Long userId, Long orderItemId);

    @Query("SELECT r FROM Review r WHERE r.variant.product.slug = :productSlug AND r.status = :status ORDER BY r.createdAt DESC")
    Page<Review> findByProductSlugAndStatus(@Param("productSlug") String productSlug, @Param("status") ReviewStatus status, Pageable pageable);

    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.variant.product.id = :productId AND r.status = 'APPROVED'")
    Double getAverageRatingForProduct(@Param("productId") Long productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.variant.product.id = :productId AND r.status = 'APPROVED'")
    Integer getReviewCountForProduct(@Param("productId") Long productId);
}
