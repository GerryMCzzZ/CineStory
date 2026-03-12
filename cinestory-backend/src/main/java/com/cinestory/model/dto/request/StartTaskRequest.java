package com.cinestory.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 启动任务请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartTaskRequest {

    /**
     * 是否使用预览模式（只生成部分切片）
     */
    private boolean previewMode;

    /**
     * 预览模式下的切片数量
     */
    private int previewSliceCount = 3;

    /**
     * 视频生成提供商（不指定则使用默认）
     */
    private String preferredProvider;

    /**
     * 是否添加字幕
     */
    private boolean addSubtitles = false;

    /**
     * 是否添加片头片尾
     */
    private boolean addIntroOutro = false;
}
