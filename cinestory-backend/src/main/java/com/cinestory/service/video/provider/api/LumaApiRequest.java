package com.cinestory.service.video.provider.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Luma AI API 请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LumaApiRequest {

    /**
     * 提示词
     */
    @JsonProperty("prompt")
    private String prompt;

    /**
     * 输入图片 URL（可选，用于图生视频）
     */
    @JsonProperty("image_url")
    private String imageUrl;

    /**
     * 结束帧图片 URL（可选）
     */
    @JsonProperty("end_image_url")
    private String endImageUrl;

    /**
     * 视频时长（秒）
     * Luma Dream Machine 支持 5 秒
     */
    @JsonProperty("duration")
    private Integer duration;

    /**
     * 宽高比
     * 支持: "9:16", "16:9", "1:1", "4:3", "3:4"
     */
    @JsonProperty("aspect_ratio")
    private String aspectRatio;

    /**
     * 循环视频（可选）
     */
    @JsonProperty("loop")
    private Boolean loop;

    /**
     * 摄像机控制（可选）
     */
    @JsonProperty("camera_motion")
    private String cameraMotion;

    /**
     * 创建标准请求
     */
    public static LumaApiRequest standard(String prompt, String aspectRatio) {
        return LumaApiRequest.builder()
                .prompt(prompt)
                .duration(5)
                .aspectRatio(aspectRatio)
                .loop(false)
                .build();
    }

    /**
     * 创建图生视频请求
     */
    public static LumaApiRequest imageToVideo(String prompt, String imageUrl, String aspectRatio) {
        return LumaApiRequest.builder()
                .prompt(prompt)
                .imageUrl(imageUrl)
                .duration(5)
                .aspectRatio(aspectRatio)
                .loop(false)
                .build();
    }

    /**
     * 创建关键帧到关键帧请求
     */
    public static LumaApiRequest keyFrameToKeyFrame(String prompt, String startImageUrl, String endImageUrl) {
        return LumaApiRequest.builder()
                .prompt(prompt)
                .imageUrl(startImageUrl)
                .endImageUrl(endImageUrl)
                .duration(5)
                .aspectRatio("16:9")
                .loop(false)
                .build();
    }
}
