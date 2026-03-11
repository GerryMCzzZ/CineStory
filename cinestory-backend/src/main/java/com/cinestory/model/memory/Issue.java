package com.cinestory.model.memory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 代码审查问题
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Issue {

    /**
     * 严重程度
     */
    private Severity severity;

    /**
     * 问题描述
     */
    private String title;

    /**
     * 详细说明
     */
    private String description;

    public enum Severity {
        ERROR,       // 错误（必须修复）
        WARNING,     // 警告（建议修复）
        SUGGESTION,  // 建议（可选优化）
        INFO,        // 信息（仅供参考）
        SUCCESS      // 成功（做得好的地方）
    }

    /**
     * 创建错误级别问题
     */
    public static Issue error(String title, String description) {
        return new Issue(Severity.ERROR, title, description);
    }

    /**
     * 创建警告级别问题
     */
    public static Issue warning(String title, String description) {
        return new Issue(Severity.WARNING, title, description);
    }

    /**
     * 创建建议级别问题
     */
    public static Issue suggestion(String title, String description) {
        return new Issue(Severity.SUGGESTION, title, description);
    }

    /**
     * 创建信息级别问题
     */
    public static Issue info(String title, String description) {
        return new Issue(Severity.INFO, title, description);
    }

    /**
     * 创建成功标记
     */
    public static Issue success(String title, String description) {
        return new Issue(Severity.SUCCESS, title, description);
    }

    /**
     * 转换为 Markdown 格式
     */
    public String toMarkdown() {
        return String.format("%s **%s**: %s", getIcon(), title, description);
    }

    /**
     * 获取严重程度对应的图标
     */
    public String getIcon() {
        return switch (severity) {
            case ERROR -> "❌";
            case WARNING -> "⚠️ ";
            case SUGGESTION -> "💡";
            case INFO -> "ℹ️ ";
            case SUCCESS -> "✅";
        };
    }
}
