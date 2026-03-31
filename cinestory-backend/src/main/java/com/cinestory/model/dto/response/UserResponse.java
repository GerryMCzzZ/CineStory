package com.cinestory.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String nickname;
    private String avatarUrl;
    private String bio;
    private String role;
    private String status;
    private Integer quotaTotal;
    private Integer quotaUsed;
    private LocalDateTime quotaResetDate;
    private Boolean apiKeyEnabled;
    private LocalDateTime createdAt;

    /**
     * 从 User 实体创建简单响应
     */
    public static UserResponse fromUser(com.cinestory.model.entity.User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole())
                .quotaTotal(user.getQuotaTotal())
                .quotaUsed(user.getQuotaUsed())
                .build();
    }

    /**
     * 从 User 实体创建完整响应
     */
    public static UserResponse fromUserDetail(com.cinestory.model.entity.User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .role(user.getRole())
                .status(user.getStatus().name())
                .quotaTotal(user.getQuotaTotal())
                .quotaUsed(user.getQuotaUsed())
                .quotaResetDate(user.getQuotaResetDate())
                .apiKeyEnabled(user.getApiKeyEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
