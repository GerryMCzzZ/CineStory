package com.cinestory.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文本切片实体
 * 表示小说文本的一个切片片段
 */
@Entity
@Table(name = "text_slices")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TextSlice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    // 切片元数据
    @Enumerated(EnumType.STRING)
    @Column(name = "scene_type", length = 50)
    private SceneType sceneType;

    @Column(name = "characters", columnDefinition = "JSON")
    private String characters;  // JSON数组字符串

    @Column(name = "mood", length = 100)
    private String mood;

    @Column(name = "location")
    private String location;

    @Column(name = "time_of_day", length = 50)
    private String timeOfDay;

    // 上下文引用
    @Column(name = "context_before", columnDefinition = "JSON")
    private String contextBefore;  // JSON数组字符串

    @Column(name = "context_after", columnDefinition = "JSON")
    private String contextAfter;   // JSON数组字符串

    // 统计信息
    @Column(name = "character_count")
    private Integer characterCount;

    @Column(name = "estimated_duration")
    private Integer estimatedDuration;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (characterCount == null && content != null) {
            characterCount = content.length();
        }
    }

    /**
     * 场景类型枚举
     */
    public enum SceneType {
        DIALOGUE,       // 对话
        DESCRIPTION,    // 描写
        ACTION,         // 动作
        TRANSITION,     // 转场
        MONOLOGUE,      // 独白
        NARRATION       // 旁白
    }
}
