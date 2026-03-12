package com.cinestory.service.impl;

import com.cinestory.exception.ResourceNotFoundException;
import com.cinestory.model.dto.request.CreateProjectRequest;
import com.cinestory.model.dto.request.StartTaskRequest;
import com.cinestory.model.dto.request.UpdateProjectRequest;
import com.cinestory.model.entity.Project;
import com.cinestory.model.entity.ProjectStatus;
import com.cinestory.repository.ProjectRepository;
import com.cinestory.service.ProjectService;
import com.cinestory.service.StyleTemplateService;
import com.cinestory.service.TextSplitterService;
import com.cinestory.service.websocket.ProgressWebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 项目服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final StyleTemplateService styleTemplateService;
    private final TextSplitterService textSplitterService;
    private final ProgressWebSocketService progressWebSocketService;

    @Override
    @Transactional
    public Project createProject(CreateProjectRequest request) {
        log.info("Creating project: {}", request.getName());

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .novelTitle(request.getNovelTitle())
                .novelAuthor(request.getNovelAuthor())
                .novelContent(request.getNovelContent())
                .styleTemplateId(request.getStyleTemplateId())
                .configJson(request.getConfigJson())
                .status(ProjectStatus.DRAFT)
                .progress(0)
                .totalSlices(0)
                .processedSlices(0)
                .succeededSlices(0)
                .failedSlices(0)
                .build();

        Project saved = projectRepository.save(project);
        log.info("Project created with id: {}", saved.getId());
        return saved;
    }

    @Override
    public Page<Project> getProjects(Pageable pageable) {
        return projectRepository.findAll(pageable);
    }

    @Override
    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
    }

    @Override
    @Transactional
    public Project updateProject(Long id, UpdateProjectRequest request) {
        log.info("Updating project: {}", id);

        Project project = getProjectById(id);

        if (request.getName() != null) {
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getStyleTemplateId() != null) {
            project.setStyleTemplateId(request.getStyleTemplateId());
        }
        if (request.getConfigJson() != null) {
            project.setConfigJson(request.getConfigJson());
        }

        return projectRepository.save(project);
    }

    @Override
    @Transactional
    public void deleteProject(Long id) {
        log.info("Deleting project: {}", id);
        Project project = getProjectById(id);

        // 只允许删除草稿或已完成的项目
        if (project.getStatus() == ProjectStatus.PROCESSING) {
            throw new IllegalStateException("Cannot delete project in processing status");
        }

        projectRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Project startTask(Long id, StartTaskRequest request) {
        log.info("Starting task for project: {}", id);

        Project project = getProjectById(id);

        if (project.getStatus() != ProjectStatus.DRAFT) {
            throw new IllegalStateException("Project is not in DRAFT status");
        }

        // 更新项目状态
        project.setStatus(ProjectStatus.PROCESSING);
        project.setStartedAt(LocalDateTime.now());
        project.setCurrentStep("正在初始化...");
        project.setProgress(0);

        Project savedProject = projectRepository.save(project);

        // 发送 WebSocket 开始消息
        progressWebSocketService.sendTaskStarted(id, "VIDEO_GENERATION");

        // 异步执行视频生成任务
        processVideoGenerationAsync(id, request);

        return savedProject;
    }

    @Override
    @Transactional
    public void cancelTask(Long id) {
        log.info("Cancelling task for project: {}", id);

        Project project = getProjectById(id);

        if (project.getStatus() != ProjectStatus.PROCESSING) {
            throw new IllegalStateException("Project is not in processing status");
        }

        project.setStatus(ProjectStatus.CANCELLED);
        project.setCurrentStep("任务已取消");
        projectRepository.save(project);

        // 发送 WebSocket 取消消息
        progressWebSocketService.sendTaskFailed(id, "VIDEO_GENERATION", "Task cancelled by user");
    }

    /**
     * 异步处理视频生成任务
     */
    @Async
    public void processVideoGenerationAsync(Long projectId, StartTaskRequest request) {
        log.info("Processing video generation for project: {}", projectId);

        try {
            // 1. 文本切片阶段 (0-20%)
            progressWebSocketService.sendProgressUpdate(projectId, "VIDEO_GENERATION", 5, "正在分析文本...");
            Thread.sleep(1000); // 模拟处理

            progressWebSocketService.sendProgressUpdate(projectId, "VIDEO_GENERATION", 15, "正在切片文本...");
            Thread.sleep(1000); // 模拟处理

            // 2. 提示词生成阶段 (20-40%)
            progressWebSocketService.sendProgressUpdate(projectId, "VIDEO_GENERATION", 25, "正在生成提示词...");
            Thread.sleep(1000); // 模拟处理

            // 3. 视频生成阶段 (40-90%)
            progressWebSocketService.sendProgressUpdate(projectId, "VIDEO_GENERATION", 40, "正在生成视频片段...");
            Thread.sleep(2000); // 模拟处理

            progressWebSocketService.sendProgressUpdate(projectId, "VIDEO_GENERATION", 70, "视频生成中...");
            Thread.sleep(2000); // 模拟处理

            // 4. 视频拼接阶段 (90-100%)
            progressWebSocketService.sendProgressUpdate(projectId, "VIDEO_GENERATION", 90, "正在拼接视频...");
            Thread.sleep(1000); // 模拟处理

            // 完成任务
            progressWebSocketService.sendProgressUpdate(projectId, "VIDEO_GENERATION", 95, "正在上传视频...");
            Thread.sleep(500); // 模拟处理

            // 更新项目状态为已完成
            updateProgress(projectId, 100, "已完成");
            progressWebSocketService.sendTaskCompleted(projectId, "VIDEO_GENERATION");

            log.info("Video generation completed for project: {}", projectId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            progressWebSocketService.sendTaskFailed(projectId, "VIDEO_GENERATION", "Task interrupted");
        } catch (Exception e) {
            log.error("Video generation failed for project: {}", projectId, e);
            progressWebSocketService.sendTaskFailed(projectId, "VIDEO_GENERATION", e.getMessage());
        }
    }

    @Override
    @Transactional
    public void updateProgress(Long projectId, int progress, String currentStep) {
        projectRepository.findById(projectId).ifPresent(project -> {
            project.setProgress(progress);
            project.setCurrentStep(currentStep);
            projectRepository.save(project);
        });
    }
}
