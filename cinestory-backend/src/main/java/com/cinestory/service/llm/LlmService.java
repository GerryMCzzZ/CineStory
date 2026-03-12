package com.cinestory.service.llm;

/**
 * LLM 服务接口
 * 用于增强和优化视频生成提示词
 */
public interface LlmService {

    /**
     * 增强提示词
     *
     * @param basePrompt 基础提示词
     * @param styleTemplate 风格模板
     * @return 增强后的提示词
     */
    String enhancePrompt(String basePrompt, String styleTemplate);

    /**
     * 优化提示词（用于重试失败的情况）
     *
     * @param originalPrompt 原始提示词
     * @param instruction 优化指令
     * @return 优化后的提示词
     */
    String optimizePrompt(String originalPrompt, String instruction);

    /**
     * 检查 LLM 服务是否可用
     *
     * @return 是否可用
     */
    boolean isAvailable();
}
