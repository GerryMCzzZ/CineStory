package com.cinestory.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 视频生成统计响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerationStatsResponse {

    private long total;
    private long pending;
    private long processing;
    private long completed;
    private long failed;
    private long cancelled;

    private long runwayTotal;
    private long runwayCompleted;
    private long pikaTotal;
    private long pikaCompleted;
    private long lumaTotal;
    private long lumaCompleted;
}
