package com.cinestory.service.file;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 小说文本解析服务
 */
@Service
public class NovelParserService {

    /**
     * 解析小说文件，提取元数据
     */
    public NovelMetadata parseMetadata(MultipartFile file) throws IOException {
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        return parseMetadata(content);
    }

    /**
     * 从文本内容解析小说元数据
     */
    public NovelMetadata parseMetadata(String content) {
        NovelMetadata metadata = new NovelMetadata();

        // 尝试提取标题（第一章 或 文件开头的标题）
        String titlePattern = "^(第[一二三四五六七八九十百千0-9]+章|\\s*标题\\s*[:：]\\s*[^\\n]+)";
        Pattern pattern = Pattern.compile(titlePattern, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            metadata.setTitle(matcher.group(1).trim());
        } else {
            // 使用第一行作为标题
            String firstLine = content.split("\\n")[0].trim();
            if (firstLine.length() > 0 && firstLine.length() < 100) {
                metadata.setTitle(firstLine);
            }
        }

        // 统计信息
        metadata.setTotalCharacters(content.length());
        metadata.setTotalWords(countWords(content));
        metadata.setEstimatedChapters(countChapters(content));

        // 检测编码类型（简单判断）
        metadata.setHasChinese(content.matches(".*[\\u4e00-\\u9fa5]+.*"));

        return metadata;
    }

    /**
     * 统计字数
     */
    private int countWords(String content) {
        // 中文字符 + 英文单词
        int chineseCount = content.replaceAll("[^\\u4e00-\\u9fa5]", "").length();
        int englishWords = content.replaceAll("[\\u4e00-\\u9fa5]", " ").split("\\s+").length;
        return chineseCount + englishWords;
    }

    /**
     * 估算章节数
     */
    private int countChapters(String content) {
        Pattern chapterPattern = Pattern.compile("第[\\s\\u4e00-\\u9fa50-9]+章");
        Matcher matcher = chapterPattern.matcher(content);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    /**
     * 清理文本内容
     */
    public String cleanContent(String content) {
        // 移除 BOM 标记
        content = content.replace("\uFEFF", "");

        // 统一换行符
        content = content.replaceAll("\\r\\n?", "\\n");

        // 移除多余空行
        content = content.replaceAll("\\n{3,}", "\\n\\n");

        // 移除行首行尾空白
        String[] lines = content.split("\\n");
        StringBuilder cleaned = new StringBuilder();
        for (String line : lines) {
            cleaned.append(line.trim()).append("\\n");
        }

        return cleaned.toString().trim();
    }

    /**
     * 小说元数据
     */
    public static class NovelMetadata {
        private String title;
        private String author;
        private int totalCharacters;
        private int totalWords;
        private int estimatedChapters;
        private boolean hasChinese;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public int getTotalCharacters() {
            return totalCharacters;
        }

        public void setTotalCharacters(int totalCharacters) {
            this.totalCharacters = totalCharacters;
        }

        public int getTotalWords() {
            return totalWords;
        }

        public void setTotalWords(int totalWords) {
            this.totalWords = totalWords;
        }

        public int getEstimatedChapters() {
            return estimatedChapters;
        }

        public void setEstimatedChapters(int estimatedChapters) {
            this.estimatedChapters = estimatedChapters;
        }

        public boolean isHasChinese() {
            return hasChinese;
        }

        public void setHasChinese(boolean hasChinese) {
            this.hasChinese = hasChinese;
        }
    }
}
