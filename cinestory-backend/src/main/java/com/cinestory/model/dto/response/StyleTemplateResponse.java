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
    private String description;
    private String category;
    private String previewImage;
    private Boolean isSystem;
    private String promptTemplate;
    private String negativePrompt;
    private Integer videoDuration;
    private Integer fps;
    private String aspectRatio;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 从实体转换为响应 DTO
     */
    public static StyleTemplateResponse fromEntity(StyleTemplate entity) {
        return StyleTemplateResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .category(entity.getCategory())
                .previewImage(entity.getPreviewImage())
                .isSystem(entity.getIsSystem())
                .promptTemplate(entity.getPromptTemplate())
                .negativePrompt(entity.getNegativePrompt())
                .videoDuration(entity.getVideoDuration())
                .fps(entity.getFps())
                .aspectRatio(entity.getAspectRatio())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
