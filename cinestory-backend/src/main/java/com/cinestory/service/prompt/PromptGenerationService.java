package com.cinestory.service.prompt;

import com.cinestory.model.entity.TextSlice;
import com.cinestory.model.entity.VideoPrompt;

import java.util.List;

/**
 * 提示词生成服务接口
 */
public interface PromptGenerationService {

    /**
     * 为文本切片生成视频提示词
     *
     * @param textSlice 文本切片
     * @param stylePromptTemplate 风格提示词模板
     * @param negativePrompt 负面提示词
     * @return 视频生成提示词
     */
    VideoPrompt generatePrompt(TextSlice textSlice, String stylePromptTemplate, String negativePrompt);

    /**
     * 批量生成提示词
     *
     * @param textSlices 文本切片列表
     * @param stylePromptTemplate 风格提示词模板
     * @param negativePrompt 负面提示词
     * @return 视频生成提示词列表
     */
    List<VideoPrompt> generatePrompts(List<TextSlice> textSlices, String stylePromptTemplate, String negativePrompt);

    /**
     * 优化提示词（用于重试）
     *
     * @param originalPrompt 原始提示词
     * @param errorMessage 错误信息
     * @return 优化后的提示词
     */
    String optimizePrompt(String originalPrompt, String errorMessage);
}
