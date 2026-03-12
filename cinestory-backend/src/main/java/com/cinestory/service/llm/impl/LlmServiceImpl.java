package com.cinestory.service.llm.impl;

import com.cinestory.service.llm.LlmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * LLM 服务实现
 * 支持多种 LLM 提供商
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

    @Override
    public String enhancePrompt(String basePrompt, String styleTemplate) {
        if (!enabled || apiKey == null || apiKey.isEmpty()) {
            log.debug("LLM service disabled or no API key, returning base prompt");
            return basePrompt + " " + styleTemplate;
        }

        try {
            String instruction = buildEnhanceInstruction(basePrompt, styleTemplate);
            return callLlm(instruction);
        } catch (Exception e) {
            log.warn("Failed to enhance prompt with LLM, using fallback", e);
            return basePrompt + " " + styleTemplate;
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
            log.warn("Failed to optimize prompt with LLM", e);
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
    private String callLlm(String instruction) {
        String url = baseUrl + "/chat/completions";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);

        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", instruction);

        requestBody.put("messages", new Object[]{message});
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 300);

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + apiKey);
        headers.put("Content-Type", "application/json");

        // 使用 RestTemplate 发送请求
        // 实际实现需要配置 RestTemplate Bean
        log.debug("Calling LLM API: {}", url);

        // 简化实现，返回空字符串
        // 实际使用时需要配置 RestTemplate 和处理响应
        return "";
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
}
