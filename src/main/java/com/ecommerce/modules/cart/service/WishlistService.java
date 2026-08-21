package com.ecommerce.modules.cart.service;

import com.ecommerce.common.exception.AppException;
import com.ecommerce.common.exception.ErrorCode;
import com.ecommerce.common.util.SecurityUtils;
import com.ecommerce.modules.cart.dto.WishlistResponse;
import com.ecommerce.modules.cart.entity.Wishlist;
import com.ecommerce.modules.cart.repository.WishlistRepository;
import com.ecommerce.modules.catalog.entity.ProductVariant;
import com.ecommerce.modules.catalog.repository.ProductVariantRepository;
import com.ecommerce.modules.identity.entity.User;
import com.ecommerce.modules.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<WishlistResponse> getWishlist() {
        Long userId = SecurityUtils.getCurrentUserId();
        return wishlistRepository.findByUserId(userId).stream()
                .map(WishlistResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addToWishlist(Long variantId) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (wishlistRepository.existsByUserIdAndVariantId(userId, variantId)) {
            return;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));

        Wishlist wishlist = Wishlist.builder()
                .user(user)
                .variant(variant)
                .build();

        wishlistRepository.save(wishlist);
    }

    @Transactional
    public void removeFromWishlist(Long variantId) {
        Long userId = SecurityUtils.getCurrentUserId();
        wishlistRepository.deleteByUserIdAndVariantId(userId, variantId);
    }
}
