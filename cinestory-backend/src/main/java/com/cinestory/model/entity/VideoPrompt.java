package com.cinestory.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 视频生成提示词实体
 */
@Entity
@Table(name = "video_prompts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoPrompt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "slice_id", nullable = false, unique = true)
    private Long sliceId;

    // 提示词内容
    @Column(name = "visual_prompt", nullable = false, columnDefinition = "TEXT")
    private String visualPrompt;

    @Column(name = "motion_prompt", columnDefinition = "TEXT")
    private String motionPrompt;

    @Column(name = "camera_prompt", columnDefinition = "TEXT")
    private String cameraPrompt;

    @Column(name = "atmosphere")
    private String atmosphere;

    // 生成参数
    @Column(name = "duration")
    @Builder.Default
    private Integer duration = 5;

    @Column(name = "aspect_ratio", length = 20)
    @Builder.Default
    private String aspectRatio = "16:9";

    // 修改记录
    @Column(name = "is_manual")
    @Builder.Default
    private Boolean isManual = false;

    @Column(name = "original_prompt", columnDefinition = "TEXT")
    private String originalPrompt;

    // LLM 调用记录
    @Column(name = "llm_provider", length = 50)
    private String llmProvider;

    @Column(name = "llm_model", length = 100)
    private String llmModel;

    @Column(name = "llm_tokens_used")
    private Integer llmTokensUsed;

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
