package com.cinestory.service.video;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cinestory.mapper.VideoGenerationMapper;
import com.cinestory.model.dto.response.GenerationStatsResponse;
import com.cinestory.model.dto.response.VideoGenerationResponse;
import com.cinestory.model.entity.VideoGeneration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 视频生成服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VideoGenerationServiceImpl implements VideoGenerationService {

    private final VideoGenerationMapper videoGenerationMapper;

    /**
     * 检查并更新视频生成状态
     */
    @Override
    public void checkAndUpdateStatus(Long generationId) {
        VideoGeneration generation = videoGenerationMapper.selectById(generationId);
        if (generation == null) {
            return;
        }

        if (generation.getProviderTaskId() == null) {
            return;
        }

        log.debug("Checking status for generation: {}", generationId);
    }

    /**
     * 重试失败的视频生成
     */
    @Override
    public void retryFailedGeneration(Long generationId) {
        VideoGeneration generation = videoGenerationMapper.selectById(generationId);
        if (generation == null) {
            return;
        }

        // 重置状态
        generation.setStatus(VideoGeneration.GenerationStatus.PENDING);
        generation.setRetryCount(generation.getRetryCount() + 1);
        generation.setErrorMessage(null);

        videoGenerationMapper.updateById(generation);
        log.info("Retrying generation for slice: {}", generation.getSliceId());
    }

    /**
     * 分页查询视频生成历史
     */
    @Override
    public IPage<VideoGenerationResponse> getHistory(IPage<VideoGeneration> page, VideoGeneration.GenerationStatus status, String provider) {
        IPage<VideoGeneration> generations;
        if (status != null) {
            generations = videoGenerationMapper.selectPage(page, new LambdaQueryWrapper<VideoGeneration>()
                    .eq(VideoGeneration::getStatus, status));
        } else {
            generations = videoGenerationMapper.selectPage(page, null);
        }

        // Convert to response page
        IPage<VideoGenerationResponse> responsePage = generations.convert(VideoGenerationResponse::fromEntity);
        return responsePage;
    }

    /**
     * 查询生成详情
     */
    @Override
    public VideoGenerationResponse getById(Long id) {
        VideoGeneration generation = videoGenerationMapper.selectById(id);
        if (generation == null) {
            return null;
        }
        return VideoGenerationResponse.fromEntity(generation);
    }

    /**
     * 重试失败的生成
     */
    @Override
    @Transactional
    public void retryGeneration(Long id) {
        VideoGeneration generation = videoGenerationMapper.selectById(id);
        if (generation == null) {
            throw new IllegalArgumentException("Video generation not found: " + id);
        }

        if (!generation.getStatus().equals(VideoGeneration.GenerationStatus.FAILED)) {
            throw new IllegalStateException("Can only retry failed generations");
        }

        // 重置状态
        generation.setStatus(VideoGeneration.GenerationStatus.PENDING);
        generation.setRetryCount(generation.getRetryCount() + 1);
        generation.setErrorMessage(null);
        generation.setProviderTaskId(null);

        videoGenerationMapper.updateById(generation);
        log.info("Retrying generation {} for slice: {}", id, generation.getSliceId());
    }

    /**
     * 取消正在处理的生成
     */
    @Override
    @Transactional
    public void cancelGeneration(Long id) {
        VideoGeneration generation = videoGenerationMapper.selectById(id);
        if (generation == null) {
            throw new IllegalArgumentException("Video generation not found: " + id);
        }

        if (!generation.getStatus().equals(VideoGeneration.GenerationStatus.PROCESSING)
                && !generation.getStatus().equals(VideoGeneration.GenerationStatus.PENDING)) {
            throw new IllegalStateException("Can only cancel pending or processing generations");
        }

        generation.setStatus(VideoGeneration.GenerationStatus.CANCELLED);
        generation.setCompletedAt(java.time.LocalDateTime.now());

        videoGenerationMapper.updateById(generation);
        log.info("Cancelled generation {} for slice: {}", id, generation.getSliceId());
    }

    /**
     * 获取生成统计
     */
    @Override
    public GenerationStatsResponse getStats() {
        return GenerationStatsResponse.builder()
                .total(videoGenerationMapper.selectCount(null))
                .pending(videoGenerationMapper.selectCount(new LambdaQueryWrapper<VideoGeneration>()
                        .eq(VideoGeneration::getStatus, VideoGeneration.GenerationStatus.PENDING)))
                .processing(videoGenerationMapper.selectCount(new LambdaQueryWrapper<VideoGeneration>()
                        .eq(VideoGeneration::getStatus, VideoGeneration.GenerationStatus.PROCESSING)))
                .completed(videoGenerationMapper.selectCount(new LambdaQueryWrapper<VideoGeneration>()
                        .eq(VideoGeneration::getStatus, VideoGeneration.GenerationStatus.COMPLETED)))
                .failed(videoGenerationMapper.selectCount(new LambdaQueryWrapper<VideoGeneration>()
                        .eq(VideoGeneration::getStatus, VideoGeneration.GenerationStatus.FAILED)))
                .cancelled(videoGenerationMapper.selectCount(new LambdaQueryWrapper<VideoGeneration>()
                        .eq(VideoGeneration::getStatus, VideoGeneration.GenerationStatus.CANCELLED)))
                .runwayTotal(videoGenerationMapper.countByProviderStartingWith("runway"))
                .runwayCompleted(videoGenerationMapper.selectCount(new LambdaQueryWrapper<VideoGeneration>()
                        .eq(VideoGeneration::getProvider, "runway")
                        .eq(VideoGeneration::getStatus, VideoGeneration.GenerationStatus.COMPLETED)))
                .pikaTotal(videoGenerationMapper.countByProviderStartingWith("pika"))
                .pikaCompleted(videoGenerationMapper.selectCount(new LambdaQueryWrapper<VideoGeneration>()
                        .eq(VideoGeneration::getProvider, "pika")
                        .eq(VideoGeneration::getStatus, VideoGeneration.GenerationStatus.COMPLETED)))
                .lumaTotal(videoGenerationMapper.countByProviderStartingWith("luma"))
                .lumaCompleted(videoGenerationMapper.selectCount(new LambdaQueryWrapper<VideoGeneration>()
                        .eq(VideoGeneration::getProvider, "luma")
                        .eq(VideoGeneration::getStatus, VideoGeneration.GenerationStatus.COMPLETED)))
                .build();
    }
}
