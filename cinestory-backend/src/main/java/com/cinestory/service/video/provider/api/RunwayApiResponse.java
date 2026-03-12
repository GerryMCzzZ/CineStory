package com.cinestory.service.video.provider.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Runway API 响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunwayApiResponse {

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
     * 输出视频 URL
     */
    @JsonProperty("output")
    private String output;

    /**
     * 进度百分比
     */
    @JsonProperty("progress")
    private Integer progress;

    /**
     * 错误消息
     */
    @JsonProperty("error")
    private String error;

    /**
     * 创建时间
     */
    @JsonProperty("createdAt")
    private String createdAt;

    /**
     * 完成时间
     */
    @JsonProperty("completedAt")
    private String completedAt;

    /**
     * 失败原因
     */
    @JsonProperty("failureReason")
    private String failureReason;

    /**
     * 预估等待时间（秒）
     */
    @JsonProperty("estimatedTimeSeconds")
    private Integer estimatedTimeSeconds;

    /**
     * 检查是否处于最终状态
     */
    public boolean isTerminal() {
        return "SUCCEEDED".equals(status) || "FAILED".equals(status) || "CANCELLED".equals(status);
    }

    /**
     * 检查是否成功
     */
    public boolean isSuccess() {
        return "SUCCEEDED".equals(status) && output != null;
    }

    /**
     * 检查是否失败
     */
    public boolean isFailed() {
        return "FAILED".equals(status) || "CANCELLED".equals(status);
    }

    /**
     * 检查是否处理中
     */
    public boolean isProcessing() {
        return "PENDING".equals(status) || "RUNNING".equals(status) || "QUEUED".equals(status);
    }
}
