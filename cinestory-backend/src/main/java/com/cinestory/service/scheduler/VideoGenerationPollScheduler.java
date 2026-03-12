package com.cinestory.service.scheduler;

import com.cinestory.model.entity.GenerationStatus;
import com.cinestory.model.entity.ProjectStatus;
import com.cinestory.model.entity.VideoGeneration;
import com.cinestory.repository.ProjectRepository;
import com.cinestory.repository.VideoGenerationRepository;
import com.cinestory.service.video.VideoGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 视频生成状态轮询定时任务
 * 定期检查处理中的视频生成任务状态
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VideoGenerationPollScheduler {

    private final VideoGenerationRepository videoGenerationRepository;
    private final ProjectRepository projectRepository;
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

        // 更新项目进度
        updateProjectProgress(generation.getProjectId());
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
     * 更新项目进度
     */
    private void updateProjectProgress(Long projectId) {
        projectRepository.findById(projectId).ifPresent(project -> {
            if (project.getStatus() != ProjectStatus.PROCESSING) {
                return;
            }

            // 统计视频生成进度
            List<VideoGeneration> allGenerations = videoGenerationRepository
                    .findByProjectId(projectId);

            int total = allGenerations.size();
            long completed = allGenerations.stream()
                    .filter(g -> g.getStatus() == GenerationStatus.COMPLETED)
                    .count();
            long failed = allGenerations.stream()
                    .filter(g -> g.getStatus() == GenerationStatus.FAILED)
                    .count();

            if (total > 0) {
                int progress = (int) ((completed + failed) * 100 / total);
                project.setProgress(progress);
                project.setProcessedSlices((int) (completed + failed));
                project.setSucceededSlices((int) completed);
                project.setFailedSlices((int) failed);

                // 检查是否全部完成
                if (completed + failed >= total) {
                    if (failed == 0) {
                        project.setStatus(ProjectStatus.COMPLETED);
                        project.setCompletedAt(LocalDateTime.now());
                        project.setCurrentStep("视频生成完成");
                    } else if (completed == 0) {
                        project.setStatus(ProjectStatus.FAILED);
                        project.setCurrentStep("视频生成失败");
                    } else {
                        project.setStatus(ProjectStatus.COMPLETED);
                        project.setCurrentStep("部分完成，有 " + failed + " 个片段失败");
                    }
                }

                projectRepository.save(project);
            }
        });
    }

    /**
     * 清理过期的临时文件
     * 每天凌晨 2 点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupTempFiles() {
        log.info("Starting temp files cleanup...");
        // TODO: 实现临时文件清理逻辑
        log.info("Temp files cleanup completed");
    }
}
