package com.cinestory.service.file;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 小说解析服务测试
 */
class NovelParserServiceTest {

    private NovelParserService novelParserService;

    @BeforeEach
    void setUp() {
        novelParserService = new NovelParserService();
    }

    @Test
    void testParseMetadata() {
        String content = """
                第一章 起点

                这是小说的开始。

                "你好，"他说。

                第二章 旅程

                这是新的章节。
                """;

        NovelParserService.NovelMetadata metadata = novelParserService.parseMetadata(content);

        assertNotNull(metadata);
        assertNotNull(metadata.getTitle());
        assertTrue(metadata.getTotalCharacters() > 0);
        assertTrue(metadata.getTotalWords() > 0);
        assertTrue(metadata.isHasChinese());
    }

    @Test
    void testCountWords() {
        String chineseText = "这是一个测试。";
        String englishText = "This is a test.";
        String mixedText = "这是测试 This is test.";

        assertTrue(novelParserService.parseMetadata(chineseText).getTotalWords() > 0);
        assertTrue(novelParserService.parseMetadata(englishText).getTotalWords() > 0);
        assertTrue(novelParserService.parseMetadata(mixedText).getTotalWords() > 0);
    }

    @Test
    void testCountChapters() {
        String content = """
                第一章 开始
                第二章 发展
                第三章 高潮
                第四章 结局
                """;

        NovelParserService.NovelMetadata metadata = novelParserService.parseMetadata(content);

        assertEquals(4, metadata.getEstimatedChapters());
    }

    @Test
    void testCleanContent() {
        String dirtyContent = "  Line 1  \n\n\n  Line 2  \r\n  Line 3  ";
        String cleaned = novelParserService.cleanContent(dirtyContent);

        assertNotNull(cleaned);
        assertFalse(cleaned.contains("\n\n\n"));
    }

    @Test
    void testEmptyContent() {
        NovelParserService.NovelMetadata metadata = novelParserService.parseMetadata("");

        assertNotNull(metadata);
        assertEquals(0, metadata.getTotalCharacters());
    }

    @Test
    void testEnglishContent() {
        String englishContent = """
                Chapter One

                This is an English novel content.
                It has multiple paragraphs.

                Chapter Two

                This is the second chapter.
                """;

        NovelParserService.NovelMetadata metadata = novelParserService.parseMetadata(englishContent);

        assertNotNull(metadata);
        assertFalse(metadata.isHasChinese());
        assertEquals(2, metadata.getEstimatedChapters());
    }
}
