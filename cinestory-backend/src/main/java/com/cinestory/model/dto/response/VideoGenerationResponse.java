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
    private Long sliceId;
    private Long promptId;
    private String provider;
    private String providerModel;
    private String providerTaskId;
    private String status;
    private String videoUrl;
    private Integer duration;
    private Integer width;
    private Integer height;
    private Long fileSize;
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
                .sliceId(entity.getSliceId())
                .promptId(entity.getPromptId())
                .provider(entity.getProvider())
                .providerModel(entity.getProviderModel())
                .providerTaskId(entity.getProviderTaskId())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .videoUrl(entity.getVideoUrl())
                .duration(entity.getDuration())
                .width(entity.getWidth())
                .height(entity.getHeight())
                .fileSize(entity.getFileSize())
                .errorMessage(entity.getErrorMessage())
                .retryCount(entity.getRetryCount())
                .createdAt(entity.getCreatedAt())
                .completedAt(entity.getCompletedAt())
                .build();
    }
}
