package com.cinestory.model.dto.response;

import com.cinestory.model.entity.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 任务响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {

    private Long id;
    private Long projectId;
    private String projectName;
    private String status;
    private String currentStep;
    private Integer progress;
    private String outputVideoUrl;
    private Integer totalDuration;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    /**
     * 从项目实体转换为任务响应 DTO
     */
    public static TaskResponse fromEntity(Project project) {
        return TaskResponse.builder()
                .id(project.getId())
                .projectId(project.getId())
                .projectName(project.getName())
                .status(project.getStatus() != null ? project.getStatus().name() : null)
                .currentStep(project.getCurrentStep())
                .progress(project.getProgress())
                .outputVideoUrl(project.getOutputVideoUrl())
                .totalDuration(project.getTotalDuration())
                .createdAt(project.getCreatedAt())
                .startedAt(project.getStartedAt())
                .completedAt(project.getCompletedAt())
                .build();
    }
}
