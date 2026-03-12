package com.cinestory.service.video.provider;

import com.cinestory.model.dto.VideoGenerationRequest;
import com.cinestory.service.video.VideoGenerationProvider;
import com.cinestory.service.video.VideoGenerationResult;
import com.cinestory.service.video.VideoGenerationStatus;
import com.cinestory.service.video.provider.api.LumaApiRequest;
import com.cinestory.service.video.provider.api.LumaApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * Luma AI 视频生成提供商
 *
 * API 文档: https://docs.lumalabs.ai/dream-machine/api
 */
@Slf4j
@Component
public class LumaProvider implements VideoGenerationProvider {

    private static final String GENERATE_ENDPOINT = "/v1/video/generations";
    private static final String STATUS_ENDPOINT = "/v1/video/generations/";

    @Value("${video.generation.providers.luma.api-key:}")
    private String apiKey;

    @Value("${video.generation.providers.luma.base-url:https://api.lumalabs.ai}")
    private String baseUrl;

    @Value("${video.generation.providers.luma.enabled:true}")
    private boolean enabled;

    @Value("${video.generation.providers.luma.max-retries:3}")
    private int maxRetries;

    @Value("${video.generation.providers.luma.timeout:300000}")
    private int timeoutMs;

    private final RestTemplate restTemplate;

    public LumaProvider(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String getProviderName() {
        return "luma";
    }

    @Override
    public VideoGenerationResult generate(VideoGenerationRequest request) {
        if (!isAvailable()) {
            log.warn("Luma provider not available - API key not configured");
            return VideoGenerationResult.failure("Luma provider not configured", getProviderName());
        }

        try {
            log.info("Calling Luma API with prompt: {}", request.getPrompt());

            // 构建 API 请求
            LumaApiRequest apiRequest = buildApiRequest(request);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            headers.set("Accept", "application/json");

            HttpEntity<LumaApiRequest> entity = new HttpEntity<>(apiRequest, headers);

            // 发送请求
            String url = baseUrl + GENERATE_ENDPOINT;
            log.debug("POST {} with request: {}", url, apiRequest);

            ResponseEntity<LumaApiResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    LumaApiResponse.class
            );

            LumaApiResponse body = response.getBody();
            if (body != null && body.getId() != null) {
                log.info("Luma task created successfully: {}", body.getId());

                // 如果立即完成，直接返回视频 URL
                if (body.isSuccess() && body.getVideoUrl() != null) {
                    return VideoGenerationResult.builder()
                            .success(true)
                            .taskId(body.getId())
                            .provider(getProviderName())
                            .videoUrl(body.getVideoUrl())
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
                return VideoGenerationResult.failure("Invalid response from Luma API", getProviderName());
            }

        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("Luma API authentication failed: {}", e.getMessage());
            return VideoGenerationResult.failure("Invalid API key", getProviderName());

        } catch (HttpClientErrorException e) {
            log.error("Luma API error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return VideoGenerationResult.failure("API error: " + e.getStatusCode(), getProviderName());

        } catch (ResourceAccessException e) {
            log.error("Luma API connection error", e);
            return VideoGenerationResult.failure("Connection error", getProviderName());

        } catch (Exception e) {
            log.error("Luma API unexpected error", e);
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
            headers.set("Accept", "application/json");

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            // 发送请求
            String url = baseUrl + STATUS_ENDPOINT + taskId;
            log.debug("GET {} to check status", url);

            ResponseEntity<LumaApiResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    LumaApiResponse.class
            );

            LumaApiResponse body = response.getBody();
            if (body != null) {
                return mapResponseToStatus(body);
            } else {
                return VideoGenerationStatus.builder()
                        .status(VideoGenerationStatus.Status.FAILED)
                        .errorMessage("Invalid response")
                        .build();
            }

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Luma task not found: {}", taskId);
            return VideoGenerationStatus.builder()
                    .status(VideoGenerationStatus.Status.FAILED)
                    .errorMessage("Task not found")
                    .build();

        } catch (Exception e) {
            log.error("Luma status check error for task: {}", taskId, e);
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
        return 3; // 低优先级
    }

    /**
     * 构建 Luma API 请求
     */
    private LumaApiRequest buildApiRequest(VideoGenerationRequest request) {
        // Luma Dream Machine 支持 5 秒视频
        String aspectRatio = request.getAspectRatio() != null ? request.getAspectRatio() : "16:9";

        return LumaApiRequest.builder()
                .prompt(request.getPrompt())
                .imageUrl(request.getImageUrl())
                .duration(5)
                .aspectRatio(aspectRatio)
                .loop(false)
                .build();
    }

    /**
     * 映射 API 响应到状态
     */
    private VideoGenerationStatus mapResponseToStatus(LumaApiResponse response) {
        VideoGenerationStatus.Status status;
        String errorMessage = null;
        String videoUrl = null;
        Integer progress = null;

        if (response.isProcessing()) {
            status = VideoGenerationStatus.Status.PROCESSING;
            progress = 50;
        } else if (response.isSuccess()) {
            status = VideoGenerationStatus.Status.COMPLETED;
            videoUrl = response.getVideoUrl();
            progress = 100;
        } else if (response.isFailed()) {
            status = VideoGenerationStatus.Status.FAILED;
            errorMessage = response.getError() != null ? response.getError() : response.getFailureReason();
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
    private int calculateEstimatedTime(LumaApiResponse response) {
        // Luma 响应中包含预估时间
        int totalEstimate = 0;
        if (response.getWaitTimeEst() != null) {
            totalEstimate += response.getWaitTimeEst();
        }
        if (response.getProcessTimeEst() != null) {
            totalEstimate += response.getProcessTimeEst();
        }

        return totalEstimate > 0 ? totalEstimate : 120;
    }
}
