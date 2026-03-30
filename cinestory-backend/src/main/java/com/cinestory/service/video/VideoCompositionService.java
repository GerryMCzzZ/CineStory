package com.cinestory.service.video;

import com.cinestory.model.entity.VideoGeneration;
import com.cinestory.service.storage.StorageService;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.Data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    @Value("${ffmpeg.path:ffmpeg}")
    private String ffmpegPath;

    @Value("${video.output.path:./storage/output}")
    private String outputBasePath;

    @Value("${video.transition.enabled:false}")
    private boolean transitionEnabled;

    @Value("${video.transition.duration:0.5}")
    private double transitionDuration;

    @Value("${video.transition.type:fade}")
    private String transitionType;

    @Value("${video.normalize.enabled:true}")
    private boolean normalizeVideos;

    @Value("${video.output.width:1920}")
    private int outputWidth;

    @Value("${video.output.height:1080}")
    private int outputHeight;

    @Value("${video.output.fps:24}")
    private int outputFps;

    @Value("${video.output.bitrate:2M}")
    private String outputBitrate;

    /**
     * 视频合成配置
     */
    @Data
    @Builder
    public static class CompositionConfig {
        private boolean addIntro;
        private boolean addOutro;
        private boolean addTransitions;
        private boolean normalize;
        private String introPath;
        private String outroPath;
        private int targetWidth;
        private int targetHeight;
        private int targetFps;
    }

    /**
     * 拼接视频片段
     *
     * @param projectId    项目 ID
     * @param generations  视频生成记录列表
     * @param config       合成配置
     * @return 最终视频 URL
     */
    public String composeVideo(Long projectId, List<VideoGeneration> generations, CompositionConfig config) {
        log.info("Starting video composition for project: {} with {} segments", projectId, generations.size());

        try {
            // 1. 下载所有视频片段
            List<Path> segmentPaths = downloadSegments(generations);

            if (segmentPaths.isEmpty()) {
                throw new IllegalStateException("No video segments available for composition");
            }

            log.info("Successfully downloaded {} video segments", segmentPaths.size());

            // 2. 创建输出目录
            Path outputDir = Paths.get(outputBasePath, projectId.toString());
            Files.createDirectories(outputDir);

            // 3. 视频标准化（如果需要）
            List<Path> processedSegments = segmentPaths;
            if (config.isNormalize() || normalizeVideos) {
                processedSegments = normalizeSegments(segmentPaths, outputDir);
            }

            // 4. 拼接视频
            Path outputPath;
            if (config.isAddTransitions() && transitionEnabled && processedSegments.size() > 1) {
                outputPath = composeWithTransitions(processedSegments, outputDir);
            } else {
                outputPath = composeSegments(processedSegments, outputDir);
            }

            // 5. 添加片头片尾
            if (config.isAddIntro() && config.getIntroPath() != null) {
                outputPath = addIntro(outputPath, config.getIntroPath(), outputDir);
            }
            if (config.isAddOutro() && config.getOutroPath() != null) {
                outputPath = addOutro(outputPath, config.getOutroPath(), outputDir);
            }

            // 6. 上传到存储
            String videoUrl = storageService.uploadVideo(outputPath, projectId.toString());

            log.info("Video composition completed for project: {}, output: {}", projectId, videoUrl);
            return videoUrl;

        } catch (Exception e) {
            log.error("Video composition failed for project: {}", projectId, e);
            throw new RuntimeException("Failed to compose video: " + e.getMessage(), e);
        }
    }

    /**
     * 简化版拼接方法（保持向后兼容）
     */
    public String composeVideo(Long projectId, List<VideoGeneration> generations, boolean addIntroOutro) {
        CompositionConfig config = CompositionConfig.builder()
                .addIntro(addIntroOutro)
                .addOutro(addIntroOutro)
                .addTransitions(false)
                .normalize(true)
                .build();
        return composeVideo(projectId, generations, config);
    }

    /**
     * 下载视频片段
     */
    private List<Path> downloadSegments(List<VideoGeneration> generations) throws IOException {
        Path tempDir = Files.createTempDirectory("segments_" + System.currentTimeMillis());

        return generations.stream()
                .filter(g -> g.getVideoUrl() != null)
                .sorted(Comparator.comparing(VideoGeneration::getId)) // 确保按顺序
                .map(g -> {
                    try {
                        Path segmentPath = tempDir.resolve(g.getId() + ".mp4");
                        storageService.downloadVideo(g.getVideoUrl(), segmentPath);
                        log.debug("Downloaded segment {} to {}", g.getId(), segmentPath);
                        return segmentPath;
                    } catch (Exception e) {
                        log.error("Failed to download segment: {}", g.getId(), e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 标准化视频片段（统一分辨率、编码、帧率）
     */
    private List<Path> normalizeSegments(List<Path> segments, Path outputDir) {
        log.info("Normalizing {} video segments to {}x{} @ {}fps",
                segments.size(), outputWidth, outputHeight, outputFps);

        List<Path> normalizedSegments = new ArrayList<>();
        Path normalizeDir = outputDir.resolve("normalized");

        try {
            Files.createDirectories(normalizeDir);
        } catch (IOException e) {
            log.error("Failed to create normalize directory", e);
            return segments; // 返回原始片段
        }

        for (int i = 0; i < segments.size(); i++) {
            Path input = segments.get(i);
            Path output = normalizeDir.resolve("segment_" + i + ".mp4");

            try {
                normalizeVideo(input, output);
                normalizedSegments.add(output);
            } catch (Exception e) {
                log.warn("Failed to normalize segment {}, using original: {}", i, e.getMessage());
                normalizedSegments.add(input);
            }
        }

        return normalizedSegments;
    }

    /**
     * 标准化单个视频
     */
    private void normalizeVideo(Path input, Path output) throws IOException, InterruptedException {
        List<String> command = Arrays.asList(
                ffmpegPath,
                "-i", input.toString(),
                "-vf", String.format("scale=%d:%d:force_original_aspect_ratio=decrease,pad=%d:%d:(ow-iw)/2:(oh-ih)/2",
                        outputWidth, outputHeight, outputWidth, outputHeight),
                "-r", String.valueOf(outputFps),
                "-c:v", "libx264",
                "-preset", "medium",
                "-b:v", outputBitrate,
                "-c:a", "aac",
                "-b:a", "128k",
                "-movflags", "+faststart",
                "-y",
                output.toString()
        );

        executeFFmpeg(command, "normalize video");
    }

    /**
     * 简单拼接视频片段（使用 concat demuxer）
     */
    private Path composeSegments(List<Path> segments, Path outputDir) throws IOException, InterruptedException {
        if (segments.size() == 1) {
            log.info("Only one segment, copying to output");
            return segments.get(0);
        }

        log.info("Composing {} segments without transitions", segments.size());

        // 创建 concat 列表文件
        Path concatList = createConcatListFile(segments);

        Path outputFile = outputDir.resolve("final_" + UUID.randomUUID() + ".mp4");

        List<String> command = Arrays.asList(
                ffmpegPath,
                "-f", "concat",
                "-safe", "0",
                "-i", concatList.toString(),
                "-c", "copy",
                "-y",
                outputFile.toString()
        );

        executeFFmpeg(command, "compose segments");

        // 清理临时文件
        Files.deleteIfExists(concatList);

        return outputFile;
    }

    /**
     * 带转场效果的拼接（使用 xfade 滤镜）
     */
    private Path composeWithTransitions(List<Path> segments, Path outputDir) throws IOException, InterruptedException {
        if (segments.size() < 2) {
            return composeSegments(segments, outputDir);
        }

        log.info("Composing {} segments with {} transition (duration: {}s)",
                segments.size(), transitionType, transitionDuration);

        Path outputFile = outputDir.resolve("final_" + UUID.randomUUID() + ".mp4");

        // 构建 xfade 滤镜链
        StringBuilder filterChain = new StringBuilder();
        List<String> inputs = new ArrayList<>();

        inputs.add("-i");
        inputs.add(segments.get(0).toString());

        for (int i = 0; i < segments.size() - 1; i++) {
            inputs.add("-i");
            inputs.add(segments.get(i + 1).toString());

            if (i > 0) {
                filterChain.append(";");
            }

            // xfade 滤镜语法
            // 格式: [n:v][n+1:v]xfade=transition=TYPE:duration=D:offset=O[voutN]
            int offset = calculateOffset(i, segments.size());
            filterChain.append(String.format("[%d:v][%d:v]xfade=transition=%s:duration=%.1f:offset=%.1f[v%d]",
                    i, i + 1, transitionType, transitionDuration, offset, i));

            // 音频使用 acrossfade
            filterChain.append(String.format(";[%d:a][%d:a]acrossfade=d=%.1f[a%d]",
                    i, i + 1, transitionDuration, i));
        }

        // 构建输出映射
        StringBuilder outputMap = new StringBuilder();
        for (int i = 0; i < segments.size() - 1; i++) {
            if (i > 0) outputMap.append(";");
            outputMap.append(String.format("[v%d]", i));
        }
        for (int i = 0; i < segments.size() - 1; i++) {
            outputMap.append(String.format("[a%d]", i));
        }

        // 对于超过2个片段，需要更复杂的滤镜链
        // 这里简化处理：先两两合并
        return composeWithTransitionsProgressive(segments, outputDir);
    }

    /**
     * 渐进式添加转场效果（处理多个片段）
     */
    private Path composeWithTransitionsProgressive(List<Path> segments, Path outputDir) throws IOException, InterruptedException {
        Path currentOutput = segments.get(0);
        Path tempDir = outputDir.resolve("temp_transition");
        Files.createDirectories(tempDir);

        for (int i = 1; i < segments.size(); i++) {
            Path nextInput = segments.get(i);
            Path outputPath = tempDir.resolve("merge_" + i + ".mp4");

            mergeWithTransition(currentOutput, nextInput, outputPath);

            // 删除中间文件（除了最后一个）
            if (i > 1 && !currentOutput.equals(segments.get(0))) {
                Files.deleteIfExists(currentOutput);
            }

            currentOutput = outputPath;
        }

        return currentOutput;
    }

    /**
     * 合并两个视频并添加转场
     */
    private void mergeWithTransition(Path video1, Path video2, Path output) throws IOException, InterruptedException {
        // 获取第一个视频的时长
        double duration1 = getVideoDuration(video1);
        double offset = duration1 - transitionDuration;

        if (offset < 0) {
            offset = 0;
        }

        // 构建转场滤镜
        String videoFilter = String.format(
                "[0:v][1:v]xfade=transition=%s:duration=%.1f:offset=%.1f[vout]",
                transitionType, transitionDuration, offset);

        String audioFilter = String.format(
                "[0:a][1:a]acrossfade=d=%.1f[aout]",
                transitionDuration);

        List<String> command = Arrays.asList(
                ffmpegPath,
                "-i", video1.toString(),
                "-i", video2.toString(),
                "-filter_complex", videoFilter + ";" + audioFilter,
                "-map", "[vout]",
                "-map", "[aout]",
                "-c:v", "libx264",
                "-preset", "medium",
                "-c:a", "aac",
                "-b:a", "128k",
                "-y",
                output.toString()
        );

        executeFFmpeg(command, "merge with transition");
    }

    /**
     * 添加片头
     */
    private Path addIntro(Path mainVideo, String introPath, Path outputDir) throws IOException, InterruptedException {
        log.info("Adding intro to video");

        Path introFile = Paths.get(introPath);
        if (!Files.exists(introFile)) {
            log.warn("Intro file not found: {}, skipping", introPath);
            return mainVideo;
        }

        Path outputFile = outputDir.resolve("with_intro_" + UUID.randomUUID() + ".mp4");

        // 创建 concat 列表
        Path concatList = Files.createTempFile("intro_concat_", ".txt");
        Files.writeString(concatList,
                "file '" + introFile.toAbsolutePath() + "'\n" +
                "file '" + mainVideo.toAbsolutePath() + "'\n");

        List<String> command = Arrays.asList(
                ffmpegPath,
                "-f", "concat",
                "-safe", "0",
                "-i", concatList.toString(),
                "-c", "copy",
                "-y",
                outputFile.toString()
        );

        executeFFmpeg(command, "add intro");

        Files.deleteIfExists(concatList);
        return outputFile;
    }

    /**
     * 添加片尾
     */
    private Path addOutro(Path mainVideo, String outroPath, Path outputDir) throws IOException, InterruptedException {
        log.info("Adding outro to video");

        Path outroFile = Paths.get(outroPath);
        if (!Files.exists(outroFile)) {
            log.warn("Outro file not found: {}, skipping", outroPath);
            return mainVideo;
        }

        Path outputFile = outputDir.resolve("with_outro_" + UUID.randomUUID() + ".mp4");

        // 创建 concat 列表
        Path concatList = Files.createTempFile("outro_concat_", ".txt");
        Files.writeString(concatList,
                "file '" + mainVideo.toAbsolutePath() + "'\n" +
                "file '" + outroFile.toAbsolutePath() + "'\n");

        List<String> command = Arrays.asList(
                ffmpegPath,
                "-f", "concat",
                "-safe", "0",
                "-i", concatList.toString(),
                "-c", "copy",
                "-y",
                outputFile.toString()
        );

        executeFFmpeg(command, "add outro");

        Files.deleteIfExists(concatList);
        return outputFile;
    }

    /**
     * 创建 FFmpeg concat 列表文件
     */
    private Path createConcatListFile(List<Path> segments) throws IOException {
        Path listFile = Files.createTempFile("concat_", ".txt");
        StringBuilder content = new StringBuilder();

        for (Path segment : segments) {
            // 转义路径中的特殊字符
            String path = segment.toAbsolutePath().toString().replace("'", "'\\''");
            content.append("file '").append(path).append("'\n");
        }

        Files.writeString(listFile, content);
        return listFile;
    }

    /**
     * 计算转场偏移时间
     */
    private int calculateOffset(int index, int totalSegments) {
        // 简化计算，实际应该基于视频时长
        return index * 5; // 假设每个片段约5秒
    }

    /**
     * 获取视频时长（秒）
     */
    public double getVideoDuration(Path videoPath) {
        try {
            List<String> command = Arrays.asList(
                    ffmpegPath,
                    "-i", videoPath.toString(),
                    "-f", "null",
                    "-"
            );

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);

            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            Pattern durationPattern = Pattern.compile("Duration: (\\d{2}):(\\d{2}):(\\d{2})\\.(\\d{2})");
            Double duration = null;

            while ((line = reader.readLine()) != null) {
                Matcher matcher = durationPattern.matcher(line);
                if (matcher.find()) {
                    int hours = Integer.parseInt(matcher.group(1));
                    int minutes = Integer.parseInt(matcher.group(2));
                    int seconds = Integer.parseInt(matcher.group(3));
                    int centiseconds = Integer.parseInt(matcher.group(4));
                    duration = hours * 3600.0 + minutes * 60.0 + seconds + centiseconds / 100.0;
                    break;
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.warn("FFmpeg exited with code {} when getting duration", exitCode);
            }

            return duration != null ? duration : 5.0; // 默认5秒

        } catch (Exception e) {
            log.error("Failed to get video duration for: {}", videoPath, e);
            return 5.0;
        }
    }

    /**
     * 获取视频信息
     */
    public VideoInfo getVideoInfo(Path videoPath) {
        try {
            List<String> command = Arrays.asList(
                    ffmpegPath,
                    "-i", videoPath.toString(),
                    "-f", "null",
                    "-"
            );

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);

            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            VideoInfo info = new VideoInfo();
            info.path = videoPath.toString();

            Pattern durationPattern = Pattern.compile("Duration: (\\d{2}):(\\d{2}):(\\d{2})\\.(\\d{2})");
            Pattern resolutionPattern = Pattern.compile("(\\d{3,4})x(\\d{3,4})");
            Pattern fpsPattern = Pattern.compile("(\\d{2}) fps");

            while ((line = reader.readLine()) != null) {
                // 解析时长
                Matcher durationMatcher = durationPattern.matcher(line);
                if (durationMatcher.find()) {
                    int hours = Integer.parseInt(durationMatcher.group(1));
                    int minutes = Integer.parseInt(durationMatcher.group(2));
                    int seconds = Integer.parseInt(durationMatcher.group(3));
                    int centiseconds = Integer.parseInt(durationMatcher.group(4));
                    info.duration = hours * 3600.0 + minutes * 60.0 + seconds + centiseconds / 100.0;
                }

                // 解析分辨率
                Matcher resolutionMatcher = resolutionPattern.matcher(line);
                if (resolutionMatcher.find() && info.width == 0) {
                    info.width = Integer.parseInt(resolutionMatcher.group(1));
                    info.height = Integer.parseInt(resolutionMatcher.group(2));
                }

                // 解析帧率
                Matcher fpsMatcher = fpsPattern.matcher(line);
                if (fpsMatcher.find()) {
                    info.fps = Integer.parseInt(fpsMatcher.group(1));
                }
            }

            process.waitFor();
            return info;

        } catch (Exception e) {
            log.error("Failed to get video info for: {}", videoPath, e);
            return new VideoInfo();
        }
    }

    /**
     * 视频信息类
     */
    @Data
    public static class VideoInfo {
        private String path;
        private double duration;
        private int width;
        private int height;
        private int fps;
        private String codec;
    }

    /**
     * 检查 FFmpeg 是否可用
     */
    public boolean isFfmpegAvailable() {
        try {
            List<String> command = Arrays.asList(ffmpegPath, "-version");
            ProcessBuilder pb = new ProcessBuilder(command);
            Process process = pb.start();
            boolean success = process.waitFor(5, TimeUnit.SECONDS);
            return success && process.exitValue() == 0;
        } catch (Exception e) {
            log.error("FFmpeg not available", e);
            return false;
        }
    }

    /**
     * 执行 FFmpeg 命令并处理输出
     */
    private void executeFFmpeg(List<String> command, String operation) throws IOException, InterruptedException {
        log.debug("Executing FFmpeg command for: {}", operation);
        log.trace("Command: {}", String.join(" ", command));

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);

        Process process = pb.start();

        // 读取输出用于日志
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.trace("FFmpeg: {}", line);
            }
        }

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException(
                    String.format("FFmpeg failed for operation '%s' with exit code: %d", operation, exitCode));
        }

        log.debug("FFmpeg operation '{}' completed successfully", operation);
    }

    /**
     * 清理临时文件
     */
    public void cleanupTempFiles(Path tempDir) {
        try {
            if (tempDir != null && Files.exists(tempDir)) {
                Files.walk(tempDir)
                        .sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException e) {
                                log.warn("Failed to delete temp file: {}", path, e);
                            }
                        });
                log.debug("Cleaned up temp directory: {}", tempDir);
            }
        } catch (Exception e) {
            log.error("Failed to cleanup temp files", e);
        }
    }
}
