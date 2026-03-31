package com.cinestory.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;

    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "nickname", length = 50)
    private String nickname;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "role", nullable = false, length = 20)
    @Builder.Default
    private String role = Role.USER;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    // 配额管理
    @Column(name = "quota_total")
    @Builder.Default
    private Integer quotaTotal = 100;

    @Column(name = "quota_used")
    @Builder.Default
    private Integer quotaUsed = 0;

    @Column(name = "quota_reset_date")
    private LocalDateTime quotaResetDate;

    // API 密钥
    @Column(name = "api_key", unique = true, length = 100)
    private String apiKey;

    @Column(name = "api_key_enabled")
    @Builder.Default
    private Boolean apiKeyEnabled = false;

    // 时间戳
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (quotaResetDate == null) {
            quotaResetDate = LocalDateTime.now().plusMonths(1);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

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
