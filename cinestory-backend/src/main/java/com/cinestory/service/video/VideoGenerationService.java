package com.cinestory.service.video;

import com.cinestory.model.dto.VideoGenerationRequest;
import com.cinestory.model.entity.VideoGeneration;
import com.cinestory.repository.VideoGenerationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 视频生成服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VideoGenerationService {

    private final List<VideoGenerationProvider> providers;
    private final VideoGenerationRepository videoGenerationRepository;

    /**
     * 生成视频（异步）
     */
    @Async
    public void generateVideoAsync(VideoGeneration generation, VideoGenerationRequest request) {
        log.info("Starting video generation for slice: {}", generation.getTextSliceId());

        // 尝试所有提供商，直到成功
        for (VideoGenerationProvider provider : providers) {
            if (!provider.isAvailable()) {
                log.debug("Provider {} not available, skipping", provider.getProviderName());
                continue;
            }

            try {
                log.info("Attempting video generation with provider: {}", provider.getProviderName());
                VideoGenerationResult result = provider.generate(request);

                if (result.isSuccess()) {
                    handleSuccess(generation, result);
                    return;
                } else {
                    log.warn("Provider {} failed: {}", provider.getProviderName(), result.getErrorMessage());
                }
            } catch (Exception e) {
                log.error("Error with provider {}", provider.getProviderName(), e);
            }
        }

        // 所有提供商都失败
        handleFailure(generation, "All video generation providers failed");
    }

    /**
     * 检查并更新视频生成状态
     */
    public void checkAndUpdateStatus(Long generationId) {
        Optional<VideoGeneration> optional = videoGenerationRepository.findById(generationId);
        if (optional.isEmpty()) {
            return;
        }

        VideoGeneration generation = optional.get();
        if (generation.getTaskId() == null) {
            return;
        }

        VideoGenerationProvider provider = findProvider(generation.getProvider());
        if (provider == null) {
            log.warn("Provider not found: {}", generation.getProvider());
            return;
        }

        try {
            VideoGenerationStatus status = provider.checkStatus(generation.getTaskId());
            updateGenerationStatus(generation, status);
        } catch (Exception e) {
            log.error("Error checking status for generation {}", generationId, e);
        }
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
        generation.setStatus(com.cinestory.model.entity.GenerationStatus.PENDING);
        generation.setRetryCount(generation.getRetryCount() + 1);
        generation.setErrorMessage(null);

        videoGenerationRepository.save(generation);

        // 重新生成（这里简化处理，实际应该重新构造请求）
        log.info("Retrying generation for slice: {}", generation.getTextSliceId());
    }

    /**
     * 处理生成成功
     */
    private void handleSuccess(VideoGeneration generation, VideoGenerationResult result) {
        generation.setTaskId(result.getTaskId());
        generation.setProvider(result.getProvider());

        if (result.getVideoUrl() != null) {
            generation.setStatus(com.cinestory.model.entity.GenerationStatus.COMPLETED);
            generation.setVideoUrl(result.getVideoUrl());
            generation.setCompletedAt(java.time.LocalDateTime.now());
        } else {
            generation.setStatus(com.cinestory.model.entity.GenerationStatus.PROCESSING);
        }

        videoGenerationRepository.save(generation);
        log.info("Video generation succeeded for slice: {}", generation.getTextSliceId());
    }

    /**
     * 处理生成失败
     */
    private void handleFailure(VideoGeneration generation, String errorMessage) {
        generation.setStatus(com.cinestory.model.entity.GenerationStatus.FAILED);
        generation.setErrorMessage(errorMessage);
        videoGenerationRepository.save(generation);
        log.error("Video generation failed for slice: {}", generation.getTextSliceId());
    }

    /**
     * 更新生成状态
     */
    private void updateGenerationStatus(VideoGeneration generation, VideoGenerationStatus status) {
        switch (status.getStatus()) {
            case COMPLETED:
                generation.setStatus(com.cinestory.model.entity.GenerationStatus.COMPLETED);
                generation.setVideoUrl(status.getVideoUrl());
                generation.setCompletedAt(java.time.LocalDateTime.now());
                break;
            case FAILED:
                generation.setStatus(com.cinestory.model.entity.GenerationStatus.FAILED);
                generation.setErrorMessage(status.getErrorMessage());
                break;
            case PROCESSING:
                generation.setStatus(com.cinestory.model.entity.GenerationStatus.PROCESSING);
                break;
            case PENDING:
                generation.setStatus(com.cinestory.model.entity.GenerationStatus.PENDING);
                break;
        }

        videoGenerationRepository.save(generation);
    }

    /**
     * 查找提供商
     */
    private VideoGenerationProvider findProvider(String providerName) {
        return providers.stream()
                .filter(p -> p.getProviderName().equalsIgnoreCase(providerName))
                .findFirst()
                .orElse(null);
    }
}
