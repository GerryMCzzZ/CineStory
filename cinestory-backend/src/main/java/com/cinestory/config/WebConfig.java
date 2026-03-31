package com.cinestory.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置
 * CORS 由 SecurityConfig 统一管理，避免重复配置
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
}
