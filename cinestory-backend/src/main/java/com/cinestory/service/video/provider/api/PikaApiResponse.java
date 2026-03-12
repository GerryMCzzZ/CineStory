package com.cinestory.service.video.provider.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Pika API 响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PikaApiResponse {

    /**
     * 任务 ID
     */
    @JsonProperty("id")
    private String id;

    /**
     * 任务状态
     */
    @JsonProperty("status")
    private String status;

    /**
     * 进度百分比 (0-100)
     */
    @JsonProperty("progress")
    private Integer progress;

    /**
     * 视频资源列表
     */
    @JsonProperty("video")
    private List<VideoResource> videos;

    /**
     * 错误消息
     */
    @JsonProperty("error")
    private String error;

    /**
     * 创建时间
     */
    @JsonProperty("created_at")
    private String createdAt;

    /**
     * 开始时间
     */
    @JsonProperty("started_at")
    private String startedAt;

    /**
     * 完成时间
     */
    @JsonProperty("finished_at")
    private String finishedAt;

    /**
     * 预估时间（秒）
     */
    @JsonProperty("estimate")
    private Integer estimate;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VideoResource {
        /**
         * 视频 URL
         */
        @JsonProperty("url")
        private String url;

        /**
         * 视频时长（秒）
         */
        @JsonProperty("duration")
        private Double duration;

        /**
         * 缩略图 URL
         */
        @JsonProperty("thumbnail")
        private String thumbnail;

        /**
         * 分辨率
         */
        @JsonProperty("resolution")
        private String resolution;
    }

    /**
     * 检查是否处于最终状态
     */
    public boolean isTerminal() {
        return "succeeded".equalsIgnoreCase(status)
                || "failed".equalsIgnoreCase(status)
                || "cancelled".equalsIgnoreCase(status);
    }

    /**
     * 检查是否成功
     */
    public boolean isSuccess() {
        return "succeeded".equalsIgnoreCase(status)
                && videos != null
                && !videos.isEmpty();
    }

    /**
     * 检查是否失败
     */
    public boolean isFailed() {
        return "failed".equalsIgnoreCase(status) || "cancelled".equalsIgnoreCase(status);
    }

    /**
     * 检查是否处理中
     */
    public boolean isProcessing() {
        return "queued".equalsIgnoreCase(status)
                || "processing".equalsIgnoreCase(status)
                || "init".equalsIgnoreCase(status);
    }

    /**
     * 获取第一个视频 URL
     */
    public String getFirstVideoUrl() {
        if (videos != null && !videos.isEmpty()) {
            return videos.get(0).getUrl();
        }
        return null;
    }
}
