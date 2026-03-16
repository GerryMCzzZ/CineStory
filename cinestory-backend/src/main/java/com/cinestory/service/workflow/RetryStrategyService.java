package com.cinestory.service.workflow;

import com.cinestory.model.entity.VideoGeneration;
import com.cinestory.repository.VideoGenerationRepository;
import com.cinestory.service.video.VideoGenerationProvider;
import com.cinestory.service.video.VideoGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 智能重试策略服务
 * 处理视频生成的失败重试逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RetryStrategyService {

    private final VideoGenerationRepository videoGenerationRepository;
    private final VideoGenerationService videoGenerationService;
    private final List<VideoGenerationProvider> providers;

    @Value("${video.retry.max-attempts:3}")
    private int maxRetryAttempts;

    @Value("${video.retry.initial-delay-seconds:60}")
    private int initialDelaySeconds;

    @Value("${video.retry.max-delay-seconds:3600}")
    private int maxDelaySeconds;

    @Value("${video.retry.backoff-multiplier:2.0}")
    private double backoffMultiplier;

    @Value("${video.retry.provider-switch-enabled:true}")
    private boolean providerSwitchEnabled;

    // 跟踪重试状态
    private final Map<Long, RetryState> retryStates = new ConcurrentHashMap<>();

    /**
     * 重试状态
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RetryState {
        private Long generationId;
        private int attemptCount;
        private String lastProvider;
        private LocalDateTime lastAttemptAt;
        private LocalDateTime nextAttemptAt;
        private String lastError;
        private RetryStatus status;

        public boolean canRetry() {
            return status == RetryStatus.PENDING && nextAttemptAt != null && nextAttemptAt.isBefore(LocalDateTime.now());
        }
    }

    /**
     * 重试状态枚举
     */
    public enum RetryStatus {
        PENDING,     // 等待重试
        RUNNING,     // 正在重试
        SUCCESS,     // 重试成功
        FAILED,      // 重试失败
        ABANDONED    // 放弃重试
    }

    /**
     * 处理失败的生成任务
     */
    @Transactional
    public void handleFailure(Long generationId, String errorMessage, String failedProvider) {
        VideoGeneration generation = videoGenerationRepository.findById(generationId).orElse(null);
        if (generation == null) {
            log.warn("Generation not found for retry: {}", generationId);
            return;
        }

        int attemptCount = generation.getRetryCount() + 1;

        // 检查是否超过最大重试次数
        if (attemptCount > maxRetryAttempts) {
            log.warn("Max retry attempts reached for generation: {}", generationId);
            abandonRetry(generationId);
            return;
        }

        // 计算下次重试时间（指数退避）
        LocalDateTime nextAttemptAt = calculateNextAttemptTime(attemptCount);

        // 选择下一个提供商
        String nextProvider = selectNextProvider(failedProvider, attemptCount);

        // 更新生成记录
        generation.setRetryCount(attemptCount);
        generation.setErrorMessage(errorMessage);
        videoGenerationRepository.save(generation);

        // 记录重试状态
        RetryState state = RetryState.builder()
                .generationId(generationId)
                .attemptCount(attemptCount)
                .lastProvider(failedProvider)
                .lastAttemptAt(LocalDateTime.now())
                .nextAttemptAt(nextAttemptAt)
                .lastError(errorMessage)
                .status(RetryStatus.PENDING)
                .build();

        retryStates.put(generationId, state);

        log.info("Scheduled retry #{} for generation {} at {} using provider {}",
                attemptCount, generationId, nextAttemptAt, nextProvider);
    }

    /**
     * 执行待处理的重试
     */
    @Scheduled(fixedDelay = 30000) // 每30秒检查一次
    @Transactional
    public void processPendingRetries() {
        List<Long> toRetry = new ArrayList<>();

        for (Map.Entry<Long, RetryState> entry : retryStates.entrySet()) {
            RetryState state = entry.getValue();
            if (state.canRetry()) {
                toRetry.add(state.getGenerationId());
            }
        }

        if (!toRetry.isEmpty()) {
            log.info("Processing {} pending retries", toRetry.size());
            for (Long generationId : toRetry) {
                executeRetry(generationId);
            }
        }
    }

    /**
     * 执行单个重试
     */
    @Transactional
    public void executeRetry(Long generationId) {
        RetryState state = retryStates.get(generationId);
        if (state == null || state.getStatus() != RetryStatus.PENDING) {
            return;
        }

        // 更新状态为运行中
        state.setStatus(RetryStatus.RUNNING);
        retryStates.put(generationId, state);

        VideoGeneration generation = videoGenerationRepository.findById(generationId).orElse(null);
        if (generation == null) {
            log.warn("Generation not found for retry: {}", generationId);
            abandonRetry(generationId);
            return;
        }

        // 重置生成状态
        generation.setStatus(VideoGeneration.GenerationStatus.PENDING);
        generation.setProvider(null);
        generation.setTaskId(null);
        generation.setErrorMessage(null);
        videoGenerationRepository.save(generation);

        log.info("Executing retry #{} for generation {}", state.getAttemptCount(), generationId);

        // 注意：这里不直接调用 generateVideoAsync，因为那需要 VideoGenerationRequest
        // 实际的重试应该由调度器触发
    }

    /**
     * 标记重试为成功
     */
    public void markSuccess(Long generationId) {
        RetryState state = retryStates.get(generationId);
        if (state != null) {
            state.setStatus(RetryStatus.SUCCESS);
            retryStates.put(generationId, state);

            // 清理成功的重试状态（延迟清理）
            scheduleCleanup(generationId, 300000); // 5分钟后清理
        }
    }

    /**
     * 放弃重试
     */
    @Transactional
    public void abandonRetry(Long generationId) {
        RetryState state = retryStates.get(generationId);
        if (state != null) {
            state.setStatus(RetryStatus.ABANDONED);
            retryStates.put(generationId, state);
        }

        VideoGeneration generation = videoGenerationRepository.findById(generationId).orElse(null);
        if (generation != null) {
            generation.setErrorMessage("Failed after " + state.getAttemptCount() + " attempts: " + state.getLastError());
            videoGenerationRepository.save(generation);
        }

        // 立即清理
        retryStates.remove(generationId);

        log.info("Abandoned retry for generation after {} attempts", generationId);
    }

    /**
     * 计算下次重试时间（指数退避）
     */
    private LocalDateTime calculateNextAttemptTime(int attemptCount) {
        long delaySeconds = (long) (initialDelaySeconds * Math.pow(backoffMultiplier, attemptCount - 1));
        delaySeconds = Math.min(delaySeconds, maxDelaySeconds);

        // 添加随机抖动（±20%）避免惊群效应
        double jitter = 0.8 + Math.random() * 0.4;
        delaySeconds = (long) (delaySeconds * jitter);

        return LocalDateTime.now().plusSeconds(delaySeconds);
    }

    /**
     * 选择下一个提供商
     */
    private String selectNextProvider(String failedProvider, int attemptCount) {
        if (!providerSwitchEnabled || providers.size() <= 1) {
            return failedProvider;
        }

        // 按优先级排序提供商
        List<VideoGenerationProvider> sortedProviders = new ArrayList<>(providers);
        sortedProviders.sort(Comparator.comparingInt(VideoGenerationProvider::getPriority));

        // 找到失败的提供商
        int failedIndex = -1;
        for (int i = 0; i < sortedProviders.size(); i++) {
            if (sortedProviders.get(i).getProviderName().equalsIgnoreCase(failedProvider)) {
                failedIndex = i;
                break;
            }
        }

        // 选择下一个可用的提供商
        for (int i = 1; i < sortedProviders.size(); i++) {
            int nextIndex = (failedIndex + i) % sortedProviders.size();
            VideoGenerationProvider nextProvider = sortedProviders.get(nextIndex);
            if (nextProvider.isAvailable()) {
                return nextProvider.getProviderName();
            }
        }

        return failedProvider; // 没有其他可用提供商，返回原提供商
    }

    /**
     * 获取重试统计
     */
    public RetryStats getRetryStats() {
        int total = retryStates.size();
        int pending = 0;
        int running = 0;
        int success = 0;
        int abandoned = 0;

        for (RetryState state : retryStates.values()) {
            switch (state.getStatus()) {
                case PENDING -> pending++;
                case RUNNING -> running++;
                case SUCCESS -> success++;
                case ABANDONED -> abandoned++;
            }
        }

        return RetryStats.builder()
                .totalRetries(total)
                .pending(pending)
                .running(running)
                .success(success)
                .abandoned(abandoned)
                .build();
    }

    /**
     * 清理过期的重试状态
     */
    @Scheduled(fixedDelay = 300000) // 每5分钟清理一次
    public void cleanupOldStates() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(10);

        retryStates.entrySet().removeIf(entry -> {
            RetryState state = entry.getValue();
            boolean shouldRemove = state.getStatus() == RetryStatus.SUCCESS
                    && state.getLastAttemptAt().isBefore(cutoff);
            if (shouldRemove) {
                log.debug("Cleaned up retry state for generation: {}", entry.getKey());
            }
            return shouldRemove;
        });
    }

    private void scheduleCleanup(Long generationId, long delayMillis) {
        // 使用 Spring 的 TaskScheduler 或简单地在 cleanupOldStates 中处理
    }

    /**
     * 重试统计
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RetryStats {
        private int totalRetries;
        private int pending;
        private int running;
        private int success;
        private int abandoned;
    }

    /**
     * 重试配置
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RetryConfig {
        private int maxAttempts;
        private int initialDelaySeconds;
        private int maxDelaySeconds;
        private double backoffMultiplier;
        private boolean providerSwitchEnabled;
    }

    /**
     * 获取当前重试配置
     */
    public RetryConfig getConfig() {
        return RetryConfig.builder()
                .maxAttempts(maxRetryAttempts)
                .initialDelaySeconds(initialDelaySeconds)
                .maxDelaySeconds(maxDelaySeconds)
                .backoffMultiplier(backoffMultiplier)
                .providerSwitchEnabled(providerSwitchEnabled)
                .build();
    }

    /**
     * 获取生成任务的重试状态
     */
    public RetryState getRetryState(Long generationId) {
        return retryStates.get(generationId);
    }

    /**
     * 清理所有重试状态（用于测试或手动重置）
     */
    public void clearAllRetries() {
        int count = retryStates.size();
        retryStates.clear();
        log.info("Cleared {} retry states", count);
    }
}
