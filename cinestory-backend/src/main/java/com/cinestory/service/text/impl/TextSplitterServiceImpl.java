package com.cinestory.service.text.impl;

import com.cinestory.model.dto.NovelInput;
import com.cinestory.model.dto.SplitConfig;
import com.cinestory.model.entity.TextSlice;
import com.cinestory.repository.TextSliceRepository;
import com.cinestory.service.text.TextSplitterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 文本切片服务实现
 * 采用多级切片策略：
 * 1. 按章节分割
 * 2. 按场景边界分割
 * 3. 按句子完整性分割
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TextSplitterServiceImpl implements TextSplitterService {

    private final TextSliceRepository textSliceRepository;

    // 句子结束标记
    private static final Pattern SENTENCE_END = Pattern.compile("[。！？\\.!?]+");

    // 对话模式（中文引号）
    private static final Pattern DIALOGUE_PATTERN = Pattern.compile("「[^」]*」");

    // 场景描写关键词
    private static final String[] DESCRIPTION_KEYWORDS = {
        "只见", "看去", "只见那", "只见得", "放眼望去",
        "周围", "四周", "这里", "那里", "此处",
        "阳光", "月光", "星空", "乌云", "细雨"
    };

    // 动作关键词
    private static final String[] ACTION_KEYWORDS = {
        "拔出", "冲向", "跳上", "飞身", "猛地",
        "迅速", "急忙", "连忙", "立刻", "马上",
        "挥", "抓", "踢", "打", "砍", "刺"
    };

    @Override
    public List<TextSlice> split(NovelInput input, SplitConfig config) {
        log.info("开始切片小说: {}, 内容长度: {}", input.getTitle(), input.getContent().length());

        String content = preprocessContent(input.getContent());
        List<String> chapters = new ArrayList<>();

        // 第一级：按章节分割
        if (config.isSplitByChapter()) {
            chapters = splitByChapters(content, config);
        } else {
            chapters.add(content);
        }

        log.info("章节分割完成，共 {} 章", chapters.size());

        List<TextSlice> allSlices = new ArrayList<>();
        int globalOrder = 0;

        // 处理每一章
        for (int chapterIndex = 0; chapterIndex < chapters.size(); chapterIndex++) {
            String chapter = chapters.get(chapterIndex);
            List<String> sceneSlices;

            // 第二级：按场景或长度分割
            if (config.isDetectSceneChange()) {
                sceneSlices = splitByScenes(chapter, config);
            } else {
                sceneSlices = splitByLength(chapter, config);
            }

            // 第三级：确保切片长度合理
            sceneSlices = refineSlices(sceneSlices, config);

            // 创建 TextSlice 对象
            for (String sliceContent : sceneSlices) {
                if (StringUtils.isBlank(sliceContent)) {
                    continue;
                }

                TextSlice slice = createSliceFromContent(
                    sliceContent,
                    globalOrder++,
                    config
                );
                allSlices.add(slice);
            }

            log.info("第 {} 章处理完成，生成 {} 个切片", chapterIndex + 1, sceneSlices.size());
        }

        log.info("切片完成，共生成 {} 个切片", allSlices.size());
        return allSlices;
    }

    @Override
    public List<TextSlice> split(NovelInput input) {
        return split(input, SplitConfig.builder().build());
    }

    @Override
    @Transactional
    public List<TextSlice> splitAndSave(Long projectId, NovelInput input, SplitConfig config) {
        List<TextSlice> slices = split(input, config);

        // 保存到数据库
        for (TextSlice slice : slices) {
            slice.setProjectId(projectId);
        }

        return textSliceRepository.saveAll(slices);
    }

    @Override
    public TextSlice.SceneType detectSceneType(String content) {
        if (StringUtils.isBlank(content)) {
            return TextSlice.SceneType.NARRATION;
        }

        // 统计对话占比
        Matcher dialogueMatcher = DIALOGUE_PATTERN.matcher(content);
        int dialogueChars = 0;
        while (dialogueMatcher.find()) {
            dialogueChars += dialogueMatcher.group().length();
        }
        double dialogueRatio = (double) dialogueChars / content.length();

        // 对话占比高，判定为对话场景
        if (dialogueRatio > 0.4) {
            return TextSlice.SceneType.DIALOGUE;
        }

        // 检测动作场景
        for (String keyword : ACTION_KEYWORDS) {
            if (content.contains(keyword)) {
                return TextSlice.SceneType.ACTION;
            }
        }

        // 检测描写场景
        for (String keyword : DESCRIPTION_KEYWORDS) {
            if (content.contains(keyword)) {
                return TextSlice.SceneType.DESCRIPTION;
            }
        }

        // 默认为旁白
        return TextSlice.SceneType.NARRATION;
    }

    @Override
    public List<String> extractDialogues(String content) {
        List<String> dialogues = new ArrayList<>();
        Matcher matcher = DIALOGUE_PATTERN.matcher(content);

        while (matcher.find()) {
            String dialogue = matcher.group();
            // 去掉引号
            dialogues.add(dialogue.substring(1, dialogue.length() - 1));
        }

        return dialogues;
    }

    @Override
    public int estimateDuration(int characterCount, TextSlice.SceneType sceneType) {
        // 基础时长：每个字约0.1秒
        int baseDuration = characterCount / 10;

        // 根据场景类型调整
        return switch (sceneType) {
            case DIALOGUE -> Math.max(3, baseDuration + 2);    // 对话需要更多时间
            case ACTION -> Math.max(4, baseDuration + 3);     // 动作场景需要更长
            case DESCRIPTION -> Math.max(3, baseDuration);    // 描写场景适中
            default -> Math.max(3, Math.min(18, baseDuration)); // 默认3-18秒
        };
    }

    /**
     * 预处理内容
     */
    private String preprocessContent(String content) {
        // 统一换行符
        content = content.replace("\r\n", "\n").replace("\r", "\n");

        // 去除多余空白
        content = content.replaceAll("\\n{3,}", "\n\n");

        // 去除首尾空白
        content = content.trim();

        return content;
    }

    /**
     * 按章节分割
     */
    private List<String> splitByChapters(String content, SplitConfig config) {
        List<String> chapters = new ArrayList<>();
        Pattern chapterPattern = Pattern.compile(config.getChapterPattern());

        int lastEnd = 0;
        Matcher matcher = chapterPattern.matcher(content);

        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                String chapter = content.substring(lastEnd, matcher.start()).trim();
                if (!chapter.isEmpty()) {
                    chapters.add(chapter);
                }
            }
            lastEnd = matcher.start();
        }

        // 添加最后一章
        if (lastEnd < content.length()) {
            String lastChapter = content.substring(lastEnd).trim();
            if (!lastChapter.isEmpty()) {
                chapters.add(lastChapter);
            }
        }

        // 如果没有找到章节，返回整篇内容
        if (chapters.isEmpty()) {
            chapters.add(content);
        }

        return chapters;
    }

    /**
     * 按场景转换分割
     */
    private List<String> splitByScenes(String content, SplitConfig config) {
        List<String> scenes = new ArrayList<>();
        String[] keywords = config.getSceneChangeKeywords();

        int lastEnd = 0;
        int pos = 0;

        while (pos < content.length()) {
            boolean foundKeyword = false;

            // 查找场景转换关键词
            for (String keyword : keywords) {
                int keywordPos = content.indexOf(keyword, pos);
                if (keywordPos != -1 && keywordPos > lastEnd + config.getMinSliceLength()) {
                    // 检查是否在句子中间
                    int sentenceEnd = findSentenceEnd(content, keywordPos, 50);
                    if (sentenceEnd != -1) {
                        String scene = content.substring(lastEnd, sentenceEnd).trim();
                        if (scene.length() >= config.getMinSliceLength()) {
                            scenes.add(scene);
                            lastEnd = sentenceEnd;
                            pos = sentenceEnd;
                            foundKeyword = true;
                            break;
                        }
                    }
                }
            }

            if (!foundKeyword) {
                // 没找到场景转换，检查是否超过最大长度
                if (pos - lastEnd >= config.getMaxSliceLength()) {
                    int splitPos = findBestSplitPosition(content, lastEnd, config);
                    if (splitPos > lastEnd) {
                        scenes.add(content.substring(lastEnd, splitPos).trim());
                        lastEnd = splitPos;
                        pos = splitPos;
                    } else {
                        pos++;
                    }
                } else {
                    pos++;
                }
            }
        }

        // 添加最后一个场景
        if (lastEnd < content.length()) {
            scenes.add(content.substring(lastEnd).trim());
        }

        return scenes.stream()
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * 按长度分割
     */
    private List<String> splitByLength(String content, SplitConfig config) {
        List<String> slices = new ArrayList<>();
        int maxLen = config.getMaxSliceLength();

        int start = 0;
        while (start < content.length()) {
            int end = Math.min(start + maxLen, content.length());

            if (end < content.length()) {
                // 尝试在句子结束处分割
                end = findSentenceEnd(content, start, maxLen);
                if (end == -1 || end <= start) {
                    end = start + maxLen;
                }
            }

            String slice = content.substring(start, end).trim();
            if (!slice.isEmpty()) {
                slices.add(slice);
            }

            start = end;
        }

        return slices;
    }

    /**
     * 优化切片，添加上下文重叠
     */
    private List<String> refineSlices(List<String> slices, SplitConfig config) {
        if (slices.size() <= 1 || config.getContextOverlap() <= 0) {
            return slices;
        }

        List<String> refined = new ArrayList<>();
        int overlap = config.getContextOverlap();

        for (int i = 0; i < slices.size(); i++) {
            String current = slices.get(i);

            if (i > 0 && overlap > 0) {
                // 添加前置上下文
                String previous = slices.get(i - 1);
                int contextLen = Math.min(overlap, previous.length());
                String context = previous.substring(previous.length() - contextLen);
                current = "... [前文] " + context + " " + current;
            }

            if (i < slices.size() - 1 && overlap > 0) {
                // 添加后置上下文预览
                String next = slices.get(i + 1);
                int contextLen = Math.min(overlap, next.length());
                String context = next.substring(0, contextLen);
                current = current + " ... [下文预览] " + context + " ...";
            }

            refined.add(current.trim());
        }

        return refined;
    }

    /**
     * 查找最佳分割位置
     */
    private int findBestSplitPosition(String content, int start, SplitConfig config) {
        int searchEnd = Math.min(start + config.getMaxSliceLength(), content.length());
        int searchRange = searchEnd - start;

        // 优先在句子结束处分割
        int sentenceEnd = findSentenceEnd(content, start, searchRange);
        if (sentenceEnd != -1 && sentenceEnd > start + config.getMinSliceLength()) {
            return sentenceEnd;
        }

        // 次选：在对话结束处分割
        if (config.isPreserveDialogue()) {
            int dialogueEnd = findDialogueEnd(content, start, searchRange);
            if (dialogueEnd != -1 && dialogueEnd > start + config.getMinSliceLength()) {
                return dialogueEnd;
            }
        }

        // 兜底：在最大长度处分割
        return searchEnd;
    }

    /**
     * 查找句子结束位置
     */
    private int findSentenceEnd(String content, int start, int maxOffset) {
        int searchEnd = Math.min(start + maxOffset, content.length());
        String substring = content.substring(start, searchEnd);

        Matcher matcher = SENTENCE_END.matcher(substring);
        int lastEnd = -1;
        while (matcher.find()) {
            lastEnd = matcher.end();
        }

        if (lastEnd != -1) {
            return start + lastEnd;
        }
        return -1;
    }

    /**
     * 查找对话结束位置
     */
    private int findDialogueEnd(String content, int start, int maxOffset) {
        int searchEnd = Math.min(start + maxOffset, content.length());
        for (int i = start; i < searchEnd; i++) {
            if (content.charAt(i) == '」' || content.charAt(i) == '"') {
                return i + 1;
            }
        }
        return -1;
    }

    /**
     * 从内容创建切片对象
     */
    private TextSlice createSliceFromContent(String content, int order, SplitConfig config) {
        TextSlice.SceneType sceneType = detectSceneType(content);
        List<String> dialogues = extractDialogues(content);
        int duration = estimateDuration(content.length(), sceneType);

        return TextSlice.builder()
                .content(content)
                .orderIndex(order)
                .sceneType(sceneType)
                .characters(dialogues.isEmpty() ? null : String.join(",", dialogues).substring(0, Math.min(255, dialogues.toString().length())))
                .characterCount(content.length())
                .estimatedDuration(duration)
                .build();
    }
}
