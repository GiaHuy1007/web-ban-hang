package com.ecommerce.modules.promotion.service;

import com.ecommerce.common.exception.AppException;
import com.ecommerce.common.exception.ErrorCode;
import com.ecommerce.modules.catalog.entity.ProductVariant;
import com.ecommerce.modules.catalog.repository.ProductVariantRepository;
import com.ecommerce.modules.catalog.service.CategoryService;
import com.ecommerce.modules.promotion.dto.PromotionProductDto;
import com.ecommerce.modules.promotion.dto.PromotionRequest;
import com.ecommerce.modules.promotion.dto.PromotionResponse;
import com.ecommerce.modules.promotion.entity.Promotion;
import com.ecommerce.modules.promotion.entity.PromotionProduct;
import com.ecommerce.modules.promotion.repository.PromotionProductRepository;
import com.ecommerce.modules.promotion.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final PromotionProductRepository promotionProductRepository;
    private final ProductVariantRepository variantRepository;

    @Transactional(readOnly = true)
    public List<PromotionResponse> getActivePromotions() {
        return promotionRepository.findActivePromotions(LocalDateTime.now()).stream()
                .map(PromotionResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PromotionResponse getPromotionBySlug(String slug) {
        Promotion promotion = promotionRepository.findBySlug(slug)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy chương trình khuyến mãi."));
        return PromotionResponse.from(promotion);
    }

    @Transactional
    public PromotionResponse createPromotion(PromotionRequest request) {
        String slug = CategoryService.toSlug(request.getName());
        if (promotionRepository.findBySlug(slug).isPresent()) {
            slug = slug + "-" + System.currentTimeMillis();
        }

        Promotion promotion = Promotion.builder()
                .name(request.getName().trim())
                .slug(slug)
                .description(request.getDescription())
                .bannerUrl(request.getBannerUrl())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .isActive(request.getIsActive())
                .build();

        Promotion saved = promotionRepository.save(promotion);

        if (request.getProducts() != null) {
            List<PromotionProduct> promoProducts = new ArrayList<>();
            for (PromotionProductDto pDto : request.getProducts()) {
                ProductVariant variant = variantRepository.findById(pDto.getVariantId())
                        .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));

                PromotionProduct pp = PromotionProduct.builder()
                        .promotion(saved)
                        .variant(variant)
                        .promotionalPrice(pDto.getPromotionalPrice())
                        .quantityLimit(pDto.getQuantityLimit())
                        .quantitySold(0)
                        .build();

                promoProducts.add(promotionProductRepository.save(pp));
            }
            saved.setProducts(promoProducts);
        }

        return PromotionResponse.from(saved);
    }
}
