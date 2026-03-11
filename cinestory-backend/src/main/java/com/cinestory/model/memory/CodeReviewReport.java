package com.cinestory.model.memory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 代码审查报告
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeReviewReport {

    /**
     * 审查日期 (yyyy-MM-dd)
     */
    private String reviewDate;

    /**
     * 审查时间 (yyyy-MM-dd HH:mm)
     */
    private String reviewTime;

    /**
     * 代码质量检查结果
     * Key: 文件名或模块名
     * Value: 问题列表
     */
    private Map<String, List<Issue>> codeQualityResults;

    /**
     * 业务逻辑检查结果
     */
    private Map<String, List<Issue>> businessLogicResults;

    /**
     * 架构合理性检查结果
     */
    private Map<String, List<Issue>> architectureResults;

    /**
     * 审查总结
     */
    private ReviewSummary summary;

    /**
     * 获取问题总数
     */
    public int getTotalIssueCount() {
        int count = 0;
        if (codeQualityResults != null) {
            count += codeQualityResults.values().stream().mapToInt(List::size).sum();
        }
        if (businessLogicResults != null) {
            count += businessLogicResults.values().stream().mapToInt(List::size).sum();
        }
        if (architectureResults != null) {
            count += architectureResults.values().stream().mapToInt(List::size).sum();
        }
        return count;
    }

    /**
     * 按严重程度统计问题数量
     */
    public int getIssueCountBySeverity(Issue.Severity severity) {
        int count = 0;
        count += countBySeverity(codeQualityResults, severity);
        count += countBySeverity(businessLogicResults, severity);
        count += countBySeverity(architectureResults, severity);
        return count;
    }

    private int countBySeverity(Map<String, List<Issue>> results, Issue.Severity severity) {
        if (results == null) {
            return 0;
        }
        return results.values().stream()
                .flatMap(List::stream)
                .filter(issue -> issue.getSeverity() == severity)
                .mapToInt(issue -> 1)
                .sum();
    }
}
