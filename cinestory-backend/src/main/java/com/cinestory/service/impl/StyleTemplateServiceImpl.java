package com.cinestory.service.impl;

import com.cinestory.exception.ResourceNotFoundException;
import com.cinestory.model.entity.StyleTemplate;
import com.cinestory.repository.StyleTemplateRepository;
import com.cinestory.service.StyleTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 风格模板服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StyleTemplateServiceImpl implements StyleTemplateService {

    private final StyleTemplateRepository styleTemplateRepository;

    @Override
    public Page<StyleTemplate> getAllStyles(Pageable pageable) {
        return styleTemplateRepository.findAll(pageable);
    }

    @Override
    public Page<StyleTemplate> getByCategory(String category, Pageable pageable) {
        return styleTemplateRepository.findByCategory(category, pageable);
    }

    @Override
    public List<StyleTemplate> getSystemStyles() {
        return styleTemplateRepository.findByIsSystemTrueOrderByCategoryAscNameAsc();
    }

    @Override
    public List<StyleTemplate> getCustomStyles() {
        return styleTemplateRepository.findByIsSystemFalseOrderByCreatedAtDesc();
    }

    @Override
    public StyleTemplate getById(Long id) {
        return styleTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Style template not found with id: " + id));
    }

    @Override
    public List<String> getCategories() {
        return styleTemplateRepository.findAllCategories();
    }

    @Override
    @Transactional
    public StyleTemplate create(StyleTemplate styleTemplate) {
        styleTemplate.setIsSystem(false);
        return styleTemplateRepository.save(styleTemplate);
    }

    @Override
    @Transactional
    public StyleTemplate update(Long id, StyleTemplate styleTemplate) {
        StyleTemplate existing = getById(id);

        if (existing.getIsSystem()) {
            throw new IllegalStateException("Cannot update system style template");
        }

        styleTemplate.setId(id);
        styleTemplate.setIsSystem(false);
        return styleTemplateRepository.save(styleTemplate);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        StyleTemplate styleTemplate = getById(id);

        if (styleTemplate.getIsSystem()) {
            throw new IllegalStateException("Cannot delete system style template");
        }

        styleTemplateRepository.deleteById(id);
    }
}
