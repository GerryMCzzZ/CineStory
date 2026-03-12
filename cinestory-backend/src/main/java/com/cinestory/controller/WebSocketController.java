package com.cinestory.controller;

import com.cinestory.model.dto.ProgressMessage;
import com.cinestory.service.websocket.ProgressWebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

/**
 * WebSocket 消息控制器
 */
@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final ProgressWebSocketService progressWebSocketService;

    /**
     * 客户端订阅进度更新
     * 订阅地址: /topic/progress/{projectId}
     *
     * 客户端代码示例:
     * stompClient.subscribe('/topic/progress/' + projectId, (message) => {
     *   const progress = JSON.parse(message.body);
     *   console.log('Progress:', progress);
     * });
     */

    /**
     * 处理客户端发送的消息（心跳等）
     */
    @MessageMapping("/ws/ping")
    @SendTo("/topic/pong")
    public String handlePing(String message) {
        return "pong: " + message;
    }
}
