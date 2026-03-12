package com.cinestory.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 视频生成请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoGenerationRequest {

    /**
     * 提示词
     */
    private String prompt;

    /**
     * 负面提示词
     */
    private String negativePrompt;

    /**
     * 视频时长（秒）
     */
    private Integer duration;

    /**
     * 宽度
     */
    private Integer width;

    /**
     * 高度
     */
    private Integer height;

    /**
     * 宽高比 (e.g., "16:9", "9:16")
     */
    private String aspectRatio;

    /**
     * 帧率
     */
    private Integer fps;

    /**
     * 风格参数
     */
    private String style;

    /**
     * 回调 URL
     */
    private String callbackUrl;

    /**
     * 输入图片 URL（用于图生视频）
     */
    private String imageUrl;

    /**
     * 模型名称（可选，用于指定特定模型）
     */
    private String model;
}
