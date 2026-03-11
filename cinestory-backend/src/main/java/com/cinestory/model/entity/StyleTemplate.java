package com.cinestory.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 风格模板实体
 * 用于定义视频生成的视觉风格
 */
@Entity
@Table(name = "style_templates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StyleTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "name_en", length = 100)
    private String nameEn;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_system", nullable = false)
    @Builder.Default
    private Boolean isSystem = true;

    @Column(name = "is_public", nullable = false)
    @Builder.Default
    private Boolean isPublic = true;

    // 视觉风格配置
    @Column(name = "visual_style", columnDefinition = "TEXT")
    private String visualStyle;

    @Column(name = "character_style", columnDefinition = "TEXT")
    private String characterStyle;

    @Column(name = "background_style", columnDefinition = "TEXT")
    private String backgroundStyle;

    @Column(name = "negative_prompts", columnDefinition = "TEXT")
    private String negativePrompts;

    // 镜头和运动偏好
    @Column(name = "default_camera")
    private String defaultCamera;

    @Column(name = "default_motion")
    private String defaultMotion;

    // 其他配置
    @Column(name = "preview_image_url")
    private String previewImageUrl;

    @Column(name = "config_json", columnDefinition = "JSON")
    private String configJson;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
