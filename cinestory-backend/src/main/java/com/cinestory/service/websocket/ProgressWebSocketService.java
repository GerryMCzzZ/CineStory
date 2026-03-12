package com.cinestory.service.websocket;

import com.cinestory.model.dto.ProgressMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 进度推送 WebSocket 服务
 */
@Service
public class ProgressWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public ProgressWebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * 发送进度消息到项目主题
     *
     * @param message 进度消息
     */
    public void sendProgress(ProgressMessage message) {
        message.setTimestamp(LocalDateTime.now());
        messagingTemplate.convertAndSend("/topic/progress/" + message.getProjectId(), message);
    }

    /**
     * 发送任务开始消息
     */
    public void sendTaskStarted(Long projectId, String taskType) {
        sendProgress(ProgressMessage.start(projectId, taskType));
    }

    /**
     * 发送进度更新消息
     */
    public void sendProgressUpdate(Long projectId, String taskType, int progress, String currentStep) {
        sendProgress(ProgressMessage.update(projectId, taskType, progress, currentStep));
    }

    /**
     * 发送任务完成消息
     */
    public void sendTaskCompleted(Long projectId, String taskType) {
        sendProgress(ProgressMessage.complete(projectId, taskType));
    }

    /**
     * 发送任务失败消息
     */
    public void sendTaskFailed(Long projectId, String taskType, String errorMessage) {
        sendProgress(ProgressMessage.error(projectId, taskType, errorMessage));
    }

    /**
     * 发送详细进度消息
     */
    public void sendDetailedProgress(Long projectId, String taskType,
                                     int progress, String currentStep,
                                     int total, int processed,
                                     int succeeded, int failed) {
        ProgressMessage message = ProgressMessage.builder()
                .projectId(projectId)
                .taskType(taskType)
                .currentStep(currentStep)
                .progress(progress)
                .total(total)
                .processed(processed)
                .succeeded(succeeded)
                .failed(failed)
                .status("PROCESSING")
                .timestamp(LocalDateTime.now())
                .build();
        sendProgress(message);
    }

    /**
     * 发送用户专属消息（点对点）
     */
    public void sendToUser(String username, String destination, Object payload) {
        messagingTemplate.convertAndSendToUser(username, destination, payload);
    }
}
