package com.cinestory.controller;

import com.cinestory.config.jwt.JwtAuthenticationResponse;
import com.cinestory.config.jwt.UserPrincipal;
import com.cinestory.model.dto.ApiResponse;
import com.cinestory.model.dto.auth.*;
import com.cinestory.model.entity.User;
import com.cinestory.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证", description = "用户认证相关接口")
public class AuthController {

    private final AuthService authService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "创建新用户账号")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request);
        UserResponse response = UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole())
                .quotaTotal(user.getQuotaTotal())
                .quotaUsed(user.getQuotaUsed())
                .build();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "使用用户名密码登录，返回 JWT Token")
    public ResponseEntity<ApiResponse<JwtAuthenticationResponse>> login(@Valid @RequestBody LoginRequest request) {
        JwtAuthenticationResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 刷新 Token
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新 Token", description = "使用刷新 Token 获取新的访问 Token")
    public ResponseEntity<ApiResponse<JwtAuthenticationResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        JwtAuthenticationResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 验证 Token
     */
    @PostMapping("/validate")
    @Operation(summary = "验证 Token", description = "验证 Token 是否有效")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateToken(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Map<String, Object> result = authService.validateToken(token);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = authService.getCurrentUser();
        if (user == null) {
            return ResponseEntity.ok(ApiResponse.error("未登录"));
        }

        UserResponse response = UserResponse.builder()
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

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    @Operation(summary = "用户退出", description = "用户退出登录")
    public ResponseEntity<ApiResponse<Void>> logout() {
        // JWT 是无状态的，客户端删除 token 即可
        // 可选：实现 token 黑名单机制
        return ResponseEntity.ok(ApiResponse.success(null, "退出成功"));
    }

    /**
     * 更新密码
     */
    @PutMapping("/password")
    @Operation(summary = "修改密码", description = "修改当前用户密码")
    public ResponseEntity<ApiResponse<Void>> updatePassword(@Valid @RequestBody UpdatePasswordRequest request) {
        authService.updatePassword(request);
        return ResponseEntity.ok(ApiResponse.success(null, "密码修改成功"));
    }

    /**
     * 更新用户信息
     */
    @PatchMapping("/profile")
    @Operation(summary = "更新用户信息", description = "更新昵称、头像等用户信息")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(@RequestBody Map<String, Object> updates) {
        User user = authService.updateProfile(updates);
        UserResponse response = UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .role(user.getRole())
                .build();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 重新生成 API Key
     */
    @PostMapping("/api-key/regenerate")
    @Operation(summary = "重新生成 API Key", description = "重新生成用户的 API Key")
    public ResponseEntity<ApiResponse<Map<String, String>>> regenerateApiKey() {
        String newApiKey = authService.regenerateApiKey();
        Map<String, String> result = new HashMap<>();
        result.put("apiKey", newApiKey);
        return ResponseEntity.ok(ApiResponse.success(result, "API Key 已重新生成"));
    }

    /**
     * 切换 API Key 状态
     */
    @PutMapping("/api-key/toggle")
    @Operation(summary = "切换 API Key 状态", description = "启用或禁用 API Key")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> toggleApiKey(@RequestParam boolean enabled) {
        authService.toggleApiKey(enabled);
        Map<String, Boolean> result = new HashMap<>();
        result.put("enabled", enabled);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 用户响应 DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UserResponse {
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
        private java.time.LocalDateTime quotaResetDate;
        private Boolean apiKeyEnabled;
        private java.time.LocalDateTime createdAt;
    }
}
