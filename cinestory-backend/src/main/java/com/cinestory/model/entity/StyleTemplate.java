package com.cinestory.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 风格模板实体
 *
 * @author CineStory
 */
@TableName("style_templates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StyleTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String nameEn;

    private String description;

    @Builder.Default
    private Boolean isSystem = true;

    @Builder.Default
    private Boolean isPublic = true;

    /**
     * 视觉风格
     */
    private String visualStyle;

    /**
     * 人物风格
     */
    private String characterStyle;

    /**
     * 背景风格
     */
    private String backgroundStyle;

    /**
     * 负面提示词
     */
    private String negativePrompts;

    /**
     * 默认镜头
     */
    private String defaultCamera;

    /**
     * 默认运动方式
     */
    private String defaultMotion;

    private String previewImageUrl;

    /**
     * 扩展配置 JSON
     */
    private String configJson;

    private Long userId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
