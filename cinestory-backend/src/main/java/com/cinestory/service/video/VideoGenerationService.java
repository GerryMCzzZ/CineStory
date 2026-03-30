package com.cinestory.service.video;

import com.cinestory.controller.VideoGenerationController.GenerationStats;
import com.cinestory.model.dto.response.VideoGenerationResponse;
import com.cinestory.model.entity.VideoGeneration;
import com.cinestory.repository.VideoGenerationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 视频生成服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VideoGenerationService {

    private final VideoGenerationRepository videoGenerationRepository;

    /**
     * 检查并更新视频生成状态
     */
    public void checkAndUpdateStatus(Long generationId) {
        Optional<VideoGeneration> optional = videoGenerationRepository.findById(generationId);
        if (optional.isEmpty()) {
            return;
        }

        VideoGeneration generation = optional.get();
        if (generation.getProviderTaskId() == null) {
            return;
        }

        log.debug("Checking status for generation: {}", generationId);
    }

    /**
     * 重试失败的视频生成
     */
    public void retryFailedGeneration(Long generationId) {
        Optional<VideoGeneration> optional = videoGenerationRepository.findById(generationId);
        if (optional.isEmpty()) {
            return;
        }

        VideoGeneration generation = optional.get();

        // 重置状态
        generation.setStatus(VideoGeneration.GenerationStatus.PENDING);
        generation.setRetryCount(generation.getRetryCount() + 1);
        generation.setErrorMessage(null);

        videoGenerationRepository.save(generation);
        log.info("Retrying generation for slice: {}", generation.getSliceId());
    }

    /**
     * 分页查询视频生成历史
     */
    public Page<VideoGenerationResponse> getHistory(Pageable pageable, VideoGeneration.GenerationStatus status, String provider) {
        Page<VideoGeneration> generations;

        if (status != null && provider != null) {
            // 按状态和提供商筛选 - 简化为按状态筛选
            generations = videoGenerationRepository.findByStatus(status, pageable);
        } else if (status != null) {
            generations = videoGenerationRepository.findByStatus(status, pageable);
        } else {
            generations = videoGenerationRepository.findAll(pageable);
        }

        return generations.map(VideoGenerationResponse::fromEntity);
    }

    /**
     * 查询生成详情
     */
    public VideoGenerationResponse getById(Long id) {
        Optional<VideoGeneration> optional = videoGenerationRepository.findById(id);
        return optional.map(VideoGenerationResponse::fromEntity).orElse(null);
    }

    /**
     * 重试失败的生成
     */
    @Transactional
    public void retryGeneration(Long id) {
        Optional<VideoGeneration> optional = videoGenerationRepository.findById(id);
        if (optional.isEmpty()) {
            throw new IllegalArgumentException("Video generation not found: " + id);
        }

        VideoGeneration generation = optional.get();
        if (!generation.getStatus().equals(VideoGeneration.GenerationStatus.FAILED)) {
            throw new IllegalStateException("Can only retry failed generations");
        }

        // 重置状态
        generation.setStatus(VideoGeneration.GenerationStatus.PENDING);
        generation.setRetryCount(generation.getRetryCount() + 1);
        generation.setErrorMessage(null);
        generation.setProviderTaskId(null);

        videoGenerationRepository.save(generation);
        log.info("Retrying generation {} for slice: {}", id, generation.getSliceId());
    }

    /**
     * 取消正在处理的生成
     */
    @Transactional
    public void cancelGeneration(Long id) {
        Optional<VideoGeneration> optional = videoGenerationRepository.findById(id);
        if (optional.isEmpty()) {
            throw new IllegalArgumentException("Video generation not found: " + id);
        }

        VideoGeneration generation = optional.get();
        if (!generation.getStatus().equals(VideoGeneration.GenerationStatus.PROCESSING)
                && !generation.getStatus().equals(VideoGeneration.GenerationStatus.PENDING)) {
            throw new IllegalStateException("Can only cancel pending or processing generations");
        }

        generation.setStatus(VideoGeneration.GenerationStatus.CANCELLED);
        generation.setCompletedAt(java.time.LocalDateTime.now());

        videoGenerationRepository.save(generation);
        log.info("Cancelled generation {} for slice: {}", id, generation.getSliceId());
    }

    /**
     * 获取生成统计
     */
    public GenerationStats getStats() {
        return GenerationStats.builder()
                .total(videoGenerationRepository.count())
                .pending(videoGenerationRepository.countByStatus(VideoGeneration.GenerationStatus.PENDING))
                .processing(videoGenerationRepository.countByStatus(VideoGeneration.GenerationStatus.PROCESSING))
                .completed(videoGenerationRepository.countByStatus(VideoGeneration.GenerationStatus.COMPLETED))
                .failed(videoGenerationRepository.countByStatus(VideoGeneration.GenerationStatus.FAILED))
                .cancelled(videoGenerationRepository.countByStatus(VideoGeneration.GenerationStatus.CANCELLED))
                .runwayTotal(videoGenerationRepository.countByProviderStartingWith("runway"))
                .runwayCompleted(videoGenerationRepository.countByProviderAndStatus(
                        "runway", VideoGeneration.GenerationStatus.COMPLETED))
                .pikaTotal(videoGenerationRepository.countByProviderStartingWith("pika"))
                .pikaCompleted(videoGenerationRepository.countByProviderAndStatus(
                        "pika", VideoGeneration.GenerationStatus.COMPLETED))
                .lumaTotal(videoGenerationRepository.countByProviderStartingWith("luma"))
                .lumaCompleted(videoGenerationRepository.countByProviderAndStatus(
                        "luma", VideoGeneration.GenerationStatus.COMPLETED))
                .build();
    }
}
