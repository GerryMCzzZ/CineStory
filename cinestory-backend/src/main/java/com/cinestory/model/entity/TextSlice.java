package com.cinestory.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文本切片实体
 *
 * @author CineStory
 */
@TableName("text_slices")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TextSlice {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private String content;

    private Integer orderIndex;

    /**
     * 场景类型
     */
    private SceneType sceneType;

    /**
     * 出场人物 JSON
     */
    private String characters;

    private String mood;

    private String location;

    private String timeOfDay;

    /**
     * 前文上下文 JSON
     */
    private String contextBefore;

    /**
     * 后文上下文 JSON
     */
    private String contextAfter;

    private Integer characterCount;

    private Integer estimatedDuration;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 场景类型枚举
     */
    public enum SceneType {
        DIALOGUE,
        DESCRIPTION,
        ACTION,
        TRANSITION,
        MONOLOGUE,
        NARRATION
    }
}
