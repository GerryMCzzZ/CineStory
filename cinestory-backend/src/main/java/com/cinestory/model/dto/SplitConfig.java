package com.cinestory.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文本切片配置DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SplitConfig {

    /**
     * 最大切片长度（字符数）
     */
    @Builder.Default
    private int maxSliceLength = 500;

    /**
     * 最小切片长度（字符数）
     */
    @Builder.Default
    private int minSliceLength = 50;

    /**
     * 上下文重叠字符数
     * 用于保持切片间的连贯性
     */
    @Builder.Default
    private int contextOverlap = 100;

    /**
     * 是否保持对话完整性
     * true: 对话不会被切分到两个切片
     */
    @Builder.Default
    private boolean preserveDialogue = true;

    /**
     * 是否按章节分割
     * true: 优先识别章节标题进行分割
     */
    @Builder.Default
    private boolean splitByChapter = true;

    /**
     * 是否识别场景转换
     * true: 在场景变化处分割
     */
    @Builder.Default
    private boolean detectSceneChange = true;

    /**
     * 章节识别正则表达式
     * 默认匹配 "第X章"、"第X回" 等格式
     */
    @Builder.Default
    private String chapterPattern = "第[一二三四五六七八九十百千万0-9]+[章节回卷集]";

    /**
     * 对话开始标记
     */
    @Builder.Default
    private String dialogueStart = "「";

    /**
     * 对话结束标记
     */
    @Builder.Default
    private String dialogueEnd = "」";

    /**
     * 场景转换关键词
     */
    @Builder.Default
    private String[] sceneChangeKeywords = {
        "此时", "这时", "突然", "另一边", "与此同时",
        "次日", "第二天", "几天后", "转眼",
        "就在这时", "却说", "且说", "话说"
    };
}
