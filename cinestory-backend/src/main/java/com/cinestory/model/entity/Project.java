package com.cinestory.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 项目实体
 *
 * @author CineStory
 */
@TableName("projects")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String name;

    private String description;

    private Long styleTemplateId;

    /**
     * 小说标题
     */
    private String novelTitle;

    /**
     * 小说作者
     */
    private String novelAuthor;

    /**
     * 小说正文内容
     */
    private String novelContent;

    private Integer totalCharacters;

    /**
     * 项目配置 JSON
     */
    private String configJson;

    @Builder.Default
    private ProjectStatus status = ProjectStatus.DRAFT;

    private String currentStep;

    @Builder.Default
    private Integer progress = 0;

    private String outputVideoUrl;

    private String outputVideoPath;

    private Integer totalDuration;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    /**
     * 项目状态枚举
     */
    public enum ProjectStatus {
        DRAFT,
        PROCESSING,
        COMPLETED,
        FAILED,
        CANCELLED
    }
}
