package com.cinestory.service.scheduler;

import com.cinestory.model.entity.VideoGeneration;
import com.cinestory.model.entity.VideoGeneration.GenerationStatus;
import com.cinestory.repository.TextSliceRepository;
import com.cinestory.repository.VideoGenerationRepository;
import com.cinestory.service.video.VideoGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 视频生成状态轮询定时任务
 * 定期检查处理中的视频生成任务状态
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VideoGenerationPollScheduler {

    private final VideoGenerationRepository videoGenerationRepository;
    private final TextSliceRepository textSliceRepository;
    private final VideoGenerationService videoGenerationService;

    /**
     * 轮询处理中的视频生成任务
     * 每 30 秒执行一次
     */
    @Scheduled(fixedDelay = 30000, initialDelay = 10000)
    public void pollPendingGenerations() {
        log.debug("Polling pending video generations...");

        List<VideoGeneration> pendingGenerations = videoGenerationRepository
                .findByStatusIn(List.of(
                        GenerationStatus.PENDING,
                        GenerationStatus.PROCESSING
                ));

        if (pendingGenerations.isEmpty()) {
            log.debug("No pending video generations found");
            return;
        }

        log.info("Found {} pending video generations", pendingGenerations.size());

        for (VideoGeneration generation : pendingGenerations) {
            try {
                checkAndUpdateGeneration(generation);
            } catch (Exception e) {
                log.error("Error checking generation status: {}", generation.getId(), e);
            }
        }
    }

    /**
     * 检查并更新单个视频生成任务状态
     */
    private void checkAndUpdateGeneration(VideoGeneration generation) {
        log.debug("Checking generation: {}", generation.getId());

        // 超时检查（超过 1 小时未完成）
        if (isTimeout(generation)) {
            handleTimeout(generation);
            return;
        }

        // 检查状态
        videoGenerationService.checkAndUpdateStatus(generation.getId());
    }

    /**
     * 检查是否超时
     */
    private boolean isTimeout(VideoGeneration generation) {
        if (generation.getCreatedAt() == null) {
            return false;
        }

        LocalDateTime timeout = generation.getCreatedAt().plusHours(1);
        return LocalDateTime.now().isAfter(timeout);
    }

    /**
     * 处理超时任务
     */
    private void handleTimeout(VideoGeneration generation) {
        log.warn("Generation timeout: {}", generation.getId());

        generation.setStatus(GenerationStatus.FAILED);
        generation.setErrorMessage("Generation timeout");
        videoGenerationRepository.save(generation);

        // 检查是否需要重试
        if (generation.getRetryCount() < 3) {
            videoGenerationService.retryFailedGeneration(generation.getId());
        }
    }

    /**
     * 清理过期的临时文件
     * 每天凌晨 2 点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupTempFiles() {
        log.info("Starting temp files cleanup...");
        try {
            Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "cinestory");
            if (!Files.exists(tempDir)) {
                log.debug("Temp directory does not exist: {}", tempDir);
                return;
            }

            long cutoffTime = System.currentTimeMillis() - (24 * 60 * 60 * 1000); // 24小时前
            AtomicInteger deletedCount = new AtomicInteger(0);

            Files.walk(tempDir)
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        try {
                            return Files.getLastModifiedTime(path).toMillis() < cutoffTime;
                        } catch (IOException e) {
                            log.warn("Failed to get last modified time for: {}", path, e);
                            return false;
                        }
                    })
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                            deletedCount.incrementAndGet();
                            log.debug("Deleted temp file: {}", path);
                        } catch (IOException e) {
                            log.warn("Failed to delete temp file: {}", path, e);
                        }
                    });

            log.info("Temp files cleanup completed. Deleted {} files.", deletedCount.get());
        } catch (Exception e) {
            log.error("Error during temp files cleanup", e);
        }
    }
}
