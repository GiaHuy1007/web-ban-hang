package com.ecommerce.modules.catalog.service;

import com.ecommerce.common.exception.AppException;
import com.ecommerce.common.exception.ErrorCode;
import com.ecommerce.modules.catalog.dto.*;
import com.ecommerce.modules.catalog.entity.*;
import com.ecommerce.modules.catalog.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final CategoryAttributeTemplateRepository templateRepository;
    private final ProductAttributeValueRepository productAttrRepo;
    private final VariantAttributeValueRepository variantAttrRepo;
    private final ProductImageRepository imageRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ProductDetailResponse createProduct(ProductCreateRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));

        String slug = CategoryService.toSlug(request.getName());
        if (productRepository.existsBySlug(slug)) {
            slug = slug + "-" + System.currentTimeMillis();
        }

        // 1. Create SPU Product
        Product product = Product.builder()
                .category(category)
                .brand(brand)
                .name(request.getName().trim())
                .slug(slug)
                .description(request.getDescription())
                .shortDescription(request.getShortDescription())
                .thumbnailUrl(request.getThumbnailUrl())
                .status(ProductStatus.PUBLISHED)
                .build();

        Product savedProduct = productRepository.save(product);

        // 2. Save Informational Attributes
        Map<String, CategoryAttributeTemplate> templateMap = templateRepository.findByCategoryIdOrderBySortOrderAsc(category.getId())
                .stream().collect(Collectors.toMap(CategoryAttributeTemplate::getCode, t -> t, (k1, k2) -> k1));

        if (request.getInformationalAttributes() != null) {
            for (AttributeValueDto attrDto : request.getInformationalAttributes()) {
                CategoryAttributeTemplate template = templateMap.get(attrDto.getCode().toLowerCase());
                if (template != null) {
                    ProductAttributeValue pav = ProductAttributeValue.builder()
                            .product(savedProduct)
                            .attributeTemplate(template)
                            .value(attrDto.getValue())
                            .build();
                    productAttrRepo.save(pav);
                }
            }
        }

        // 3. Save SPU Images
        if (request.getImageUrls() != null) {
            int order = 0;
            for (String imgUrl : request.getImageUrls()) {
                ProductImage img = ProductImage.builder()
                        .product(savedProduct)
                        .imageUrl(imgUrl)
                        .isThumbnail(order == 0)
                        .sortOrder(order++)
                        .build();
                imageRepository.save(img);
            }
        }

        // 4. Save SKU Variants
        if (savedProduct.getVariants() == null) {
            savedProduct.setVariants(new ArrayList<>());
        }
        for (VariantRequest vReq : request.getVariants()) {
            if (variantRepository.existsBySku(vReq.getSku())) {
                throw new AppException(ErrorCode.SKU_ALREADY_EXISTS, "Mã SKU '" + vReq.getSku() + "' đã tồn tại.");
            }

            ProductVariant variant = ProductVariant.builder()
                    .product(savedProduct)
                    .sku(vReq.getSku().trim().toUpperCase())
                    .name(vReq.getName().trim())
                    .basePrice(vReq.getBasePrice())
                    .salePrice(vReq.getSalePrice())
                    .isActive(vReq.getIsActive())
                    .build();

            ProductVariant savedVariant = variantRepository.save(variant);
            savedProduct.getVariants().add(savedVariant);

            if (vReq.getAttributeValues() != null) {
                for (AttributeValueDto vAttrDto : vReq.getAttributeValues()) {
                    CategoryAttributeTemplate template = templateMap.get(vAttrDto.getCode().toLowerCase());
                    if (template != null) {
                        VariantAttributeValue vav = VariantAttributeValue.builder()
                                .variant(savedVariant)
                                .attributeTemplate(template)
                                .value(vAttrDto.getValue())
                                .build();
                        variantAttrRepo.save(vav);
                    }
                }
            }

            if (vReq.getImageUrls() != null) {
                int vOrder = 0;
                for (String vImgUrl : vReq.getImageUrls()) {
                    ProductImage vImg = ProductImage.builder()
                            .product(savedProduct)
                            .variant(savedVariant)
                            .imageUrl(vImgUrl)
                            .sortOrder(vOrder++)
                            .build();
                    imageRepository.save(vImg);
                }
            }
        }

        log.info("Created product SPU: id={}, slug={}, variantsCount={}", savedProduct.getId(), savedProduct.getSlug(), request.getVariants().size());
        return ProductDetailResponse.from(savedProduct);
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse getProductDetailBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        return ProductDetailResponse.from(product);
    }

    @Transactional(readOnly = true)
    public VariantResponse getVariantBySku(String sku) {
        ProductVariant variant = variantRepository.findBySku(sku)
                .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));
        return VariantResponse.from(variant);
    }

    @Transactional(readOnly = true)
    public VariantResponse getVariantByProductSlugAndSku(String productSlug, String sku) {
        ProductVariant variant = variantRepository.findByProductSlugAndSku(productSlug, sku)
                .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));
        return VariantResponse.from(variant);
    }

    @Transactional
    public ProductDetailResponse updateProductStatus(Long id, ProductStatus status) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        product.setStatus(status);
        productRepository.save(product);
        return ProductDetailResponse.from(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        productRepository.delete(product);
    }
}
