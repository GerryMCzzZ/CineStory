package com.cinestory.model.dto.response;

import com.cinestory.model.entity.VideoGeneration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 视频生成响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoGenerationResponse {

    private Long id;
    private Long projectId;
    private Long textSliceId;
    private String provider;
    private String status;
    private String prompt;
    private String videoUrl;
    private Integer duration;
    private Integer width;
    private Integer height;
    private String errorMessage;
    private Integer retryCount;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    /**
     * 从实体转换为响应 DTO
     */
    public static VideoGenerationResponse fromEntity(VideoGeneration entity) {
        return VideoGenerationResponse.builder()
                .id(entity.getId())
                .projectId(entity.getProjectId())
                .textSliceId(entity.getTextSliceId())
                .provider(entity.getProvider())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .prompt(entity.getPrompt())
                .videoUrl(entity.getVideoUrl())
                .duration(entity.getDuration())
                .width(entity.getWidth())
                .height(entity.getHeight())
                .errorMessage(entity.getErrorMessage())
                .retryCount(entity.getRetryCount())
                .createdAt(entity.getCreatedAt())
                .completedAt(entity.getCompletedAt())
                .build();
    }
}
