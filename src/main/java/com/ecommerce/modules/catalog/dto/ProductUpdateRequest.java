package com.ecommerce.modules.catalog.dto;

import com.ecommerce.modules.catalog.entity.ProductStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateRequest {

    @NotNull(message = "Danh mục không được để trống.")
    private Long categoryId;

    @NotNull(message = "Thương hiệu không được để trống.")
    private Long brandId;

    @NotBlank(message = "Tên sản phẩm không được để trống.")
    private String name;

    private String description;
    private String shortDescription;
    private String thumbnailUrl;
    private ProductStatus status;

    @Builder.Default
    private List<AttributeValueDto> informationalAttributes = new ArrayList<>();

    @Builder.Default
    private List<String> imageUrls = new ArrayList<>();
}
