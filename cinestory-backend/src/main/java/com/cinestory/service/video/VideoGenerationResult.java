package com.cinestory.service.video;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 视频生成结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoGenerationResult {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 任务 ID（用于轮询状态）
     */
    private String taskId;

    /**
     * 视频 URL（如果立即可用）
     */
    private String videoUrl;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 提供商名称
     */
    private String provider;

    /**
     * 预计完成时间（秒）
     */
    private Integer estimatedSeconds;

    /**
     * 创建成功结果
     */
    public static VideoGenerationResult success(String taskId, String videoUrl, String provider) {
        return VideoGenerationResult.builder()
                .success(true)
                .taskId(taskId)
                .videoUrl(videoUrl)
                .provider(provider)
                .build();
    }

    /**
     * 创建失败结果
     */
    public static VideoGenerationResult failure(String errorMessage, String provider) {
        return VideoGenerationResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .provider(provider)
                .build();
    }
}
