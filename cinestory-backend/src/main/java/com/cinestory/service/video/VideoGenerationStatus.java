package com.cinestory.service.video;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 视频生成状态
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoGenerationStatus {

    /**
     * 状态
     */
    private Status status;

    /**
     * 视频 URL（完成时可用）
     */
    private String videoUrl;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 进度百分比
     */
    private Integer progress;

    /**
     * 状态枚举
     */
    public enum Status {
        PENDING,    // 等待处理
        PROCESSING, // 处理中
        COMPLETED,  // 已完成
        FAILED      // 失败
    }
}
