package com.ecommerce.modules.catalog.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
public class ProductCreateRequest {

    @NotNull(message = "Danh mục sản phẩm không được để trống.")
    private Long categoryId;

    @NotNull(message = "Thương hiệu không được để trống.")
    private Long brandId;

    @NotBlank(message = "Tên sản phẩm không được để trống.")
    private String name;

    private String description;
    private String shortDescription;
    private String thumbnailUrl;

    @Builder.Default
    private List<AttributeValueDto> informationalAttributes = new ArrayList<>();

    @NotEmpty(message = "Sản phẩm phải có ít nhất 1 biến thể SKU.")
    @Valid
    @Builder.Default
    private List<VariantRequest> variants = new ArrayList<>();

    @Builder.Default
    private List<String> imageUrls = new ArrayList<>();
}
