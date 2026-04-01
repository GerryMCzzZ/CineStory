package com.cinestory.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 视频生成提示词实体
 *
 * @author CineStory
 */
@TableName("video_prompts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoPrompt {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sliceId;

    /**
     * 视觉提示词
     */
    private String visualPrompt;

    /**
     * 运动提示词
     */
    private String motionPrompt;

    /**
     * 镜头提示词
     */
    private String cameraPrompt;

    /**
     * 氛围描述
     */
    private String atmosphere;

    @Builder.Default
    private Integer duration = 5;

    @Builder.Default
    private String aspectRatio = "16:9";

    @Builder.Default
    private Boolean isManual = false;

    /**
     * 原始提示词（LLM 生成前的原文）
     */
    private String originalPrompt;

    /**
     * LLM 服务商
     */
    private String llmProvider;

    /**
     * LLM 模型名称
     */
    private String llmModel;

    /**
     * LLM 消耗 Token 数
     */
    private Integer llmTokensUsed;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
