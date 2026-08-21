package com.ecommerce.modules.catalog.service;

import com.ecommerce.common.exception.AppException;
import com.ecommerce.common.exception.ErrorCode;
import com.ecommerce.modules.catalog.dto.BrandRequest;
import com.ecommerce.modules.catalog.dto.BrandResponse;
import com.ecommerce.modules.catalog.entity.Brand;
import com.ecommerce.modules.catalog.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;

    @Transactional(readOnly = true)
    public List<BrandResponse> getActiveBrands() {
        return brandRepository.findByIsActiveTrueOrderByNameAsc().stream()
                .map(BrandResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BrandResponse> getAllBrands() {
        return brandRepository.findAll().stream().map(BrandResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BrandResponse getBrandBySlug(String slug) {
        Brand brand = brandRepository.findBySlug(slug)
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));
        return BrandResponse.from(brand);
    }

    @Transactional
    public BrandResponse createBrand(BrandRequest request) {
        String slug = CategoryService.toSlug(request.getName());
        if (brandRepository.existsBySlug(slug)) {
            slug = slug + "-" + System.currentTimeMillis();
        }

        Brand brand = Brand.builder()
                .name(request.getName().trim())
                .slug(slug)
                .logoUrl(request.getLogoUrl())
                .description(request.getDescription())
                .isActive(request.getIsActive())
                .build();

        return BrandResponse.from(brandRepository.save(brand));
    }

    @Transactional
    public BrandResponse updateBrand(Long id, BrandRequest request) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));

        if (!brand.getName().equalsIgnoreCase(request.getName())) {
            String slug = CategoryService.toSlug(request.getName());
            if (brandRepository.existsBySlug(slug) && !slug.equals(brand.getSlug())) {
                slug = slug + "-" + System.currentTimeMillis();
            }
            brand.setSlug(slug);
        }

        brand.setName(request.getName().trim());
        brand.setLogoUrl(request.getLogoUrl());
        brand.setDescription(request.getDescription());
        brand.setIsActive(request.getIsActive());

        return BrandResponse.from(brandRepository.save(brand));
    }

    @Transactional
    public void deleteBrand(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));
        brandRepository.delete(brand);
    }
}
