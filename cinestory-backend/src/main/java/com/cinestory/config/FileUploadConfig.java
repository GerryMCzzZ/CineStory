package com.cinestory.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件上传配置
 */
@Configuration
public class FileUploadConfig implements WebMvcConfigurer {

    @Value("${file.upload.path:/tmp/uploads}")
    private String uploadPath;

    /**
     * 配置静态资源处理
     * 允许通过 HTTP 访问上传的文件
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();

        // 映射 /uploads/** 到上传目录
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/");

        // 映射 /storage/** 到存储目录
        Path storageDir = Paths.get("./storage").toAbsolutePath().normalize();
        registry.addResourceHandler("/storage/**")
                .addResourceLocations("file:" + storageDir + "/");
    }
}
