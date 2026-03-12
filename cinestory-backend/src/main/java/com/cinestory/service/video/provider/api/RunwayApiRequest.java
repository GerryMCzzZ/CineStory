package com.cinestory.service.video.provider.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Runway API 请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunwayApiRequest {

    /**
     * 提示词文本
     */
    @JsonProperty("promptText")
    private String promptText;

    /**
     * 模型名称
     */
    @JsonProperty("model")
    private String model;

    /**
     * 视频时长（秒）
     */
    @JsonProperty("duration")
    private Integer duration;

    /**
     * 宽高比
     */
    @JsonProperty("ratio")
    private String ratio;

    /**
     * 图片作为输入（可选）
     */
    @JsonProperty("image")
    private String imageUrl;

    /**
     * 遮罩图片（可选）
     */
    @JsonProperty("mask")
    private String maskUrl;

    /**
     * 水印（可选）
     */
    @JsonProperty("watermark")
    private Boolean watermark;

    /**
     * 预设提示词增强（可选）
     */
    @JsonProperty("promptEnhancement")
    private Boolean promptEnhancement;

    /**
     * 创建 Gen-3 Alpha Turbo 请求
     */
    public static RunwayApiRequest gen3Turbo(String promptText, int duration, String ratio) {
        return RunwayApiRequest.builder()
                .promptText(promptText)
                .model("gen3a_turbo")
                .duration(duration)
                .ratio(ratio)
                .watermark(false)
                .promptEnhancement(true)
                .build();
    }

    /**
     * 创建 Gen-2 请求
     */
    public static RunwayApiRequest gen2(String promptText, int duration) {
        return RunwayApiRequest.builder()
                .promptText(promptText)
                .model("gen2")
                .duration(duration)
                .ratio("16:9")
                .watermark(false)
                .build();
    }
}
