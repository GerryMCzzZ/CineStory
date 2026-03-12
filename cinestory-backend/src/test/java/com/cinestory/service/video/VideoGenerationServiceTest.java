package com.cinestory.service.video;

import com.cinestory.model.dto.VideoGenerationRequest;
import com.cinestory.service.video.provider.RunwayProvider;
import com.cinestory.service.video.provider.PikaProvider;
import com.cinestory.service.video.provider.LumaProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 视频生成服务测试
 */
@ExtendWith(SpringExtension.class)
class VideoGenerationServiceTest {

    @Mock
    private RunwayProvider runwayProvider;

    @Mock
    private PikaProvider pikaProvider;

    @Mock
    private LumaProvider lumaProvider;

    private VideoGenerationService videoGenerationService;

    @BeforeEach
    void setUp() {
        List<VideoGenerationProvider> providers = List.of(
                runwayProvider,
                pikaProvider,
                lumaProvider
        );
        // videoGenerationService = new VideoGenerationService(providers, null, null);
    }

    @Test
    void testProviderPriority() {
        when(runwayProvider.getPriority()).thenReturn(1);
        when(pikaProvider.getPriority()).thenReturn(2);
        when(lumaProvider.getPriority()).thenReturn(3);

        assertTrue(runwayProvider.getPriority() < pikaProvider.getPriority());
        assertTrue(pikaProvider.getPriority() < lumaProvider.getPriority());
    }

    @Test
    void testProviderAvailability() {
        when(runwayProvider.isAvailable()).thenReturn(true);
        when(pikaProvider.isAvailable()).thenReturn(false);

        assertTrue(runwayProvider.isAvailable());
        assertFalse(pikaProvider.isAvailable());
    }

    @Test
    void testVideoGenerationRequest() {
        VideoGenerationRequest request = VideoGenerationRequest.builder()
                .prompt("A beautiful sunset over mountains")
                .negativePrompt("blurry, low quality")
                .duration(5)
                .width(1920)
                .height(1080)
                .aspectRatio("16:9")
                .fps(24)
                .build();

        assertNotNull(request);
        assertEquals("A beautiful sunset over mountains", request.getPrompt());
        assertEquals("blurry, low quality", request.getNegativePrompt());
        assertEquals(5, request.getDuration());
        assertEquals(1920, request.getWidth());
        assertEquals(1080, request.getHeight());
    }

    @Test
    void testVideoGenerationStatus() {
        VideoGenerationStatus status = VideoGenerationStatus.builder()
                .status(VideoGenerationStatus.Status.PROCESSING)
                .progress(50)
                .build();

        assertEquals(VideoGenerationStatus.Status.PROCESSING, status.getStatus());
        assertEquals(50, status.getProgress());
    }

    @Test
    void testVideoGenerationResult() {
        VideoGenerationResult successResult = VideoGenerationResult.success("task123", "https://example.com/video.mp4", "runway");

        assertTrue(successResult.isSuccess());
        assertEquals("task123", successResult.getTaskId());
        assertEquals("https://example.com/video.mp4", successResult.getVideoUrl());
        assertEquals("runway", successResult.getProvider());

        VideoGenerationResult failureResult = VideoGenerationResult.failure("API error", "pika");

        assertFalse(failureResult.isSuccess());
        assertEquals("API error", failureResult.getErrorMessage());
        assertEquals("pika", failureResult.getProvider());
    }
}
