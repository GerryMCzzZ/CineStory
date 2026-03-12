package com.cinestory.service.prompt;

import com.cinestory.model.entity.SceneType;
import com.cinestory.model.entity.TextSlice;
import com.cinestory.service.prompt.impl.PromptGenerationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 提示词生成服务测试
 */
@ExtendWith(MockitoExtension.class)
class PromptGenerationServiceTest {

    @Mock
    private com.cinestory.service.llm.LlmService llmService;

    private PromptGenerationService promptGenerationService;

    @BeforeEach
    void setUp() {
        // 使用真实的实现进行测试
        promptGenerationService = new PromptGenerationServiceImpl(llmService);
    }

    @Test
    void testGeneratePromptForDialogue() {
        TextSlice slice = new TextSlice();
        slice.setId(1L);
        slice.setSceneType(SceneType.DIALOGUE);
        slice.setContent("\"你好，世界！\"他大声说道。");

        String styleTemplate = "Anime style, vibrant colors";
        String negativePrompt = "blurry, low quality";

        var prompt = promptGenerationService.generatePrompt(slice, styleTemplate, negativePrompt);

        assertNotNull(prompt);
        assertNotNull(prompt.getPrompt());
        assertTrue(prompt.getPrompt().length() > 0);
        assertEquals(negativePrompt, prompt.getNegativePrompt());
        assertEquals("DIALOGUE", prompt.getSceneType());
    }

    @Test
    void testGeneratePromptForAction() {
        TextSlice slice = new TextSlice();
        slice.setId(1L);
        slice.setSceneType(SceneType.ACTION);
        slice.setContent("小明飞快地跑过街道，躲避着追赶。");

        String styleTemplate = "Dynamic anime style";
        String negativePrompt = "";

        var prompt = promptGenerationService.generatePrompt(slice, styleTemplate, negativePrompt);

        assertNotNull(prompt);
        assertTrue(prompt.getPrompt().contains("Action"));
    }

    @Test
    void testPromptLength() {
        TextSlice slice = new TextSlice();
        slice.setId(1L);
        slice.setSceneType(SceneType.DESCRIPTION);
        // 很长的内容
        slice.setContent("这是一个很长的描述。".repeat(100));

        String styleTemplate = "Anime style";
        String negativePrompt = "bad quality";

        var prompt = promptGenerationService.generatePrompt(slice, styleTemplate, negativePrompt);

        // 提示词应该被截断
        assertNotNull(prompt);
        assertTrue(prompt.getPrompt().length() <= 500);
    }

    @Test
    void testPromptWithContext() {
        TextSlice slice = new TextSlice();
        slice.setId(1L);
        slice.setSceneType(SceneType.DIALOGUE);
        slice.setContent("\"我明白了，\"她说。");
        slice.setContextBefore("经过长时间的思考");

        var prompt = promptGenerationService.generatePrompt(slice, "Anime style", "");

        assertNotNull(prompt);
        assertTrue(prompt.getPrompt().contains("Previous"));
    }
}
