package com.cinestory.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户实体
 *
 * @author CineStory
 */
@TableName("users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String email;

    private String password;

    private String nickname;

    private String avatarUrl;

    private String bio;

    @Builder.Default
    private String role = Role.USER;

    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    /**
     * 配额总量
     */
    @Builder.Default
    private Integer quotaTotal = 100;

    /**
     * 已使用配额
     */
    @Builder.Default
    private Integer quotaUsed = 0;

    private LocalDateTime quotaResetDate;

    /**
     * API 密钥
     */
    private String apiKey;

    @Builder.Default
    private Boolean apiKeyEnabled = false;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    private LocalDateTime lastLoginAt;

    /**
     * 检查配额是否已用完
     */
    public boolean isQuotaExceeded() {
        return quotaUsed >= quotaTotal;
    }

    /**
     * 增加配额使用量
     */
    public void incrementQuotaUsed() {
        this.quotaUsed++;
    }

    /**
     * 重置配额
     */
    public void resetQuota(int newTotal) {
        this.quotaTotal = newTotal;
        this.quotaUsed = 0;
        this.quotaResetDate = LocalDateTime.now().plusMonths(1);
    }

    /**
     * 用户角色常量
     */
    public static class Role {
        public static final String USER = "USER";
        public static final String ADMIN = "ADMIN";
        public static final String VIP = "VIP";
    }

    /**
     * 用户状态
     */
    public enum UserStatus {
        ACTIVE,
        INACTIVE,
        LOCKED,
        BANNED
    }
}
