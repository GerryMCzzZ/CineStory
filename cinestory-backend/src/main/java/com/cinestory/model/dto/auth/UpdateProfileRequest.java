package com.cinestory.model.dto.auth;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新用户信息请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @Size(max = 50, message = "昵称长度不能超过50")
    private String nickname;

    @Size(max = 500, message = "个人简介不能超过500")
    private String bio;

    @Size(max = 500, message = "头像URL不能超过500")
    private String avatarUrl;
}
