package com.cinestory.service.video;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cinestory.model.dto.response.GenerationStatsResponse;
import com.cinestory.model.dto.response.VideoGenerationResponse;
import com.cinestory.model.entity.VideoGeneration;

/**
 * 视频生成服务
 */
public interface VideoGenerationService {

    /**
     * 检查并更新视频生成状态
     */
    void checkAndUpdateStatus(Long generationId);

    /**
     * 重试失败的视频生成
     */
    void retryFailedGeneration(Long generationId);

    /**
     * 分页查询视频生成历史
     */
    IPage<VideoGenerationResponse> getHistory(IPage<VideoGeneration> page, VideoGeneration.GenerationStatus status, String provider);

    /**
     * 查询生成详情
     */
    VideoGenerationResponse getById(Long id);

    /**
     * 重试失败的生成
     */
    void retryGeneration(Long id);

    /**
     * 取消正在处理的生成
     */
    void cancelGeneration(Long id);

    /**
     * 获取生成统计
     */
    GenerationStatsResponse getStats();
}
