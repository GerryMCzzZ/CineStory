package com.cinestory.service.video;

import com.cinestory.model.dto.VideoGenerationRequest;

/**
 * 视频生成提供商接口
 */
public interface VideoGenerationProvider {

    /**
     * 获取提供商名称
     */
    String getProviderName();

    /**
     * 生成视频
     *
     * @param request 视频生成请求
     * @return 视频生成结果（包含视频 URL 或任务 ID）
     */
    VideoGenerationResult generate(VideoGenerationRequest request);

    /**
     * 检查生成任务状态
     *
     * @param taskId 任务 ID
     * @return 任务状态
     */
    VideoGenerationStatus checkStatus(String taskId);

    /**
     * 检查提供商是否可用
     */
    boolean isAvailable();

    /**
     * 获取优先级（数字越小优先级越高）
     */
    int getPriority();
}
