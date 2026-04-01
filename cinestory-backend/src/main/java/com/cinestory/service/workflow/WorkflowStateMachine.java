package com.cinestory.service.workflow;

import com.cinestory.mapper.ProjectMapper;
import com.cinestory.model.entity.Project;
import com.cinestory.service.video.VideoCompositionService;
import com.cinestory.service.video.VideoGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 工作流状态机服务
 * 管理视频生成项目的完整生命周期
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowStateMachine {

    private final ProjectMapper projectMapper;
    private final VideoGenerationService videoGenerationService;
    private final VideoCompositionService videoCompositionService;

    /**
     * 定义工作流步骤
     */
    public enum WorkflowStep {
        INIT("初始化", 0),
        TEXT_SLICING("文本切片", 10),
        PROMPT_GENERATION("生成提示词", 20),
        VIDEO_GENERATION("生成视频片段", 30),
        VIDEO_COMPOSITION("合成视频", 90),
        COMPLETED("完成", 100);

        private final String description;
        private final int progressPercent;

        WorkflowStep(String description, int progressPercent) {
            this.description = description;
            this.progressPercent = progressPercent;
        }

        public String getDescription() {
            return description;
        }

        public int getProgressPercent() {
            return progressPercent;
        }
    }

    /**
     * 定义状态转换规则
     */
    public enum StateTransition {
        DRAFT_TO_PROCESSING(Project.ProjectStatus.DRAFT, Project.ProjectStatus.PROCESSING),
        PROCESSING_TO_COMPLETED(Project.ProjectStatus.PROCESSING, Project.ProjectStatus.COMPLETED),
        PROCESSING_TO_FAILED(Project.ProjectStatus.PROCESSING, Project.ProjectStatus.FAILED),
        PROCESSING_TO_CANCELLED(Project.ProjectStatus.PROCESSING, Project.ProjectStatus.CANCELLED),
        FAILED_TO_PROCESSING(Project.ProjectStatus.FAILED, Project.ProjectStatus.PROCESSING);

        private final Project.ProjectStatus from;
        private final Project.ProjectStatus to;

        StateTransition(Project.ProjectStatus from, Project.ProjectStatus to) {
            this.from = from;
            this.to = to;
        }

        public boolean canTransition(Project.ProjectStatus current) {
            return from == current;
        }
    }

    /**
     * 启动项目工作流
     */
    @Transactional
    public void startProject(Long projectId) {
        Project project = getProject(projectId);

        // 验证状态转换
        validateTransition(project.getStatus(), Project.ProjectStatus.PROCESSING);

        // 更新状态
        project.setStatus(Project.ProjectStatus.PROCESSING);
        project.setStartedAt(LocalDateTime.now());
        project.setCurrentStep(WorkflowStep.INIT.getDescription());
        project.setProgress(WorkflowStep.INIT.getProgressPercent());

        projectMapper.updateById(project);
        log.info("Started workflow for project: {}", projectId);
    }

    /**
     * 更新工作流步骤
     */
    @Transactional
    public void updateStep(Long projectId, WorkflowStep step, String detail) {
        Project project = getProject(projectId);
        project.setCurrentStep(step.getDescription() + (detail != null ? ": " + detail : ""));
        project.setProgress(step.getProgressPercent());
        projectMapper.updateById(project);
        log.debug("Updated step for project {}: {}", projectId, step.getDescription());
    }

    /**
     * 更新工作流进度
     */
    @Transactional
    public void updateProgress(Long projectId, int progress, String detail) {
        Project project = getProject(projectId);
        project.setProgress(Math.min(100, Math.max(0, progress)));
        if (detail != null) {
            project.setCurrentStep(detail);
        }
        projectMapper.updateById(project);
        log.debug("Updated progress for project {}: {}%", projectId, progress);
    }

    /**
     * 完成项目
     */
    @Transactional
    public void completeProject(Long projectId, String outputVideoUrl) {
        Project project = getProject(projectId);

        validateTransition(project.getStatus(), Project.ProjectStatus.COMPLETED);

        project.setStatus(Project.ProjectStatus.COMPLETED);
        project.setCompletedAt(LocalDateTime.now());
        project.setCurrentStep(WorkflowStep.COMPLETED.getDescription());
        project.setProgress(100);
        project.setOutputVideoUrl(outputVideoUrl);

        projectMapper.updateById(project);
        log.info("Completed workflow for project: {}", projectId);
    }

    /**
     * 失败项目
     */
    @Transactional
    public void failProject(Long projectId, String errorMessage) {
        Project project = getProject(projectId);

        validateTransition(project.getStatus(), Project.ProjectStatus.FAILED);

        project.setStatus(Project.ProjectStatus.FAILED);
        project.setCompletedAt(LocalDateTime.now());
        project.setCurrentStep("失败");

        projectMapper.updateById(project);
        log.error("Failed workflow for project {}: {}", projectId, errorMessage);
    }

    /**
     * 取消项目
     */
    @Transactional
    public void cancelProject(Long projectId, String reason) {
        Project project = getProject(projectId);

        validateTransition(project.getStatus(), Project.ProjectStatus.CANCELLED);

        project.setStatus(Project.ProjectStatus.CANCELLED);
        project.setCompletedAt(LocalDateTime.now());
        project.setCurrentStep("已取消");

        projectMapper.updateById(project);
        log.info("Cancelled workflow for project: {}", projectId);
    }

    /**
     * 重试失败的项目
     */
    @Transactional
    public void retryProject(Long projectId) {
        Project project = getProject(projectId);

        if (project.getStatus() != Project.ProjectStatus.FAILED) {
            throw new IllegalStateException("Can only retry failed projects");
        }

        // 重置状态
        project.setStatus(Project.ProjectStatus.PROCESSING);
        project.setStartedAt(LocalDateTime.now());
        project.setCurrentStep(WorkflowStep.INIT.getDescription());
        project.setProgress(WorkflowStep.INIT.getProgressPercent());
        project.setCompletedAt(null);

        projectMapper.updateById(project);
        log.info("Retrying workflow for project: {}", projectId);
    }

    /**
     * 验证状态转换是否合法
     */
    private void validateTransition(Project.ProjectStatus current, Project.ProjectStatus target) {
        boolean valid = false;
        for (StateTransition transition : StateTransition.values()) {
            if (transition.from == current && transition.to == target) {
                valid = true;
                break;
            }
        }

        if (!valid) {
            throw new IllegalStateException(
                    String.format("Invalid state transition from %s to %s", current, target));
        }
    }

    /**
     * 检查项目是否可以转换到目标状态
     */
    public boolean canTransitionTo(Long projectId, Project.ProjectStatus target) {
        Project project = getProject(projectId);
        for (StateTransition transition : StateTransition.values()) {
            if (transition.from == project.getStatus() && transition.to == target) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取项目当前状态信息
     */
    public ProjectStatusInfo getStatusInfo(Long projectId) {
        Project project = getProject(projectId);
        return ProjectStatusInfo.builder()
                .projectId(projectId)
                .status(project.getStatus())
                .currentStep(project.getCurrentStep())
                .progress(project.getProgress())
                .startedAt(project.getStartedAt())
                .completedAt(project.getCompletedAt())
                .outputVideoUrl(project.getOutputVideoUrl())
                .canStart(canTransitionTo(projectId, Project.ProjectStatus.PROCESSING))
                .canCancel(canTransitionTo(projectId, Project.ProjectStatus.CANCELLED))
                .canRetry(project.getStatus() == Project.ProjectStatus.FAILED)
                .canDownload(project.getStatus() == Project.ProjectStatus.COMPLETED)
                .build();
    }

    private Project getProject(Long projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new IllegalArgumentException("Project not found: " + projectId);
        }
        return project;
    }

    /**
     * 项目状态信息 DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ProjectStatusInfo {
        private Long projectId;
        private Project.ProjectStatus status;
        private String currentStep;
        private Integer progress;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private String outputVideoUrl;
        private boolean canStart;
        private boolean canCancel;
        private boolean canRetry;
        private boolean canDownload;
    }
}
