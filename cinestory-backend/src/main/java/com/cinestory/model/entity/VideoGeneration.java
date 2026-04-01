package com.cinestory.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 视频生成记录实体
 *
 * @author CineStory
 */
@TableName("video_generations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoGeneration {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sliceId;

    private Long promptId;

    /**
     * 生成服务提供商
     */
    private String provider;

    /**
     * 服务商模型名称
     */
    private String providerModel;

    /**
     * 服务商任务 ID
     */
    private String providerTaskId;

    @Builder.Default
    private GenerationStatus status = GenerationStatus.PENDING;

    private String errorMessage;

    private String videoUrl;

    private String localPath;

    private String minioPath;

    private Integer duration;

    private Integer width;

    private Integer height;

    private Long fileSize;

    @Builder.Default
    private Integer retryCount = 0;

    private LocalDateTime lastRetryAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    /**
     * 视频生成状态枚举
     */
    public enum GenerationStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        TIMEOUT,
        CANCELLED
    }

    /**
     * 判断是否可以重试
     */
    public boolean canRetry(int maxRetries) {
        return (status == GenerationStatus.FAILED || status == GenerationStatus.TIMEOUT)
                && retryCount < maxRetries;
    }

    /**
     * 判断是否处于最终状态
     */
    public boolean isFinalState() {
        return status == GenerationStatus.COMPLETED
                || status == GenerationStatus.FAILED
                || status == GenerationStatus.TIMEOUT
                || status == GenerationStatus.CANCELLED;
    }

    /**
     * 增加重试次数
     */
    public void incrementRetry() {
        this.retryCount++;
        this.lastRetryAt = LocalDateTime.now();
        this.status = GenerationStatus.PENDING;
    }
}
