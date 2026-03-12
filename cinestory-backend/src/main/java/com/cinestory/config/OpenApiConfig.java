package com.cinestory.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3.0 配置
 * 自动生成 API 文档
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cinestoryOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CineStory API")
                        .description("小说转动漫视频生成服务 API 文档")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("CineStory Team")
                                .email("support@cinestory.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT"))
                        .addSchemas("ErrorResponse", createErrorResponseSchema()))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/api/**")
                .build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("admin")
                .pathsToMatch("/api/admin/**")
                .build();
    }

    private Schema<?> createErrorResponseSchema() {
        return new Schema<>()
                .type("object")
                .addProperty("code", new Schema<>().type("integer").description("错误码"))
                .addProperty("message", new Schema<>().type("string").description("错误消息"))
                .addProperty("timestamp", new Schema<>().type("integer").description("时间戳"))
                .addProperty("path", new Schema<>().type("string").description("请求路径"));
    }
}
