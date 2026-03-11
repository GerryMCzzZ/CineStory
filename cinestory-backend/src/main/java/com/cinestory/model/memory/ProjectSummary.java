package com.cinestory.model.memory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 项目摘要
 * 用于展示项目的整体进度状态
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectSummary {

    /**
     * 已完成功能数量
     */
    private long completedCount;

    /**
     * 进行中任务数量
     */
    private long inProgressCount;

    /**
     * 待办事项数量
     */
    private long pendingCount;

    /**
     * 阻塞问题数量
     */
    private long blockedCount;

    /**
     * 最后更新时间
     */
    private String lastUpdated;

    /**
     * 计算整体进度百分比
     */
    public int getProgressPercentage() {
        long total = completedCount + inProgressCount + pendingCount;
        if (total == 0) {
            return 0;
        }
        return (int) ((completedCount * 100) / total);
    }

    /**
     * 获取进度状态描述
     */
    public String getStatusDescription() {
        if (blockedCount > 0) {
            return "有阻塞问题需要解决";
        } else if (inProgressCount > 0) {
            return "开发进行中";
        } else if (pendingCount > 0) {
            return "待开始新任务";
        } else {
            return "所有任务已完成";
        }
    }
}
