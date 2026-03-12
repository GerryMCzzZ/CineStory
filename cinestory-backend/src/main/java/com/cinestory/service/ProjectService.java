package com.cinestory.service;

import com.cinestory.model.dto.request.CreateProjectRequest;
import com.cinestory.model.dto.request.StartTaskRequest;
import com.cinestory.model.dto.request.UpdateProjectRequest;
import com.cinestory.model.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 项目服务接口
 */
public interface ProjectService {

    /**
     * 创建项目
     */
    Project createProject(CreateProjectRequest request);

    /**
     * 获取项目列表（分页）
     */
    Page<Project> getProjects(Pageable pageable);

    /**
     * 根据 ID 获取项目
     */
    Project getProjectById(Long id);

    /**
     * 更新项目
     */
    Project updateProject(Long id, UpdateProjectRequest request);

    /**
     * 删除项目
     */
    void deleteProject(Long id);

    /**
     * 启动任务
     */
    Project startTask(Long id, StartTaskRequest request);

    /**
     * 取消任务
     */
    void cancelTask(Long id);

    /**
     * 更新项目进度
     */
    void updateProgress(Long projectId, int progress, String currentStep);
}
