package com.cinestory.model.memory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 审查总结
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSummary {

    /**
     * 总问题数
     */
    private int totalIssues;

    /**
     * 错误数量
     */
    private int errorCount;

    /**
     * 警告数量
     */
    private int warningCount;

    /**
     * 建议数量
     */
    private int suggestionCount;

    /**
     * 信息数量
     */
    private int infoCount;

    /**
     * 成功标记数量
     */
    private int successCount;

    /**
     * 评级 (A/B/C)
     */
    private String grade;

    /**
     * 评估描述
     */
    private String assessment;

    /**
     * 改进建议列表
     */
    private List<String> recommendations;
}
