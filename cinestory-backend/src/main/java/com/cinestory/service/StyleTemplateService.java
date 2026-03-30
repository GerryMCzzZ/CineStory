package com.cinestory.service;

import com.cinestory.model.entity.StyleTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 风格模板服务接口
 */
public interface StyleTemplateService {

    /**
     * 获取所有风格模板（分页）
     */
    Page<StyleTemplate> getAllStyles(Pageable pageable);

    /**
     * 获取系统预设风格
     */
    List<StyleTemplate> getSystemStyles();

    /**
     * 获取自定义风格
     */
    List<StyleTemplate> getCustomStyles();

    /**
     * 根据 ID 获取风格模板
     */
    StyleTemplate getById(Long id);

    /**
     * 创建风格模板
     */
    StyleTemplate create(StyleTemplate styleTemplate);

    /**
     * 更新风格模板
     */
    StyleTemplate update(Long id, StyleTemplate styleTemplate);

    /**
     * 删除风格模板
     */
    void delete(Long id);
}
