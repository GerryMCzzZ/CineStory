package com.cinestory.service.video;

import com.cinestory.model.entity.VideoGeneration;
import com.cinestory.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 视频拼接服务
 * 使用 FFmpeg 进行视频处理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VideoCompositionService {

    private final StorageService storageService;

    @Value("${video.ffmpeg.path:ffmpeg}")
    private String ffmpegPath;

    @Value("${.video.output.path:/tmp/videos}")
    private String outputbasePath;

    @Value("${video.transition.enabled:true}")
    private boolean transitionEnabled;

    @Value("${video.transition.duration:0.5}")
    private double transitionDuration;

    /**
     * 拼接视频片段
     *
     * @param projectId 项目 ID
     * @param generations 视频生成记录列表
     * @param addIntroOutro 是否添加片头片尾
     * @return 最终视频 URL
     */
    public String composeVideo(Long projectId, List<VideoGeneration> generations, boolean addIntroOutro) {
        log.info("Starting video composition for project: {}", projectId);

        try {
            // 1. 下载所有视频片段
            List<Path> segmentPaths = downloadSegments(generations);

            if (segmentPaths.isEmpty()) {
                throw new IllegalStateException("No video segments available for composition");
            }

            // 2. 创建输出目录
            Path outputDir = Paths.get(outputbasePath, projectId.toString());
            Files.createDirectories(outputDir);

            // 3. 拼接视频
            Path outputPath = composeSegments(segmentPaths, outputDir, addIntroOutro);

            // 4. 上传到存储
            String videoUrl = storageService.uploadVideo(outputPath, projectId.toString());

            log.info("Video composition completed for project: {}", projectId);
            return videoUrl;

        } catch (Exception e) {
            log.error("Video composition failed for project: {}", projectId, e);
            throw new RuntimeException("Failed to compose video", e);
        }
    }

    /**
     * 下载视频片段
     */
    private List<Path> downloadSegments(List<VideoGeneration> generations) throws IOException {
        Path tempDir = Files.createTempDirectory("segments_" + System.currentTimeMillis());

        return generations.stream()
                .filter(g -> g.getVideoUrl() != null)
                .map(g -> {
                    try {
                        Path segmentPath = tempDir.resolve(g.getId() + ".mp4");
                        storageService.downloadVideo(g.getVideoUrl(), segmentPath);
                        return segmentPath;
                    } catch (Exception e) {
                        log.error("Failed to download segment: {}", g.getId(), e);
                        return null;
                    }
                })
                .filter(path -> path != null)
                .collect(Collectors.toList());
    }

    /**
     * 拼接视频片段
     */
    private Path composeSegments(List<Path> segments, Path outputDir, boolean addIntroOutro) throws IOException, InterruptedException {
        String outputFile = outputDir.resolve("final_" + UUID.randomUUID() + ".mp4").toString();

        if (segments.size() == 1) {
            // 单个片段，直接复制
            Files.copy(segments.get(0), Path.of(outputFile));
            return Path.of(outputFile);
        }

        // 创建 FFmpeg concat 列表文件
        Path concatList = createConcatListFile(segments);

        // 构建 FFmpeg 命令
        ProcessBuilder pb = new ProcessBuilder(
                ffmpegPath,
                "-f", "concat",
                "-safe", "0",
                "-i", concatList.toString(),
                "-c", "copy",
                "-y",
                outputFile
        );

        pb.redirectErrorStream(true);

        log.debug("FFmpeg command: {}", String.join(" ", pb.command()));

            Process process = pb.start();

        // 读取输出
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.debug("FFmpeg: {}", line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg failed with exit code: " + exitCode);
        }

        // 添加片头片尾
        if (addIntroOutro) {
            return addIntroOutro(Path.of(outputFile), outputDir);
        }

        return Path.of(outputFile);
    }

    /**
     * 创建 FFmpeg concat 列表文件
     */
    private Path createConcatListFile(List<Path> segments) throws IOException {
        Path listFile = Files.createTempFile("concat_", ".txt");
        StringBuilder content = new StringBuilder();

        for (Path segment : segments) {
            content.append("file '").append(segment.toAbsolutePath()).append("'\n");
        }

        Files.writeString(listFile, content);
        return listFile;
    }

    /**
     * 添加片头片尾
     */
    private Path addIntroOutro(Path videoPath, Path outputDir) throws IOException, InterruptedException {
        // 简化实现，直接返回原视频
        // 实际实现需要使用 FFmpeg 添加片头片尾视频
        log.debug("Adding intro/outro to video: {}", videoPath);
        return videoPath;
    }

    /**
     * 添加过渡效果
     */
    private Path addTransitions(List<Path> segments, Path outputDir) throws IOException, InterruptedException {
        // 简化实现，直接使用简单拼接
        // 实际实现需要使用 FFmpeg 滤镜添加过渡效果
        log.debug("Adding transitions between segments");
        return composeSegments(segments, outputDir, false);
    }

    /**
     * 获取视频时长
     */
    public double getVideoDuration(Path videoPath) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                ffmpegPath,
                "-i", videoPath.toString(),
                "-f", "null",
                "-"
        );

        pb.redirectErrorStream(true);

        Process process = pb.start();
        int exitCode = process.waitFor();

        // 解析输出获取时长
        // 简化实现，返回默认值
        return 5.0;
    }

    /**
     * 检查 FFmpeg 是否可用
     */
    public boolean isFfmpegAvailable() {
        try {
            ProcessBuilder pb = new ProcessBuilder(ffmpegPath, "-version");
            Process process = pb.start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            log.error("FFmpeg not available", e);
            return false;
        }
    }
}
