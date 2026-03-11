package com.cinestory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * CineStory Application Main Class
 *
 * 小说转动漫视频生成服务 - 主应用入口
 *
 * @author CineStory Team
 * @since 1.0.0
 */
@SpringBootApplication
@EnableBatchProcessing
@EnableAsync
public class CineStoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(CineStoryApplication.class, args);
    }
}
