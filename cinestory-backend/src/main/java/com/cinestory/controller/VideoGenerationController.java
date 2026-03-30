package com.cinestory.controller;

import com.cinestory.model.dto.response.ApiResponse;
import com.cinestory.model.dto.response.PageResponse;
import com.cinestory.model.dto.response.VideoGenerationResponse;
import com.cinestory.model.entity.VideoGeneration;
import com.cinestory.service.video.VideoGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 视频生成历史控制器
 */
@Slf4j
@RestController
@RequestMapping("/video-generations")
@RequiredArgsConstructor
@Tag(name = "视频生成历史", description = "视频生成历史记录查询")
public class VideoGenerationController {

    private final VideoGenerationService videoGenerationService;

    /**
     * 分页查询视频生成历史
     */
    @GetMapping
    @Operation(summary = "分页查询视频生成历史", description = "支持按状态、提供商筛选")
    public ResponseEntity<ApiResponse<PageResponse<VideoGenerationResponse>>> getHistory(
            @Parameter(description = "页码（从0开始）") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "状态") @RequestParam(required = false) VideoGeneration.GenerationStatus status,
            @Parameter(description = "提供商") @RequestParam(required = false) String provider,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "DESC") Sort.Direction sortDirection
    ) {
        Sort sort = Sort.by(sortDirection, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<VideoGenerationResponse> result = videoGenerationService.getHistory(
                pageable, status, provider
        );

        PageResponse<VideoGenerationResponse> pageResponse = PageResponse.of(
                result.getContent(),
                result.getTotalElements(),
                result.getNumber(),
                result.getSize()
        );

        return ResponseEntity.ok(ApiResponse.success(pageResponse));
    }

    /**
     * 查询生成详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询生成详情", description = "获取单个视频生成记录的详细信息")
    public ResponseEntity<ApiResponse<VideoGenerationResponse>> getById(
            @Parameter(description = "生成记录ID") @PathVariable Long id
    ) {
        VideoGenerationResponse generation = videoGenerationService.getById(id);
        if (generation == null) {
            return ResponseEntity.ok(ApiResponse.error("记录不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(generation));
    }

    /**
     * 重试失败的生成
     */
    @PostMapping("/{id}/retry")
    @Operation(summary = "重试失败的生成", description = "重新尝试失败的视频生成任务")
    public ResponseEntity<ApiResponse<Void>> retry(
            @Parameter(description = "生成记录ID") @PathVariable Long id
    ) {
        videoGenerationService.retryGeneration(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 取消正在处理的生成
     */
    @PostMapping("/{id}/cancel")
    @Operation(summary = "取消生成任务", description = "取消正在处理的视频生成任务")
    public ResponseEntity<ApiResponse<Void>> cancel(
            @Parameter(description = "生成记录ID") @PathVariable Long id
    ) {
        videoGenerationService.cancelGeneration(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 获取生成统计
     */
    @GetMapping("/stats")
    @Operation(summary = "获取生成统计", description = "获取视频生成的统计数据")
    public ResponseEntity<ApiResponse<GenerationStats>> getStats() {
        GenerationStats stats = videoGenerationService.getStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * 生成统计数据
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class GenerationStats {
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
}
