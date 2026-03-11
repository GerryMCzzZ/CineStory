package com.cinestory.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 项目实体
 * 表示一个小说转视频的项目
 */
@Entity
@Table(name = "projects")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "style_template_id")
    private Long styleTemplateId;

    // 小说文本信息
    @Column(name = "novel_title")
    private String novelTitle;

    @Column(name = "novel_author")
    private String novelAuthor;

    @Column(name = "novel_content", columnDefinition = "TEXT")
    private String novelContent;

    @Column(name = "total_characters")
    private Integer totalCharacters;

    // 项目配置
    @Column(name = "config_json", columnDefinition = "JSON")
    private String configJson;

    // 状态管理
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private ProjectStatus status = ProjectStatus.DRAFT;

    @Column(name = "current_step", length = 50)
    private String currentStep;

    @Column(name = "progress")
    @Builder.Default
    private Integer progress = 0;

    // 输出信息
    @Column(name = "output_video_url")
    private String outputVideoUrl;

    @Column(name = "output_video_path")
    private String outputVideoPath;

    @Column(name = "total_duration")
    private Integer totalDuration;

    // 时间戳
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 项目状态枚举
     */
    public enum ProjectStatus {
        DRAFT,          // 草稿
        PROCESSING,    // 处理中
        COMPLETED,     // 已完成
        FAILED,        // 失败
        CANCELLED      // 已取消
    }
}
