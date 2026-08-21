package com.ecommerce.modules.catalog.service;

import com.ecommerce.common.exception.AppException;
import com.ecommerce.common.exception.ErrorCode;
import com.ecommerce.modules.catalog.dto.CategoryRequest;
import com.ecommerce.modules.catalog.dto.CategoryResponse;
import com.ecommerce.modules.catalog.dto.CategoryTreeResponse;
import com.ecommerce.modules.catalog.entity.Category;
import com.ecommerce.modules.catalog.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    public static String toSlug(String input) {
        if (input == null) return "";
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH).replaceAll("-+", "-");
    }

    @Transactional(readOnly = true)
    public List<CategoryTreeResponse> getCategoryTree() {
        List<Category> roots = categoryRepository.findByParentIsNullAndIsActiveTrueOrderBySortOrderAsc();
        return roots.stream().map(CategoryTreeResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream().map(CategoryResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        return CategoryResponse.from(category);
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        String slug = toSlug(request.getName());
        if (categoryRepository.existsBySlug(slug)) {
            slug = slug + "-" + System.currentTimeMillis();
        }

        Category parent = null;
        int level = 1;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND, "Không tìm thấy danh mục cha."));
            level = parent.getLevel() + 1;
        }

        Category category = Category.builder()
                .parent(parent)
                .name(request.getName().trim())
                .slug(slug)
                .iconUrl(request.getIconUrl())
                .level(level)
                .sortOrder(request.getSortOrder())
                .isActive(request.getIsActive())
                .build();

        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        if (!category.getName().equalsIgnoreCase(request.getName())) {
            String slug = toSlug(request.getName());
            if (categoryRepository.existsBySlug(slug) && !slug.equals(category.getSlug())) {
                slug = slug + "-" + System.currentTimeMillis();
            }
            category.setSlug(slug);
        }

        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND, "Không tìm thấy danh mục cha."));
            category.setParent(parent);
            category.setLevel(parent.getLevel() + 1);
        } else {
            category.setParent(null);
            category.setLevel(1);
        }

        category.setName(request.getName().trim());
        category.setIconUrl(request.getIconUrl());
        category.setSortOrder(request.getSortOrder());
        category.setIsActive(request.getIsActive());

        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        categoryRepository.delete(category);
    }
}
