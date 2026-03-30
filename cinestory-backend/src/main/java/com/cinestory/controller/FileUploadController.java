package com.cinestory.controller;

import com.cinestory.model.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@Tag(name = "文件上传", description = "文件上传、预览和配置查询")
@Slf4j
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileUploadController {

    @Value("${file.upload.path:/tmp/uploads}")
    private String uploadPath;

    @Value("${file.upload.max-size:10485760}") // 10MB
    private long maxFileSize;

    @Operation(summary = "上传小说文件", description = "上传小说文本文件并返回内容")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "上传成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "文件验证失败")
    })
    @PostMapping(value = "/upload-novel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadNovel(
            @Parameter(description = "小说文本文件", required = true)
            @RequestParam("file") MultipartFile file) {

        log.info("Uploading novel file: {}", file.getOriginalFilename());

        // 验证文件
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File is empty"));
        }

        if (file.getSize() > maxFileSize) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File size exceeds limit"));
        }

        String contentType = file.getContentType();
        if (contentType == null ||
                (!contentType.startsWith("text/") && !contentType.equals("application/octet-stream"))) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid file type. Please upload a text file."));
        }

        try {
            // 创建上传目录
            Path uploadDir = Paths.get(uploadPath, "novels");
            Files.createDirectories(uploadDir);

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".txt";
            String filename = UUID.randomUUID() + extension;
            Path filePath = uploadDir.resolve(filename);

            // 保存文件
            file.transferTo(filePath);

            // 读取文件内容
            String content = Files.readString(filePath);

            Map<String, Object> result = new HashMap<>();
            result.put("filename", originalFilename);
            result.put("savedName", filename);
            result.put("path", filePath.toString());
            result.put("size", file.getSize());
            result.put("content", content);
            result.put("contentLength", content.length());

            log.info("Novel file uploaded successfully: {}", filename);
            return ResponseEntity.ok(ApiResponse.success(result));

        } catch (IOException e) {
            log.error("Failed to upload novel file", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to upload file"));
        }
    }

    @Operation(summary = "预览小说内容", description = "上传并预览小说文件内容（仅返回前5000字）")
    @PostMapping(value = "/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, Object>>> previewNovel(
            @Parameter(description = "小说文本文件", required = true)
            @RequestParam("file") MultipartFile file) {

        log.info("Previewing novel file: {}", file.getOriginalFilename());

        try {
            // 读取文件内容
            String content = new String(file.getBytes());

            // 限制预览长度
            int maxPreviewLength = 5000;
            String preview = content.length() > maxPreviewLength
                    ? content.substring(0, maxPreviewLength) + "..."
                    : content;

            // 统计信息
            Map<String, Object> result = new HashMap<>();
            result.put("filename", file.getOriginalFilename());
            result.put("size", file.getSize());
            result.put("contentLength", content.length());
            result.put("preview", preview);
            result.put("truncated", content.length() > maxPreviewLength);

            return ResponseEntity.ok(ApiResponse.success(result));

        } catch (IOException e) {
            log.error("Failed to preview novel file", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to read file"));
        }
    }

    @Operation(summary = "获取上传配置", description = "获取文件上传配置信息")
    @GetMapping("/config")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUploadConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("maxFileSize", maxFileSize);
        config.put("maxFileSizeMB", maxFileSize / (1024 * 1024));
        config.put("allowedTypes", new String[]{"text/plain", "text/html", "application/octet-stream"});
        return ResponseEntity.ok(ApiResponse.success(config));
    }

    @Operation(summary = "上传风格预览图", description = "上传风格模板的预览图片")
    @PostMapping(value = "/upload-style-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadStyleImage(
            @Parameter(description = "图片文件", required = true)
            @RequestParam("file") MultipartFile file) {

        log.info("Uploading style image: {}", file.getOriginalFilename());

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/"))) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid file type. Please upload an image."));
        }

        try {
            // 创建上传目录
            Path uploadDir = Paths.get(uploadPath, "styles");
            Files.createDirectories(uploadDir);

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".png";
            String filename = UUID.randomUUID() + extension;
            Path filePath = uploadDir.resolve(filename);

            // 保存文件
            file.transferTo(filePath);

            Map<String, Object> result = new HashMap<>();
            result.put("filename", originalFilename);
            result.put("savedName", filename);
            result.put("path", "/uploads/styles/" + filename);
            result.put("size", file.getSize());

            log.info("Style image uploaded successfully: {}", filename);
            return ResponseEntity.ok(ApiResponse.success(result));

        } catch (IOException e) {
            log.error("Failed to upload style image", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to upload file"));
        }
    }
}
