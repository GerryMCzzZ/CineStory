package com.cinestory.model.dto.response;

import com.cinestory.model.entity.StyleTemplate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 风格模板响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StyleTemplateResponse {

    private Long id;
    private String name;
    private String nameEn;
    private String description;
    private Boolean isSystem;
    private Boolean isPublic;
    private String visualStyle;
    private String characterStyle;
    private String backgroundStyle;
    private String negativePrompts;
    private String defaultCamera;
    private String defaultMotion;
    private String previewImageUrl;
    private String configJson;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 从实体转换为响应 DTO
     */
    public static StyleTemplateResponse fromEntity(StyleTemplate entity) {
        return StyleTemplateResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .nameEn(entity.getNameEn())
                .description(entity.getDescription())
                .isSystem(entity.getIsSystem())
                .isPublic(entity.getIsPublic())
                .visualStyle(entity.getVisualStyle())
                .characterStyle(entity.getCharacterStyle())
                .backgroundStyle(entity.getBackgroundStyle())
                .negativePrompts(entity.getNegativePrompts())
                .defaultCamera(entity.getDefaultCamera())
                .defaultMotion(entity.getDefaultMotion())
                .previewImageUrl(entity.getPreviewImageUrl())
                .configJson(entity.getConfigJson())
                .userId(entity.getUserId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
