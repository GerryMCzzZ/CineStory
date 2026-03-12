package com.cinestory.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建项目请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProjectRequest {

    @NotBlank(message = "项目名称不能为空")
    @Size(max = 255, message = "项目名称不能超过255个字符")
    private String name;

    @Size(max = 1000, message = "项目描述不能超过1000个字符")
    private String description;

    @NotBlank(message = "小说内容不能为空")
    private String novelContent;

    private String novelTitle;

    private String novelAuthor;

    private Long styleTemplateId;

    private String configJson;
}
