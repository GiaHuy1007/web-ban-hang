package com.ecommerce.modules.review.dto;

import com.ecommerce.modules.review.entity.Review;
import com.ecommerce.modules.review.entity.ReviewStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

    private Long id;
    private Long variantId;
    private String variantName;
    private String variantSku;
    private String userFullName;
    private Integer rating;
    private String title;
    private String comment;
    private ReviewStatus status;
    private Integer helpfulCount;
    private List<String> images;
    private LocalDateTime createdAt;

    public static ReviewResponse from(Review review) {
        List<String> imgUrls = review.getImages() != null
                ? review.getImages().stream().map(img -> img.getImageUrl()).collect(Collectors.toList())
                : List.of();

        return ReviewResponse.builder()
                .id(review.getId())
                .variantId(review.getVariant().getId())
                .variantName(review.getVariant().getName())
                .variantSku(review.getVariant().getSku())
                .userFullName(review.getUser().getFullName())
                .rating(review.getRating())
                .title(review.getTitle())
                .comment(review.getComment())
                .status(review.getStatus())
                .helpfulCount(review.getHelpfulCount())
                .images(imgUrls)
                .createdAt(review.getCreatedAt())
                .build();
    }
}
