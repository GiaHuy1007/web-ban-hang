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
public class CategoryResponse {

    private Long id;
    private Long parentId;
    private String parentName;
    private String name;
    private String slug;
    private String iconUrl;
    private Integer level;
    private Integer sortOrder;
    private Boolean isActive;
    private List<AttributeTemplateResponse> attributeTemplates;

    public static CategoryResponse from(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .name(category.getName())
                .slug(category.getSlug())
                .iconUrl(category.getIconUrl())
                .level(category.getLevel())
                .sortOrder(category.getSortOrder())
                .isActive(category.getIsActive())
                .attributeTemplates(category.getAttributeTemplates() != null
                        ? category.getAttributeTemplates().stream().map(AttributeTemplateResponse::from).collect(Collectors.toList())
                        : List.of())
                .build();
    }
}
