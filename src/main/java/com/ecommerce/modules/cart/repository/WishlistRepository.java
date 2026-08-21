package com.ecommerce.modules.cart.repository;

import com.ecommerce.modules.cart.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    List<Wishlist> findByUserId(Long userId);

    Optional<Wishlist> findByUserIdAndVariantId(Long userId, Long variantId);

    boolean existsByUserIdAndVariantId(Long userId, Long variantId);

    void deleteByUserIdAndVariantId(Long userId, Long variantId);
}
