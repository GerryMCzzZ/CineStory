package com.cinestory.repository;

import com.cinestory.model.entity.StyleTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 风格模板数据访问接口
 */
@Repository
public interface StyleTemplateRepository extends JpaRepository<StyleTemplate, Long> {

    /**
     * 查询所有系统预设模板
     */
    List<StyleTemplate> findByIsSystemTrueOrderByCreatedAtAsc();

    /**
     * 查询系统预设模板（按分类和名称排序）
     */
    List<StyleTemplate> findByIsSystemTrueOrderByCategoryAscNameAsc();

    /**
     * 查询自定义模板
     */
    List<StyleTemplate> findByIsSystemFalseOrderByCreatedAtDesc();

    /**
     * 根据分类查询模板
     */
    Page<StyleTemplate> findByCategory(String category, Pageable pageable);

    /**
     * 查询所有公开模板（系统预设+用户公开）
     */
    List<StyleTemplate> findByIsPublicTrueOrderByCreatedAtAsc();

    /**
     * 根据用户ID查询自定义模板
     */
    List<StyleTemplate> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 根据用户ID查询模板（包括系统预设）
     */
    @Query("SELECT t FROM StyleTemplate t WHERE t.isSystem = true OR t.userId = :userId")
    List<StyleTemplate> findAvailableForUser(Long userId);

    /**
     * 查询所有分类
     */
    @Query("SELECT DISTINCT s.category FROM StyleTemplate s WHERE s.category IS NOT NULL ORDER BY s.category")
    List<String> findAllCategories();
}
