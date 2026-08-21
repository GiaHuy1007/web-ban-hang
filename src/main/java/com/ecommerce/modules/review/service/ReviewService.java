package com.ecommerce.modules.review.service;

import com.ecommerce.common.exception.AppException;
import com.ecommerce.common.exception.ErrorCode;
import com.ecommerce.common.util.SecurityUtils;
import com.ecommerce.modules.catalog.entity.Product;
import com.ecommerce.modules.catalog.repository.ProductRepository;
import com.ecommerce.modules.identity.entity.User;
import com.ecommerce.modules.identity.repository.UserRepository;
import com.ecommerce.modules.order.entity.OrderItem;
import com.ecommerce.modules.order.entity.OrderStatus;
import com.ecommerce.modules.order.repository.OrderItemRepository;
import com.ecommerce.modules.review.dto.CreateReviewRequest;
import com.ecommerce.modules.review.dto.ReviewResponse;
import com.ecommerce.modules.review.entity.Review;
import com.ecommerce.modules.review.entity.ReviewImage;
import com.ecommerce.modules.review.entity.ReviewStatus;
import com.ecommerce.modules.review.repository.ReviewImageRepository;
import com.ecommerce.modules.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository imageRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Transactional
    public ReviewResponse createReview(CreateReviewRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        OrderItem orderItem = orderItemRepository.findById(request.getOrderItemId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy sản phẩm trong đơn hàng."));

        // 1. Check Ownership & Verified Purchase (Must be DELIVERED)
        if (!orderItem.getOrder().getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.FORBIDDEN, "Bạn không có quyền đánh giá sản phẩm của đơn hàng này.");
        }

        if (orderItem.getOrder().getStatus() != OrderStatus.DELIVERED) {
            throw new AppException(ErrorCode.NOT_VERIFIED_PURCHASE, "Chỉ có thể đánh giá sản phẩm sau khi đơn hàng đã được giao thành công.");
        }

        // 2. Check Duplicate Review
        if (reviewRepository.existsByUserIdAndOrderItemId(userId, orderItem.getId())) {
            throw new AppException(ErrorCode.REVIEW_ALREADY_EXISTS, "Bạn đã viết đánh giá cho sản phẩm này rồi.");
        }

        // 3. Save Review
        Review review = Review.builder()
                .variant(orderItem.getVariant())
                .user(user)
                .orderItem(orderItem)
                .rating(request.getRating())
                .title(request.getTitle())
                .comment(request.getComment().trim())
                .status(ReviewStatus.APPROVED) // Default auto-approve
                .build();

        Review saved = reviewRepository.save(review);

        // 4. Save Review Images
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            List<ReviewImage> images = new ArrayList<>();
            int order = 0;
            for (String url : request.getImageUrls()) {
                images.add(imageRepository.save(ReviewImage.builder()
                        .review(saved)
                        .imageUrl(url)
                        .sortOrder(order++)
                        .build()));
            }
            saved.setImages(images);
        }

        // 5. Recalculate average rating for SPU
        Product product = orderItem.getVariant().getProduct();
        Double avgRating = reviewRepository.getAverageRatingForProduct(product.getId());
        Integer reviewCount = reviewRepository.getReviewCountForProduct(product.getId());

        if (avgRating != null) {
            product.setRatingAverage(BigDecimal.valueOf(avgRating).setScale(2, RoundingMode.HALF_UP));
        }
        if (reviewCount != null) {
            product.setReviewCount(reviewCount);
        }
        productRepository.save(product);

        log.info("Review created for product {}: rating={}", product.getName(), request.getRating());
        return ReviewResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getProductReviews(String productSlug, Pageable pageable) {
        return reviewRepository.findByProductSlugAndStatus(productSlug, ReviewStatus.APPROVED, pageable)
                .map(ReviewResponse::from);
    }

    @Transactional
    public ReviewResponse adminModerateReview(Long reviewId, ReviewStatus status) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));
        review.setStatus(status);
        Review saved = reviewRepository.save(review);

        // Recalculate
        Product product = review.getVariant().getProduct();
        Double avgRating = reviewRepository.getAverageRatingForProduct(product.getId());
        Integer reviewCount = reviewRepository.getReviewCountForProduct(product.getId());
        if (avgRating != null) product.setRatingAverage(BigDecimal.valueOf(avgRating).setScale(2, RoundingMode.HALF_UP));
        if (reviewCount != null) product.setReviewCount(reviewCount);
        productRepository.save(product);

        return ReviewResponse.from(saved);
    }
}
