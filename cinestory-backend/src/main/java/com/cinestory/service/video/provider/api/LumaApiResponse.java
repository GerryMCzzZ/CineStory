package com.cinestory.service.video.provider.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Luma AI API 响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LumaApiResponse {

    /**
     * 任务 ID
     */
    @JsonProperty("id")
    private String id;

    /**
     * 任务状态
     * 状态: pending, queued, processing, completed, failed
     */
    @JsonProperty("state")
    private String state;

    /**
     * 视频状态（等同于 state）
     */
    @JsonProperty("status")
    private String status;

    /**
     * 视频下载 URL
     */
    @JsonProperty("video")
    private String video;

    /**
     * 预览视频 URL（低质量）
     */
    @JsonProperty("preview_video")
    private String previewVideo;

    /**
     * 缩略图 URL
     */
    @JsonProperty("thumbnail")
    private String thumbnail;

    /**
     * 错误消息
     */
    @JsonProperty("error")
    private String error;

    /**
     * 失败原因
     */
    @JsonProperty("failure_reason")
    private String failureReason;

    /**
     * 创建时间
     */
    @JsonProperty("created_at")
    private String createdAt;

    /**
     * 完成时间
     */
    @JsonProperty("completed_at")
    private String completedAt;

    /**
     * 预估等待时间（秒）
     */
    @JsonProperty("wait_time_est")
    private Integer waitTimeEst;

    /**
     * 预估处理时间（秒）
     */
    @JsonProperty("process_time_est")
    private Integer processTimeEst;

    /**
     * 检查是否处于最终状态
     */
    public boolean isTerminal() {
        String s = status != null ? status : state;
        return "completed".equalsIgnoreCase(s)
                || "failed".equalsIgnoreCase(s)
                || "canceled".equalsIgnoreCase(s);
    }

    /**
     * 检查是否成功
     */
    public boolean isSuccess() {
        String s = status != null ? status : state;
        return "completed".equalsIgnoreCase(s) && video != null;
    }

    /**
     * 检查是否失败
     */
    public boolean isFailed() {
        String s = status != null ? status : state;
        return "failed".equalsIgnoreCase(s) || "canceled".equalsIgnoreCase(s);
    }

    /**
     * 检查是否处理中
     */
    public boolean isProcessing() {
        String s = status != null ? status : state;
        return "pending".equalsIgnoreCase(s)
                || "queued".equalsIgnoreCase(s)
                || "processing".equalsIgnoreCase(s);
    }

    /**
     * 获取视频 URL（优先使用高质量视频）
     */
    public String getVideoUrl() {
        if (video != null) {
            return video;
        }
        return previewVideo;
    }
}
