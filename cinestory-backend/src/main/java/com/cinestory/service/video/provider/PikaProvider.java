package com.cinestory.service.video.provider;

import com.cinestory.model.dto.VideoGenerationRequest;
import com.cinestory.service.video.VideoGenerationProvider;
import com.cinestory.service.video.VideoGenerationResult;
import com.cinestory.service.video.VideoGenerationStatus;
import com.cinestory.service.video.provider.api.PikaApiRequest;
import com.cinestory.service.video.provider.api.PikaApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * Pika 视频生成提供商
 *
 * API 文档: https://docs.pika.art
 */
@Slf4j
@Component
public class PikaProvider implements VideoGenerationProvider {

    private static final String GENERATE_ENDPOINT = "/generations";
    private static final String STATUS_ENDPOINT = "/generations/";

    @Value("${video.generation.providers.pika.api-key:}")
    private String apiKey;

    @Value("${video.generation.providers.pika.base-url:https://api.pika.art/v1}")
    private String baseUrl;

    @Value("${video.generation.providers.pika.enabled:true}")
    private boolean enabled;

    @Value("${video.generation.providers.pika.max-retries:3}")
    private int maxRetries;

    @Value("${video.generation.providers.pika.timeout:300000}")
    private int timeoutMs;

    private final RestTemplate restTemplate;

    public PikaProvider(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String getProviderName() {
        return "pika";
    }

    @Override
    public VideoGenerationResult generate(VideoGenerationRequest request) {
        if (!isAvailable()) {
            log.warn("Pika provider not available - API key not configured");
            return VideoGenerationResult.failure("Pika provider not configured", getProviderName());
        }

        try {
            log.info("Calling Pika API with prompt: {}", request.getPrompt());

            // 构建 API 请求
            PikaApiRequest apiRequest = buildApiRequest(request);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            headers.set("X-Pika-Version", "2024-01-01");

            HttpEntity<PikaApiRequest> entity = new HttpEntity<>(apiRequest, headers);

            // 发送请求
            String url = baseUrl + GENERATE_ENDPOINT;
            log.debug("POST {} with request: {}", url, apiRequest);

            ResponseEntity<PikaApiResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    PikaApiResponse.class
            );

            PikaApiResponse body = response.getBody();
            if (body != null && body.getId() != null) {
                log.info("Pika task created successfully: {}", body.getId());

                // 如果立即完成，直接返回视频 URL
                if (body.isSuccess() && body.getFirstVideoUrl() != null) {
                    return VideoGenerationResult.builder()
                            .success(true)
                            .taskId(body.getId())
                            .provider(getProviderName())
                            .videoUrl(body.getFirstVideoUrl())
                            .estimatedSeconds(0)
                            .build();
                }

                // 返回任务 ID，需要轮询状态
                return VideoGenerationResult.builder()
                        .success(true)
                        .taskId(body.getId())
                        .provider(getProviderName())
                        .estimatedSeconds(calculateEstimatedTime(body))
                        .build();
            } else {
                return VideoGenerationResult.failure("Invalid response from Pika API", getProviderName());
            }

        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("Pika API authentication failed: {}", e.getMessage());
            return VideoGenerationResult.failure("Invalid API key", getProviderName());

        } catch (HttpClientErrorException e) {
            log.error("Pika API error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return VideoGenerationResult.failure("API error: " + e.getStatusCode(), getProviderName());

        } catch (ResourceAccessException e) {
            log.error("Pika API connection error", e);
            return VideoGenerationResult.failure("Connection error", getProviderName());

        } catch (Exception e) {
            log.error("Pika API unexpected error", e);
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

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            // 发送请求
            String url = baseUrl + STATUS_ENDPOINT + taskId;
            log.debug("GET {} to check status", url);

            ResponseEntity<PikaApiResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    PikaApiResponse.class
            );

            PikaApiResponse body = response.getBody();
            if (body != null) {
                return mapResponseToStatus(body);
            } else {
                return VideoGenerationStatus.builder()
                        .status(VideoGenerationStatus.Status.FAILED)
                        .errorMessage("Invalid response")
                        .build();
            }

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Pika task not found: {}", taskId);
            return VideoGenerationStatus.builder()
                    .status(VideoGenerationStatus.Status.FAILED)
                    .errorMessage("Task not found")
                    .build();

        } catch (Exception e) {
            log.error("Pika status check error for task: {}", taskId, e);
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
        return 2; // 中优先级
    }

    /**
     * 构建 Pika API 请求
     */
    private PikaApiRequest buildApiRequest(VideoGenerationRequest request) {
        // Pika 支持 3-4 秒视频
        int duration = 4; // 默认 4 秒
        String aspectRatio = request.getAspectRatio() != null ? request.getAspectRatio() : "16:9";

        // 标准化宽高比格式
        if (!aspectRatio.contains(":")) {
            aspectRatio = "16:9";
        }

        return PikaApiRequest.builder()
                .prompt(request.getPrompt())
                .model("pika-1.0")
                .duration(duration)
                .aspectRatio(aspectRatio)
                .frameRate(24)
                .imageUrl(request.getImageUrl())
                .negativePrompt(request.getNegativePrompt())
                .build();
    }

    /**
     * 映射 API 响应到状态
     */
    private VideoGenerationStatus mapResponseToStatus(PikaApiResponse response) {
        VideoGenerationStatus.Status status;
        String errorMessage = null;
        String videoUrl = null;
        Integer progress = null;

        if (response.isProcessing()) {
            status = VideoGenerationStatus.Status.PROCESSING;
            progress = response.getProgress() != null ? response.getProgress() : 50;
        } else if (response.isSuccess()) {
            status = VideoGenerationStatus.Status.COMPLETED;
            videoUrl = response.getFirstVideoUrl();
            progress = 100;
        } else if (response.isFailed()) {
            status = VideoGenerationStatus.Status.FAILED;
            errorMessage = response.getError();
        } else {
            status = VideoGenerationStatus.Status.PENDING;
            progress = 0;
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
    private int calculateEstimatedTime(PikaApiResponse response) {
        // Pika 响应中包含预估时间
        if (response.getEstimate() != null) {
            return response.getEstimate();
        }
        // 默认预估 2 分钟
        return 120;
    }
}
