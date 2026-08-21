package com.ecommerce.modules.catalog.service;

import com.ecommerce.common.exception.AppException;
import com.ecommerce.common.exception.ErrorCode;
import com.ecommerce.modules.catalog.dto.AttributeTemplateRequest;
import com.ecommerce.modules.catalog.dto.AttributeTemplateResponse;
import com.ecommerce.modules.catalog.entity.Category;
import com.ecommerce.modules.catalog.entity.CategoryAttributeTemplate;
import com.ecommerce.modules.catalog.repository.CategoryAttributeTemplateRepository;
import com.ecommerce.modules.catalog.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttributeTemplateService {

    private final CategoryAttributeTemplateRepository templateRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<AttributeTemplateResponse> getTemplatesByCategory(Long categoryId) {
        return templateRepository.findByCategoryIdOrderBySortOrderAsc(categoryId).stream()
                .map(AttributeTemplateResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public AttributeTemplateResponse createTemplate(Long categoryId, AttributeTemplateRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        String code = request.getCode().trim().toLowerCase();
        if (templateRepository.findByCategoryIdAndCode(categoryId, code).isPresent()) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Mã code thuộc tính '" + code + "' đã tồn tại trong danh mục này.");
        }

        CategoryAttributeTemplate template = CategoryAttributeTemplate.builder()
                .category(category)
                .attributeName(request.getAttributeName().trim())
                .code(code)
                .dataType(request.getDataType())
                .unit(request.getUnit())
                .isVariantDefining(request.getIsVariantDefining())
                .isFilterable(request.getIsFilterable())
                .isRequired(request.getIsRequired())
                .sortOrder(request.getSortOrder())
                .build();

        return AttributeTemplateResponse.from(templateRepository.save(template));
    }

    @Transactional
    public AttributeTemplateResponse updateTemplate(Long templateId, AttributeTemplateRequest request) {
        CategoryAttributeTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new AppException(ErrorCode.ATTRIBUTE_TEMPLATE_NOT_FOUND));

        template.setAttributeName(request.getAttributeName().trim());
        template.setDataType(request.getDataType());
        template.setUnit(request.getUnit());
        template.setIsVariantDefining(request.getIsVariantDefining());
        template.setIsFilterable(request.getIsFilterable());
        template.setIsRequired(request.getIsRequired());
        template.setSortOrder(request.getSortOrder());

        return AttributeTemplateResponse.from(templateRepository.save(template));
    }

    @Transactional
    public void deleteTemplate(Long templateId) {
        CategoryAttributeTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new AppException(ErrorCode.ATTRIBUTE_TEMPLATE_NOT_FOUND));
        templateRepository.delete(template);
    }
}
