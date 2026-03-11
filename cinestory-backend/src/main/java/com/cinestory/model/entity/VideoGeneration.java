package com.cinestory.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 视频生成记录实体
 * 记录每次视频生成API的调用
 */
@Entity
@Table(name = "video_generations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoGeneration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "slice_id", nullable = false)
    private Long sliceId;

    @Column(name = "prompt_id")
    private Long promptId;

    // API 信息
    @Column(name = "provider", nullable = false, length = 50)
    private String provider;

    @Column(name = "provider_model", length = 100)
    private String providerModel;

    @Column(name = "provider_task_id", length = 255)
    private String providerTaskId;

    // 状态管理
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private GenerationStatus status = GenerationStatus.PENDING;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    // 视频信息
    @Column(name = "video_url")
    private String videoUrl;

    @Column(name = "local_path")
    private String localPath;

    @Column(name = "minio_path")
    private String minioPath;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Column(name = "file_size")
    private Long fileSize;

    // 重试记录
    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "last_retry_at")
    private LocalDateTime lastRetryAt;

    // 时间戳
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * 视频生成状态枚举
     */
    public enum GenerationStatus {
        PENDING,        // 等待处理
        PROCESSING,    // 处理中
        COMPLETED,     // 已完成
        FAILED,        // 失败
        TIMEOUT,       // 超时
        CANCELLED      // 已取消
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
