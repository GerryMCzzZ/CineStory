package com.cinestory.model.memory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 会话记录
 * 用于记录每次开发会话的摘要信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionRecord {

    /**
     * 会话日期 (yyyy-MM-dd)
     */
    private String date;

    /**
     * 开始时间 (HH:mm)
     */
    private String startTime;

    /**
     * 结束时间 (HH:mm)
     */
    private String endTime;

    /**
     * 会话主题
     */
    private String topic;

    /**
     * 参与者
     */
    @Builder.Default
    private String participant = "Claude (AI)";

    /**
     * 完成的工作列表
     */
    private List<String> completedItems;

    /**
     * 遇到的问题列表
     */
    private List<String> issues;

    /**
     * 决策记录列表
     */
    private List<String> decisions;

    /**
     * 下一步计划列表
     */
    private List<String> nextSteps;

    /**
     * 创建当前时间的会话记录
     */
    public static SessionRecord createCurrent(String topic) {
        String now = java.time.LocalDateTime.now();
        String date = now.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String time = now.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));

        return SessionRecord.builder()
                .date(date)
                .startTime(time)
                .endTime(time)
                .topic(topic)
                .participant("Claude (AI)")
                .completedItems(new java.util.ArrayList<>())
                .issues(new java.util.ArrayList<>())
                .decisions(new java.util.ArrayList<>())
                .nextSteps(new java.util.ArrayList<>())
                .build();
    }
}
