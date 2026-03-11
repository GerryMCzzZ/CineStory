package com.cinestory.repository;

import com.cinestory.model.entity.StyleTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
