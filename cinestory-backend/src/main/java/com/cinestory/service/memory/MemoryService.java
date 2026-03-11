package com.cinestory.service.memory;

import com.cinestory.model.memory.ProjectSummary;
import com.cinestory.model.memory.SessionRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * 项目记忆服务
 *
 * 用于记录和查询项目开发过程中的各种信息，实现 AI 长期记忆功能。
 *
 * 记忆内容包括：
 * - 目标记忆：项目总体目标、各阶段目标
 * - 进度追踪：已完成功能、进行中任务、待办事项、阻塞问题
 * - 决策记录：架构决策、技术选型
 * - 代码审查：代码质量评估、业务逻辑检查
 * - 洞察反思：优化建议、潜在问题、经验教训
 * - 会话记录：每次开发会话的摘要
 *
 * @author CineStory AI
 * @since 1.0.0
 */
@Slf4j
@Service
public class MemoryService {

    private static final Path MEMORY_ROOT = Paths.get(".memory");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * 记录已完成的功能
     *
     * @param category 功能分类（如 "基础设施"、"数据模型"）
     * @param featureName 功能名称
     * @param description 功能描述
     * @param filesChanged 相关文件列表
     */
    public void recordCompleted(String category, String featureName, String description, List<String> filesChanged) {
        String entry = String.format("""
            ### %s
            - [x] %s
              %s

            #### 相关文件
            %s
            """,
            category,
            featureName,
            description,
            filesChanged.isEmpty() ? "无" : String.join("\n", filesChanged.stream()
                    .map(f -> "- `" + f + "`")
                    .toList())
        );

        String dateHeader = "\n## " + LocalDateTime.now().format(DATE_FORMAT) + "\n\n";
        appendToProgressLog("completed.md", dateHeader + entry);
        log.debug("记录已完成功能: {}", featureName);
    }

    /**
     * 记录进行中的任务
     *
     * @param taskName 任务名称
     * @param status 任务状态描述
     */
    public void recordInProgress(String taskName, String status) {
        String entry = String.format("- [ ] %s - %s\n", taskName, status);
        appendToProgressLog("in_progress.md", entry);
        log.debug("记录进行中任务: {}", taskName);
    }

    /**
     * 记录待办事项
     *
     * @param priority 优先级 (P0/P1/P2)
     * @param category 分类
     * @param task 任务描述
     */
    public void recordPending(String priority, String category, String task) {
        String entry = String.format("- [ ] **[%s]** %s: %s\n", priority, category, task);
        appendToProgressLog("pending.md", entry);
        log.debug("记录待办事项: {}", task);
    }

    /**
     * 记录阻塞问题
     *
     * @param title 问题标题
     * @param description 问题描述
     * @param severity 严重程度
     */
    public void recordBlocked(String title, String description, String severity) {
        String entry = String.format("""
            ### %s
            **严重程度**: %s
            **描述**: %s
            **状态**: 🔴 阻塞中
            **日期**: %s
            """,
            title,
            severity,
            description,
            LocalDateTime.now().format(DATE_FORMAT)
        );
        appendToProgressLog("blocked.md", entry);
        log.warn("记录阻塞问题: {}", title);
    }

    /**
     * 记录架构决策 (ADR 格式)
     *
     * @param decisionId 决策ID (如 ADR-001)
     * @param title 决策标题
     * @param context 决策上下文
     * @param decision 决策内容
     * @param consequences 决策后果
     */
    public void recordDecision(String decisionId, String title, String context, String decision, String consequences) {
        String content = String.format("""

            ## %s: %s

            **状态**: ✅ 已接受

            **日期**: %s

            **上下文**:
            %s

            **决策**:
            %s

            **理由**:
            %s

            **后果**:
            %s

            ---
            """,
            decisionId,
            title,
            LocalDateTime.now().format(DATE_FORMAT),
            context,
            decision,
            "", // 理由可以单独添加
            consequences
        );

        appendToFile(MEMORY_ROOT.resolve("decisions/architecture.md"), content);
        log.info("记录架构决策: {} - {}", decisionId, title);
    }

    /**
     * 记录代码审查结果
     *
     * @param reviewContent 审查内容（Markdown 格式）
     */
    public void recordCodeReview(String reviewContent) {
        String fileName = "reviews/" + LocalDateTime.now().format(DATE_FORMAT) + "_review.md";
        String header = "# 代码审查报告 - " + LocalDateTime.now().format(DATE_FORMAT) + "\n\n" +
                        "**审查时间**: " + LocalDateTime.now().format(DATETIME_FORMAT) + "\n\n";
        writeToFile(MEMORY_ROOT.resolve(fileName), header + reviewContent);

        // 同时更新最新审查报告
        writeToFile(MEMORY_ROOT.resolve("reviews/latest_review.md"), header + reviewContent);
        log.info("记录代码审查报告: {}", fileName);
    }

    /**
     * 记录洞察和优化建议
     *
     * @param type 类型 (optimization_ideas, potential_issues, lessons_learned)
     * @param content 内容
     */
    public void recordInsight(String type, String content) {
        Path filePath = MEMORY_ROOT.resolve("insights/" + type + ".md");
        String entry = "\n## " + LocalDateTime.now().format(DATE_FORMAT) + "\n" + content + "\n";
        appendToFile(filePath, entry);
        log.debug("记录洞察: {}", type);
    }

    /**
     * 记录会话摘要
     *
     * @param record 会话记录
     */
    public void recordSession(SessionRecord record) {
        String content = String.format("""
            # 开发会话记录 - %s

            ## 会话概述
            **时间**: %s - %s
            **主题**: %s
            **参与者**: %s

            ## 完成的工作
            %s

            ## 遇到的问题
            %s

            ## 决策记录
            %s

            ## 下一步计划
            %s

            ---

            **记录时间**: %s
            """,
            record.getDate(),
            record.getStartTime(),
            record.getEndTime(),
            record.getTopic(),
            record.getParticipant(),
            formatList(record.getCompletedItems()),
            record.getIssues().isEmpty() ? "无" : String.join("\n", record.getIssues()),
            record.getDecisions().isEmpty() ? "无" : String.join("\n", record.getDecisions()),
            String.join("\n", record.getNextSteps()),
            LocalDateTime.now().format(DATETIME_FORMAT)
        );

        String fileName = "sessions/" + record.getDate() + "_session.md";
        writeToFile(MEMORY_ROOT.resolve(fileName), content);
        log.info("记录会话: {} - {}", record.getDate(), record.getTopic());
    }

    /**
     * 获取项目进度摘要
     *
     * @return 项目摘要
     */
    public ProjectSummary getProjectSummary() {
        ProjectSummary summary = new ProjectSummary();
        summary.setCompletedCount(countCompletedItems());
        summary.setInProgressCount(countInProgressItems());
        summary.setPendingCount(countPendingItems());
        summary.setBlockedCount(countBlockedItems());
        summary.setLastUpdated(LocalDateTime.now().format(DATETIME_FORMAT));
        return summary;
    }

    /**
     * 读取指定记忆文件的内容
     *
     * @param fileName 文件名（相对于 .memory 目录）
     * @return 文件内容
     */
    public String readMemory(String fileName) {
        Path path = MEMORY_ROOT.resolve(fileName);
        if (Files.exists(path)) {
            try {
                return Files.readString(path);
            } catch (IOException e) {
                log.error("读取记忆文件失败: {}", fileName, e);
                return "";
            }
        }
        return "";
    }

    /**
     * 获取所有已完成的任务列表
     */
    public List<String> getCompletedItems() {
        Path path = MEMORY_ROOT.resolve("progress/completed.md");
        List<String> items = new ArrayList<>();
        if (Files.exists(path)) {
            try (Stream<String> lines = Files.lines(path)) {
                lines.filter(line -> line.startsWith("- [x]") || line.startsWith("###"))
                     .forEach(items::add);
            } catch (IOException e) {
                log.error("读取已完成任务失败", e);
            }
        }
        return items;
    }

    // ==================== 私有辅助方法 ====================

    private void appendToProgressLog(String fileName, String content) {
        Path filePath = MEMORY_ROOT.resolve("progress/" + fileName);
        appendToFile(filePath, content);
    }

    private void appendToFile(Path path, String content) {
        try {
            Files.createDirectories(path.getParent());
            if (!Files.exists(path)) {
                Files.writeString(path, content, StandardOpenOption.CREATE);
            } else {
                Files.writeString(path, content, StandardOpenOption.APPEND);
            }
        } catch (IOException e) {
            log.error("写入文件失败: {}", path, e);
        }
    }

    private void writeToFile(Path path, String content) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, content, StandardOpenOption.CREATE,
                             StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            log.error("写入文件失败: {}", path, e);
        }
    }

    private String formatList(List<String> items) {
        if (items == null || items.isEmpty()) {
            return "无";
        }
        StringBuilder sb = new StringBuilder();
        for (String item : items) {
            sb.append("- ").append(item).append("\n");
        }
        return sb.toString();
    }

    private long countCompletedItems() {
        return countItemsInFile("progress/completed.md", "- [x]");
    }

    private long countInProgressItems() {
        return countItemsInFile("progress/in_progress.md", "- [");
    }

    private long countPendingItems() {
        return countItemsInFile("progress/pending.md", "- [");
    }

    private long countBlockedItems() {
        Path path = MEMORY_ROOT.resolve("progress/blocked.md");
        if (Files.exists(path)) {
            try (Stream<String> lines = Files.lines(path)) {
                return lines.filter(line -> line.startsWith("###"))
                           .count();
            } catch (IOException e) {
                return 0;
            }
        }
        return 0;
    }

    private long countItemsInFile(String fileName, String prefix) {
        Path path = MEMORY_ROOT.resolve(fileName);
        if (Files.exists(path)) {
            try (Stream<String> lines = Files.lines(path)) {
                return lines.filter(line -> line.startsWith(prefix))
                           .count();
            } catch (IOException e) {
                return 0;
            }
        }
        return 0;
    }
}
