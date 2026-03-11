package com.cinestory.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 小说输入DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NovelInput {

    /**
     * 小说标题
     */
    private String title;

    /**
     * 小说作者
     */
    private String author;

    /**
     * 小说内容（纯文本）
     */
    private String content;

    /**
     * 内容类型（txt, md等）
     */
    private String contentType;

    /**
     * 编码（默认UTF-8）
     */
    @Builder.Default
    private String encoding = "UTF-8";
}
