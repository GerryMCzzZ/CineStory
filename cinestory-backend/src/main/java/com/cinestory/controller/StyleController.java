package com.cinestory.controller;

import com.cinestory.model.dto.response.ApiResponse;
import com.cinestory.model.dto.response.PageResponse;
import com.cinestory.model.dto.response.StyleTemplateResponse;
import com.cinestory.model.entity.StyleTemplate;
import com.cinestory.service.StyleTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 风格模板控制器
 */
@Tag(name = "风格模板", description = "视频风格模板的查询操作")
@RestController
@RequestMapping("/api/styles")
@RequiredArgsConstructor
public class StyleController {

    private final StyleTemplateService styleTemplateService;

    @Operation(summary = "获取风格模板列表", description = "分页查询风格模板列表，支持按分类筛选")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<StyleTemplateResponse>>> getStyles(
            @Parameter(description = "页码（从0开始）") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "12") int size,
            @Parameter(description = "风格分类（可选）") @RequestParam(required = false) String category,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "name") String sortBy) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        Page<StyleTemplate> styles = category != null
                ? styleTemplateService.getByCategory(category, pageRequest)
                : styleTemplateService.getAllStyles(pageRequest);

        List<StyleTemplateResponse> responses = styles.getContent().stream()
                .map(StyleTemplateResponse::fromEntity)
                .collect(Collectors.toList());

        PageResponse<StyleTemplateResponse> pageResponse = PageResponse.of(
                responses,
                styles.getTotalElements(),
                styles.getNumber(),
                styles.getSize()
        );

        return ResponseEntity.ok(ApiResponse.page(pageResponse));
    }

    @Operation(summary = "获取系统预设风格", description = "获取系统内置的风格模板列表")
    @GetMapping("/system")
    public ResponseEntity<ApiResponse<List<StyleTemplateResponse>>> getSystemStyles() {
        List<StyleTemplate> styles = styleTemplateService.getSystemStyles();
        List<StyleTemplateResponse> responses = styles.stream()
                .map(StyleTemplateResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @Operation(summary = "获取自定义风格", description = "获取用户自定义的风格模板列表")
    @GetMapping("/custom")
    public ResponseEntity<ApiResponse<List<StyleTemplateResponse>>> getCustomStyles() {
        List<StyleTemplate> styles = styleTemplateService.getCustomStyles();
        List<StyleTemplateResponse> responses = styles.stream()
                .map(StyleTemplateResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @Operation(summary = "获取风格分类", description = "获取所有可用的风格分类列表")
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<String>>> getCategories() {
        List<String> categories = styleTemplateService.getCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @Operation(summary = "获取风格详情", description = "根据ID获取风格模板详细信息")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StyleTemplateResponse>> getStyle(
            @Parameter(description = "风格模板ID", required = true) @PathVariable Long id) {
        StyleTemplate style = styleTemplateService.getById(id);
        StyleTemplateResponse response = StyleTemplateResponse.fromEntity(style);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
