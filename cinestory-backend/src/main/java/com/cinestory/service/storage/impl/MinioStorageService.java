package com.cinestory.service.storage.impl;

import com.cinestory.service.storage.StorageService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.GetObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

/**
 * MinIO 存储服务实现
 */
@Slf4j
@Service
public class MinioStorageService implements StorageService {

    @Value("${storage.minio.endpoint:http://localhost:9000}")
    private String endpoint;

    @Value("${storage.minio.access-key:minioadmin}")
    private String accessKey;

    @Value("${storage.minio.secret-key:minioadmin}")
    private String secretKey;

    @Value("${storage.minio.bucket-name:cinestory}")
    private String bucketName;

    @Value("${storage.minio.enabled:false}")
    private boolean enabled;

    private final RestTemplate restTemplate;
    private MinioClient minioClient;

    public MinioStorageService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void init() {
        if (enabled) {
            try {
                minioClient = MinioClient.builder()
                        .endpoint(endpoint)
                        .credentials(accessKey, secretKey)
                        .build();
                log.info("MinIO storage service initialized");
            } catch (Exception e) {
                log.error("Failed to initialize MinIO client", e);
            }
        } else {
            log.info("MinIO storage disabled, using local file storage");
        }
    }

    @Override
    public String uploadVideo(Path videoPath, String projectId) {
        try {
            String objectName = "videos/" + projectId + "/" + videoPath.getFileName();

            if (enabled && minioClient != null) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .filename(videoPath.toString())
                                .build()
                );
                return endpoint + "/" + bucketName + "/" + objectName;
            } else {
                // 使用本地存储
                Path localDir = Paths.get("./storage/videos", projectId);
                Files.createDirectories(localDir);
                Path targetPath = localDir.resolve(videoPath.getFileName());
                Files.copy(videoPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                return "/storage/videos/" + projectId + "/" + videoPath.getFileName();
            }

        } catch (Exception e) {
            log.error("Failed to upload video", e);
            throw new RuntimeException("Failed to upload video", e);
        }
    }

    @Override
    public void downloadVideo(String videoUrl, Path targetPath) {
        try {
            if (videoUrl.startsWith("http")) {
                // 检查是否是 MinIO URL
                if (enabled && minioClient != null && videoUrl.contains(endpoint)) {
                    String objectName = extractObjectName(videoUrl);
                    try (InputStream stream = minioClient.getObject(
                            GetObjectArgs.builder()
                                    .bucket(bucketName)
                                    .object(objectName)
                                    .build())) {
                        Files.copy(stream, targetPath, StandardCopyOption.REPLACE_EXISTING);
                        log.info("Downloaded from MinIO: {} to {}", objectName, targetPath);
                    }
                } else {
                    // 使用流式 HTTP 客户端下载外部视频
                    downloadFromHttpStreaming(videoUrl, targetPath);
                }
            } else {
                // 本地文件复制
                Files.copy(Path.of("." + videoUrl), targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            log.error("Failed to download video: {}", videoUrl, e);
            throw new RuntimeException("Failed to download video", e);
        }
    }

    @Override
    public void deleteVideo(String videoUrl) {
        // 实现删除逻辑
        log.debug("Delete video: {}", videoUrl);
    }

    @Override
    public Path getTempStoragePath() {
        try {
            Path tempDir = Paths.get("./storage/temp");
            Files.createDirectories(tempDir);
            return tempDir;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create temp directory", e);
        }
    }

    /**
     * 从 URL 提取对象名称
     */
    private String extractObjectName(String url) {
        try {
            // 从 MinIO URL 格式提取: http://host/bucket/objectname
            String[] parts = url.split("/");
            if (parts.length > 1) {
                // 跳过协议、主机、bucket，获取对象名称
                int bucketIndex = -1;
                for (int i = 0; i < parts.length; i++) {
                    if (parts[i].equals(bucketName)) {
                        bucketIndex = i;
                        break;
                    }
                }
                if (bucketIndex > 0 && bucketIndex < parts.length - 1) {
                    return String.join("/", Arrays.copyOfRange(parts, bucketIndex + 1, parts.length));
                }
            }
            return url.substring(url.lastIndexOf('/') + 1);
        } catch (Exception e) {
            log.warn("Failed to extract object name from URL: {}", url, e);
            return url.substring(url.lastIndexOf('/') + 1);
        }
    }

    /**
     * 从 HTTP 下载
     */
    private void downloadFromHttp(String url, Path targetPath) {
        try {
            log.info("Downloading from HTTP: {} to {}", url, targetPath);

            // 确保父目录存在
            Files.createDirectories(targetPath.getParent());

            // 使用 RestTemplate 下载文件
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    byte[].class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Files.write(targetPath, response.getBody());
                log.info("Successfully downloaded {} bytes", response.getBody().length);
            } else {
                throw new RuntimeException("Failed to download file. HTTP status: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Failed to download from HTTP: {}", url, e);
            throw new RuntimeException("Failed to download video from URL: " + url, e);
        }
    }

    /**
     * 使用流式下载大文件
     */
    private void downloadFromHttpStreaming(String url, Path targetPath) {
        try {
            log.info("Streaming download from HTTP: {} to {}", url, targetPath);

            // 确保父目录存在
            Files.createDirectories(targetPath.getParent());

            // 使用 RestTemplate 执行请求并获取输入流
            ResponseEntity<org.springframework.core.io.Resource> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    org.springframework.core.io.Resource.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                try (InputStream inputStream = response.getBody().getInputStream();
                     BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)) {

                    Files.copy(bufferedInputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    log.info("Successfully streamed download to: {}", targetPath);
                }
            } else {
                throw new RuntimeException("Failed to download file. HTTP status: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Failed to stream download from HTTP: {}", url, e);
            throw new RuntimeException("Failed to download video from URL: " + url, e);
        }
    }
}
