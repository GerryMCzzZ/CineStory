package com.cinestory.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cinestory.model.dto.request.CreateProjectRequest;
import com.cinestory.model.dto.request.StartTaskRequest;
import com.cinestory.model.dto.request.UpdateProjectRequest;
import com.cinestory.model.dto.response.ApiResponse;
import com.cinestory.model.dto.response.PageResponse;
import com.cinestory.model.dto.response.ProjectResponse;
import com.cinestory.model.dto.response.TaskResponse;
import com.cinestory.model.entity.Project;
import com.cinestory.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 项目管理控制器
 *
 * @author CineStory
 */
@Tag(name = "项目管理", description = "项目的创建、查询、更新、删除等操作")
@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @Operation(summary = "创建项目", description = "创建一个新的视频生成项目")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "创建成功",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "请求参数验证失败")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @Parameter(description = "项目创建请求", required = true)
            @Valid @RequestBody CreateProjectRequest request) {
        Project project = projectService.createProject(request);
        ProjectResponse response = ProjectResponse.fromEntity(project);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @Operation(summary = "获取项目列表", description = "分页查询项目列表")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProjectResponse>>> getProjects(
            @Parameter(description = "页码（从0开始）") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向 (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir) {

        // MyBatis-Plus 页码从 1 开始，前端从 0 开始，需转换
        Page<Project> mpPage = new Page<>(page + 1, size);
        IPage<Project> projects = projectService.getProjects(mpPage);

        List<ProjectResponse> responses = projects.getRecords().stream()
                .map(ProjectResponse::fromEntity)
                .collect(Collectors.toList());

        // 返回给前端的页码从 0 开始
        PageResponse<ProjectResponse> pageResponse = PageResponse.of(
                responses,
                projects.getTotal(),
                (int) projects.getCurrent() - 1,
                (int) projects.getSize()
        );

        return ResponseEntity.ok(ApiResponse.page(pageResponse));
    }

    @Operation(summary = "获取项目详情", description = "根据ID获取项目详细信息")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProject(
            @Parameter(description = "项目ID", required = true) @PathVariable Long id) {
        Project project = projectService.getProjectById(id);
        ProjectResponse response = ProjectResponse.fromEntity(project);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "更新项目", description = "更新项目信息")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @Parameter(description = "项目ID", required = true) @PathVariable Long id,
            @Parameter(description = "更新请求", required = true)
            @Valid @RequestBody UpdateProjectRequest request) {
        Project project = projectService.updateProject(id, request);
        ProjectResponse response = ProjectResponse.fromEntity(project);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "删除项目", description = "删除指定项目")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            @Parameter(description = "项目ID", required = true) @PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "启动任务", description = "开始视频生成任务")
    @PostMapping("/{id}/start")
    public ResponseEntity<ApiResponse<TaskResponse>> startTask(
            @Parameter(description = "项目ID", required = true) @PathVariable Long id,
            @Parameter(description = "启动任务配置") @RequestBody StartTaskRequest request) {
        Project project = projectService.startTask(id, request);
        TaskResponse response = TaskResponse.fromEntity(project);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "取消任务", description = "取消正在进行的视频生成任务")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelTask(
            @Parameter(description = "项目ID", required = true) @PathVariable Long id) {
        projectService.cancelTask(id);
        return ResponseEntity.ok(ApiResponse.success("Task cancelled successfully", null));
    }

    @Operation(summary = "获取任务进度", description = "获取视频生成任务的当前进度")
    @GetMapping("/{id}/progress")
    public ResponseEntity<ApiResponse<TaskResponse>> getProgress(
            @Parameter(description = "项目ID", required = true) @PathVariable Long id) {
        Project project = projectService.getProjectById(id);
        TaskResponse response = TaskResponse.fromEntity(project);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
