package com.cinestory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cinestory.exception.ResourceNotFoundException;
import com.cinestory.mapper.StyleTemplateMapper;
import com.cinestory.model.entity.StyleTemplate;
import com.cinestory.service.StyleTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final StyleTemplateMapper styleTemplateMapper;

    @Override
    public IPage<StyleTemplate> getAllStyles(IPage<StyleTemplate> page) {
        return styleTemplateMapper.selectPage(page, null);
    }

    @Override
    public List<StyleTemplate> getSystemStyles() {
        return styleTemplateMapper.selectList(new LambdaQueryWrapper<StyleTemplate>()
                .eq(StyleTemplate::getIsSystem, true)
                .orderByAsc(StyleTemplate::getCreatedAt));
    }

    @Override
    public List<StyleTemplate> getCustomStyles() {
        return styleTemplateMapper.selectList(new LambdaQueryWrapper<StyleTemplate>()
                .eq(StyleTemplate::getIsSystem, false)
                .orderByDesc(StyleTemplate::getCreatedAt));
    }

    @Override
    public StyleTemplate getById(Long id) {
        StyleTemplate styleTemplate = styleTemplateMapper.selectById(id);
        if (styleTemplate == null) {
            throw new ResourceNotFoundException("Style template not found with id: " + id);
        }
        return styleTemplate;
    }

    @Override
    @Transactional
    public StyleTemplate create(StyleTemplate styleTemplate) {
        styleTemplate.setIsSystem(false);
        styleTemplateMapper.insert(styleTemplate);
        return styleTemplate;
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
        styleTemplateMapper.updateById(styleTemplate);
        return styleTemplate;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        StyleTemplate styleTemplate = getById(id);

        if (styleTemplate.getIsSystem()) {
            throw new IllegalStateException("Cannot delete system style template");
        }

        styleTemplateMapper.deleteById(id);
    }
}
