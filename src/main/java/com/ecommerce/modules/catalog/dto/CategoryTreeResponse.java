package com.ecommerce.modules.catalog.dto;

import com.ecommerce.modules.catalog.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryTreeResponse {

    private Long id;
    private String name;
    private String slug;
    private String iconUrl;
    private Integer level;
    private List<CategoryTreeResponse> children;

    public static CategoryTreeResponse from(Category category) {
        return CategoryTreeResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .iconUrl(category.getIconUrl())
                .level(category.getLevel())
                .children(category.getChildren() != null && !category.getChildren().isEmpty()
                        ? category.getChildren().stream()
                            .filter(Category::getIsActive)
                            .map(CategoryTreeResponse::from)
                            .collect(Collectors.toList())
                        : List.of())
                .build();
    }
}
