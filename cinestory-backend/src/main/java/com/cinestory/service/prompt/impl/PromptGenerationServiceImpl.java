package com.cinestory.service.prompt.impl;

import com.cinestory.model.entity.TextSlice;
import com.cinestory.model.entity.VideoPrompt;
import com.cinestory.service.llm.LlmService;
import com.cinestory.service.prompt.PromptGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 提示词生成服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromptGenerationServiceImpl implements PromptGenerationService {

    private final LlmService llmService;

    @Value("${video.prompt.max-length:500}")
    private int maxPromptLength;

    @Override
    public VideoPrompt generatePrompt(TextSlice textSlice, String stylePromptTemplate, String negativePrompt) {
        log.debug("Generating prompt for slice: {}", textSlice.getId());

        String basePrompt = buildBasePrompt(textSlice);
        String enhancedPrompt = enhancePrompt(basePrompt, stylePromptTemplate);

        // 确保提示词不超过最大长度
        String finalPrompt = truncatePrompt(enhancedPrompt);

        return VideoPrompt.builder()
                .sliceId(textSlice.getId())
                .visualPrompt(finalPrompt)
                .motionPrompt("")
                .build();
    }

    @Override
    public List<VideoPrompt> generatePrompts(List<TextSlice> textSlices, String stylePromptTemplate, String negativePrompt) {
        return textSlices.stream()
                .map(slice -> generatePrompt(slice, stylePromptTemplate, negativePrompt))
                .collect(Collectors.toList());
    }

    @Override
    public String optimizePrompt(String originalPrompt, String errorMessage) {
        log.debug("Optimizing prompt due to error: {}", errorMessage);

        String optimizationInstruction = buildOptimizationInstruction(errorMessage);
        return llmService.optimizePrompt(originalPrompt, optimizationInstruction);
    }

    /**
     * 构建基础提示词
     */
    private String buildBasePrompt(TextSlice textSlice) {
        StringBuilder prompt = new StringBuilder();

        // 添加场景类型描述
        prompt.append(getSceneTypeDescription(textSlice.getSceneType())).append(". ");

        // 添加文本内容
        prompt.append("Scene: ").append(textSlice.getContent()).append(". ");

        // 添加上下文信息
        if (textSlice.getContextBefore() != null && !textSlice.getContextBefore().isEmpty()) {
            prompt.append("Previous: ").append(textSlice.getContextBefore()).append(". ");
        }

        return prompt.toString();
    }

    /**
     * 使用 LLM 增强提示词
     */
    private String enhancePrompt(String basePrompt, String styleTemplate) {
        String enhanced = llmService.enhancePrompt(basePrompt, styleTemplate);
        return enhanced != null ? enhanced : basePrompt;
    }

    /**
     * 截断提示词到最大长度
     */
    private String truncatePrompt(String prompt) {
        if (prompt.length() <= maxPromptLength) {
            return prompt;
        }
        return prompt.substring(0, maxPromptLength - 3) + "...";
    }

    /**
     * 获取场景类型描述
     */
    private String getSceneTypeDescription(TextSlice.SceneType sceneType) {
        return switch (sceneType) {
            case DIALOGUE -> "Character dialogue scene, focus on facial expressions and lip sync";
            case DESCRIPTION -> "Descriptive scene, detailed environment visualization";
            case ACTION -> "Action scene, dynamic camera movements and motion";
            case TRANSITION -> "Transition scene, smooth flow between scenes";
            case MONOLOGUE -> "Monologue scene, character internal thoughts";
            case NARRATION -> "Narration scene, storytelling visual style";
        };
    }

    /**
     * 构建优化指令
     */
    private String buildOptimizationInstruction(String errorMessage) {
        if (errorMessage.contains("violence") || errorMessage.contains("inappropriate")) {
            return "Remove any violent or inappropriate content. Keep the scene safe and suitable for general audiences.";
        }
        if (errorMessage.contains("unclear") || errorMessage.contains("ambiguous")) {
            return "Make the prompt more specific and clear. Add more visual details.";
        }
        if (errorMessage.contains("too long")) {
            return "Shorten the prompt while keeping the essential visual elements.";
        }
        return "Improve the prompt to better describe a visual scene for video generation.";
    }
}
