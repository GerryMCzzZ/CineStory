package com.cinestory.service.llm.impl;

import com.cinestory.service.llm.LlmService;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * LLM 服务实现
 * 支持 OpenAI 兼容的 API（包括 OpenAI、Azure OpenAI、各类国内 LLM）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmServiceImpl implements LlmService {

    private final RestTemplate restTemplate;

    @Value("${llm.provider:openai}")
    private String provider;

    @Value("${llm.api-key:}")
    private String apiKey;

    @Value("${llm.model:gpt-3.5-turbo}")
    private String model;

    @Value("${llm.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    @Value("${llm.enabled:true}")
    private boolean enabled;

    @Value("${llm.timeout:60}")
    private int timeout;

    @Override
    public String enhancePrompt(String basePrompt, String styleTemplate) {
        if (!enabled || apiKey == null || apiKey.isEmpty()) {
            log.debug("LLM service disabled or no API key, using simple combination");
            return simpleCombine(basePrompt, styleTemplate);
        }

        try {
            String instruction = buildEnhanceInstruction(basePrompt, styleTemplate);
            return callLlm(instruction);
        } catch (Exception e) {
            log.warn("Failed to enhance prompt with LLM: {}, using fallback", e.getMessage());
            return simpleCombine(basePrompt, styleTemplate);
        }
    }

    @Override
    public String optimizePrompt(String originalPrompt, String instruction) {
        if (!enabled || apiKey == null || apiKey.isEmpty()) {
            log.debug("LLM service disabled, returning original prompt");
            return originalPrompt;
        }

        try {
            String fullInstruction = String.format(
                    "Optimize this video generation prompt. %s\n\nOriginal prompt: %s\n\nReturn only the optimized prompt.",
                    instruction, originalPrompt
            );
            return callLlm(fullInstruction);
        } catch (Exception e) {
            log.warn("Failed to optimize prompt with LLM: {}", e.getMessage());
            return originalPrompt;
        }
    }

    @Override
    public boolean isAvailable() {
        return enabled && apiKey != null && !apiKey.isEmpty();
    }

    /**
     * 调用 LLM API
     */
    private String callLlm(String userMessage) {
        String url = baseUrl + "/chat/completions";

        // 构建请求体
        ChatRequest request = new ChatRequest();
        request.setModel(model);
        request.setMessages(List.of(new Message("user", userMessage)));
        request.setTemperature(0.7);
        request.setMaxTokens(300);

        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<ChatRequest> entity = new HttpEntity<>(request, headers);

        log.debug("Calling LLM API: {} with model: {}", url, model);

        try {
            // 发送请求
            ResponseEntity<ChatResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    ChatResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String content = response.getBody().getFirstChoiceContent();
                log.debug("LLM response received, length: {}", content != null ? content.length() : 0);
                return content != null ? content.trim() : "";
            } else {
                log.warn("LLM API returned non-OK status: {}", response.getStatusCode());
                return "";
            }
        } catch (Exception e) {
            log.error("Error calling LLM API", e);
            throw e;
        }
    }

    /**
     * 简单组合提示词（降级方案）
     */
    private String simpleCombine(String basePrompt, String styleTemplate) {
        return basePrompt + " " + styleTemplate;
    }

    /**
     * 构建增强指令
     */
    private String buildEnhanceInstruction(String basePrompt, String styleTemplate) {
        return String.format("""
                You are a video generation prompt engineer. Enhance the following prompt for AI video generation.

                Base scene: %s

                Style template: %s

                Requirements:
                1. Create a detailed visual description suitable for video generation
                2. Include camera angles, lighting, and movement
                3. Ensure anime-style aesthetics
                4. Keep the prompt under 500 characters
                5. Return only the enhanced prompt, no explanations

                Enhanced prompt:
                """, basePrompt, styleTemplate);
    }

    /**
     * 聊天请求
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ChatRequest {
        private String model;
        private List<Message> messages;
        private Double temperature;
        private Integer maxTokens;

        public void setTemperature(Double temp) {
            this.temperature = temp;
        }

        public void setMaxTokens(Integer tokens) {
            this.maxTokens = tokens;
        }
    }

    /**
     * 消息
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Message {
        private String role;
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    /**
     * 聊天响应
     */
    @Data
    public static class ChatResponse {
        private String id;
        private String object;
        private Long created;
        private String model;
        private List<Choice> choices;
        private Usage usage;

        public String getFirstChoiceContent() {
            if (choices != null && !choices.isEmpty()) {
                Message message = choices.get(0).getMessage();
                return message != null ? message.getContent() : null;
            }
            return null;
        }
    }

    /**
     * 选择项
     */
    @Data
    public static class Choice {
        private Integer index;
        private Message message;
        @JsonProperty("finish_reason")
        private String finishReason;
    }

    /**
     * 使用情况
     */
    @Data
    public static class Usage {
        @JsonProperty("prompt_tokens")
        private Integer promptTokens;
        @JsonProperty("completion_tokens")
        private Integer completionTokens;
        @JsonProperty("total_tokens")
        private Integer totalTokens;
    }
}
