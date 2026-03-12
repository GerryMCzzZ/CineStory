package com.cinestory.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 进度消息 DTO
 * 用于 WebSocket 推送任务进度
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressMessage {

    /**
     * 项目 ID
     */
    private Long projectId;

    /**
     * 任务类型
     */
    private String taskType;

    /**
     * 当前步骤
     */
    private String currentStep;

    /**
     * 进度百分比 (0-100)
     */
    private Integer progress;

    /**
     * 总数
     */
    private Integer total;

    /**
     * 已处理数
     */
    private Integer processed;

    /**
     * 成功数
     */
    private Integer succeeded;

    /**
     * 失败数
     */
    private Integer failed;

    /**
     * 状态
     */
    private String status;

    /**
     * 错误信息（如果有）
     */
    private String errorMessage;

    /**
     * 时间戳
     */
    private LocalDateTime timestamp;

    /**
     * 额外数据
     */
    private Object data;

    /**
     * 创建开始消息
     */
    public static ProgressMessage start(Long projectId, String taskType) {
        return ProgressMessage.builder()
                .projectId(projectId)
                .taskType(taskType)
                .status("STARTED")
                .progress(0)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 创建进度更新消息
     */
    public static ProgressMessage update(Long projectId, String taskType, int progress, String currentStep) {
        return ProgressMessage.builder()
                .projectId(projectId)
                .taskType(taskType)
                .currentStep(currentStep)
                .progress(progress)
                .status("PROCESSING")
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 创建完成消息
     */
    public static ProgressMessage complete(Long projectId, String taskType) {
        return ProgressMessage.builder()
                .projectId(projectId)
                .taskType(taskType)
                .status("COMPLETED")
                .progress(100)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 创建失败消息
     */
    public static ProgressMessage error(Long projectId, String taskType, String errorMessage) {
        return ProgressMessage.builder()
                .projectId(projectId)
                .taskType(taskType)
                .status("FAILED")
                .errorMessage(errorMessage)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
