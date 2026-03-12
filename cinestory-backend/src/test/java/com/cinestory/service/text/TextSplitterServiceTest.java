package com.cinestory.service.text;

import com.cinestory.model.dto.NovelInput;
import com.cinestory.model.dto.SplitConfig;
import com.cinestory.model.entity.SceneType;
import com.cinestory.model.entity.TextSlice;
import com.cinestory.service.text.impl.TextSplitterServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 文本切片服务测试
 */
class TextSplitterServiceTest {

    private TextSplitterService textSplitterService;

    @BeforeEach
    void setUp() {
        textSplitterService = new TextSplitterServiceImpl();
    }

    @Test
    void testSplitByScene() {
        String content = """
                第一章 开始

                "你好，"他说。

                这是一个阳光明媚的早晨。小明走在街上，看着周围的景色。

                "今天天气真好，"小明感叹道。

                突然，天空变暗了。
                """;

        NovelInput input = NovelInput.builder()
                .content(content)
                .title("测试小说")
                .build();

        SplitConfig config = SplitConfig.builder()
                .maxSliceLength(200)
                .minSliceLength(20)
                .build();

        List<TextSlice> slices = textSplitterService.split(input, config);

        assertNotNull(slices);
        assertFalse(slices.isEmpty());
        assertTrue(slices.size() >= 2);
    }

    @Test
    void testDialogueDetection() {
        String dialogueText = """"你好，世界！"他大声说道。""";
        SceneType type = textSplitterService.detectSceneType(dialogueText);

        assertEquals(SceneType.DIALOGUE, type);
    }

    @Test
    void testActionDetection() {
        String actionText = "小明跑得很快，一转眼就不见了。";
        SceneType type = textSplitterService.detectSceneType(actionText);

        assertEquals(SceneType.ACTION, type);
    }

    @Test
    void testDescriptionDetection() {
        String descriptionText = "这里是一个美丽的花园，到处都是鲜花。";
        SceneType type = textSplitterService.detectSceneType(descriptionText);

        assertEquals(SceneType.DESCRIPTION, type);
    }

    @Test
    void testEmptyContent() {
        NovelInput input = NovelInput.builder()
                .content("")
                .title("空内容测试")
                .build();

        SplitConfig config = SplitConfig.builder().build();

        List<TextSlice> slices = textSplitterService.split(input, config);

        assertNotNull(slices);
        assertTrue(slices.isEmpty());
    }

    @Test
    void testDurationEstimation() {
        String shortText = "这是一个短句。";
        int shortDuration = textSplitterService.estimateDuration(shortText);
        assertTrue(shortDuration > 0 && shortDuration <= 5);

        String longText = "这是一个很长的句子。" + "这是一个很长的句子。".repeat(50);
        int longDuration = textSplitterService.estimateDuration(longText);
        assertTrue(longDuration > shortDuration);
    }
}
