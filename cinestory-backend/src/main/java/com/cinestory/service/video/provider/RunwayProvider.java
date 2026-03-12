package com.cinestory.service.video.provider;

import com.cinestory.model.dto.VideoGenerationRequest;
import com.cinestory.service.video.VideoGenerationProvider;
import com.cinestory.service.video.VideoGenerationResult;
import com.cinestory.service.video.VideoGenerationStatus;
import com.cinestory.service.video.provider.api.RunwayApiRequest;
import com.cinestory.service.video.provider.api.RunwayApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * Runway 视频生成提供商
 *
 * API 文档: https://dev.runwayml.com/reference
 */
@Slf4j
@Component
public class RunwayProvider implements VideoGenerationProvider {

    private static final String GENERATE_ENDPOINT = "/video_generations";

    @Value("${video.generation.providers.runway.api-key:}")
    private String apiKey;

    @Value("${video.generation.providers.runway.base-url:https://api.dev.runwayml.com/v1}")
    private String baseUrl;

    @Value("${video.generation.providers.runway.enabled:true}")
    private boolean enabled;

    @Value("${video.generation.providers.runway.max-retries:3}")
    private int maxRetries;

    @Value("${video.generation.providers.runway.timeout:600000}")
    private int timeoutMs;

    private final RestTemplate restTemplate;

    public RunwayProvider(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String getProviderName() {
        return "runway";
    }

    @Override
    public VideoGenerationResult generate(VideoGenerationRequest request) {
        if (!isAvailable()) {
            log.warn("Runway provider not available - API key not configured");
            return VideoGenerationResult.failure("Runway provider not configured", getProviderName());
        }

        try {
            log.info("Calling Runway API with prompt: {}", request.getPrompt());

            // 构建 API 请求
            RunwayApiRequest apiRequest = buildApiRequest(request);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            headers.set("X-Runway-Version", "2024-09-26");

            HttpEntity<RunwayApiRequest> entity = new HttpEntity<>(apiRequest, headers);

            // 发送请求
            String url = baseUrl + GENERATE_ENDPOINT;
            log.debug("POST {} with request: {}", url, apiRequest);

            ResponseEntity<RunwayApiResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    RunwayApiResponse.class
            );

            RunwayApiResponse body = response.getBody();
            if (body != null && body.getId() != null) {
                log.info("Runway task created successfully: {}", body.getId());

                // 如果立即完成（极少情况），直接返回视频 URL
                if (body.isSuccess() && body.getOutput() != null) {
                    return VideoGenerationResult.builder()
                            .success(true)
                            .taskId(body.getId())
                            .provider(getProviderName())
                            .videoUrl(body.getOutput())
                            .estimatedSeconds(0)
                            .build();
                }

                // 返回任务 ID，需要轮询状态
                return VideoGenerationResult.builder()
                        .success(true)
                        .taskId(body.getId())
                        .provider(getProviderName())
                        .estimatedSeconds(calculateEstimatedTime(apiRequest))
                        .build();
            } else {
                return VideoGenerationResult.failure("Invalid response from Runway API", getProviderName());
            }

        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("Runway API authentication failed: {}", e.getMessage());
            return VideoGenerationResult.failure("Invalid API key", getProviderName());

        } catch (HttpClientErrorException e) {
            log.error("Runway API error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return VideoGenerationResult.failure("API error: " + e.getStatusCode(), getProviderName());

        } catch (ResourceAccessException e) {
            log.error("Runway API connection error", e);
            return VideoGenerationResult.failure("Connection error", getProviderName());

        } catch (Exception e) {
            log.error("Runway API unexpected error", e);
            return VideoGenerationResult.failure(e.getMessage(), getProviderName());
        }
    }

    @Override
    public VideoGenerationStatus checkStatus(String taskId) {
        if (!isAvailable()) {
            return VideoGenerationStatus.builder()
                    .status(VideoGenerationStatus.Status.FAILED)
                    .errorMessage("Provider not available")
                    .build();
        }

        try {
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            headers.set("X-Runway-Version", "2024-09-26");

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            // 发送请求
            String url = baseUrl + GENERATE_ENDPOINT + "/" + taskId;
            log.debug("GET {} to check status", url);

            ResponseEntity<RunwayApiResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    RunwayApiResponse.class
            );

            RunwayApiResponse body = response.getBody();
            if (body != null) {
                return mapResponseToStatus(body);
            } else {
                return VideoGenerationStatus.builder()
                        .status(VideoGenerationStatus.Status.FAILED)
                        .errorMessage("Invalid response")
                        .build();
            }

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Runway task not found: {}", taskId);
            return VideoGenerationStatus.builder()
                    .status(VideoGenerationStatus.Status.FAILED)
                    .errorMessage("Task not found")
                    .build();

        } catch (Exception e) {
            log.error("Runway status check error for task: {}", taskId, e);
            return VideoGenerationStatus.builder()
                    .status(VideoGenerationStatus.Status.FAILED)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    @Override
    public boolean isAvailable() {
        return enabled && apiKey != null && !apiKey.isEmpty();
    }

    @Override
    public int getPriority() {
        return 1; // 高优先级
    }

    /**
     * 构建 Runway API 请求
     */
    private RunwayApiRequest buildApiRequest(VideoGenerationRequest request) {
        // 根据请求参数选择合适的模型
        String model = request.getModel() != null ? request.getModel() : "gen3a_turbo";
        int duration = request.getDuration() != null ? request.getDuration() : 5;
        String ratio = request.getAspectRatio() != null ? request.getAspectRatio() : "16:9";

        // Runway Gen-3 Alpha Turbo 支持 5-18 秒
        if (duration < 5) duration = 5;
        if (duration > 18) duration = 18;

        return RunwayApiRequest.builder()
                .promptText(request.getPrompt())
                .model(model)
                .duration(duration)
                .ratio(ratio)
                .imageUrl(request.getImageUrl())
                .watermark(false)
                .promptEnhancement(true)
                .build();
    }

    /**
     * 映射 API 响应到状态
     */
    private VideoGenerationStatus mapResponseToStatus(RunwayApiResponse response) {
        VideoGenerationStatus.Status status;
        String errorMessage = null;
        String videoUrl = null;
        Integer progress = null;

        switch (response.getStatus()) {
            case "PENDING":
            case "QUEUED":
                status = VideoGenerationStatus.Status.PENDING;
                progress = 0;
                break;
            case "RUNNING":
            case "PROCESSING":
                status = VideoGenerationStatus.Status.PROCESSING;
                progress = response.getProgress() != null ? response.getProgress() : 50;
                break;
            case "SUCCEEDED":
                status = VideoGenerationStatus.Status.COMPLETED;
                videoUrl = response.getOutput();
                progress = 100;
                break;
            case "FAILED":
            case "CANCELLED":
                status = VideoGenerationStatus.Status.FAILED;
                errorMessage = response.getError() != null ? response.getError() : response.getFailureReason();
                break;
            default:
                status = VideoGenerationStatus.Status.PENDING;
                break;
        }

        return VideoGenerationStatus.builder()
                .status(status)
                .videoUrl(videoUrl)
                .errorMessage(errorMessage)
                .progress(progress)
                .build();
    }

    /**
     * 计算预估时间
     */
    private int calculateEstimatedTime(RunwayApiRequest request) {
        // 基础时间 + 时长因子
        int baseTime = 60; // 1 分钟基础时间
        int durationFactor = request.getDuration() * 10; // 每秒 10 秒
        return baseTime + durationFactor;
    }
}
