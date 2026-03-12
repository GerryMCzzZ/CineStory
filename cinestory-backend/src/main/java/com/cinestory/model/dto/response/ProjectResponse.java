package com.cinestory.model.dto.response;

import com.cinestory.model.entity.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 项目响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {

    private Long id;
    private String name;
    private String description;
    private String novelTitle;
    private String novelAuthor;
    private Integer totalCharacters;
    private Long styleTemplateId;
    private String status;
    private String currentStep;
    private Integer progress;
    private String outputVideoUrl;
    private Integer totalDuration;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    /**
     * 从实体转换为响应 DTO
     */
    public static ProjectResponse fromEntity(Project entity) {
        return ProjectResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .novelTitle(entity.getNovelTitle())
                .novelAuthor(entity.getNovelAuthor())
                .totalCharacters(entity.getTotalCharacters())
                .styleTemplateId(entity.getStyleTemplateId())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .currentStep(entity.getCurrentStep())
                .progress(entity.getProgress())
                .outputVideoUrl(entity.getOutputVideoUrl())
                .totalDuration(entity.getTotalDuration())
                .createdAt(entity.getCreatedAt())
                .startedAt(entity.getStartedAt())
                .completedAt(entity.getCompletedAt())
                .build();
    }
}
