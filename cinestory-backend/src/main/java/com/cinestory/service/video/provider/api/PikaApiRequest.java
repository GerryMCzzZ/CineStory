package com.cinestory.service.video.provider.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Pika API 请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PikaApiRequest {

    /**
     * 提示词
     */
    @JsonProperty("prompt")
    private String prompt;

    /**
     * 模型类型
     */
    @JsonProperty("model")
    private String model;

    /**
     * 视频时长（秒）
     * Pika 通常支持 3-4 秒
     */
    @JsonProperty("duration")
    private Integer duration;

    /**
     * 宽高比
     * 支持: "9:16", "16:9", "1:1", "21:9", "4:5"
     */
    @JsonProperty("aspect_ratio")
    private String aspectRatio;

    /**
     * 帧率
     * 支持: 24, 30
     */
    @JsonProperty("frame_rate")
    private Integer frameRate;

    /**
     * 摄像机运动
     * 支持: "zoom in", "zoom out", "pan left", "pan right", "tilt up", "tilt down"
     */
    @JsonProperty("camera")
    private String camera;

    /**
     * 输入图片 URL（可选，用于图生视频）
     */
    @JsonProperty("image_url")
    private String imageUrl;

    /**
     * 负面提示词（可选）
     */
    @JsonProperty("negative_prompt")
    private String negativePrompt;

    /**
     * 种子值（可选，用于可重现性）
     */
    @JsonProperty("seed")
    private Integer seed;

    /**
     * 创建 Pika-1.0 请求（默认配置）
     */
    public static PikaApiRequest pika10(String prompt, String aspectRatio) {
        return PikaApiRequest.builder()
                .prompt(prompt)
                .model("pika-1.0")
                .duration(4)
                .aspectRatio(aspectRatio)
                .frameRate(24)
                .build();
    }

    /**
     * 创建 Pika-1.0 请求（带摄像机运动）
     */
    public static PikaApiRequest pika10WithCamera(String prompt, String aspectRatio, String camera) {
        return PikaApiRequest.builder()
                .prompt(prompt)
                .model("pika-1.0")
                .duration(4)
                .aspectRatio(aspectRatio)
                .frameRate(24)
                .camera(camera)
                .build();
    }
}
