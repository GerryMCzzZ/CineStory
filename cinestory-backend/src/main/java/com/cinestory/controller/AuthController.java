package com.cinestory.controller;

import com.cinestory.config.jwt.JwtAuthenticationResponse;
import com.cinestory.config.jwt.UserPrincipal;
import com.cinestory.model.dto.response.ApiResponse;
import com.cinestory.model.dto.response.UserResponse;
import com.cinestory.model.dto.auth.LoginRequest;
import com.cinestory.model.dto.auth.RegisterRequest;
import com.cinestory.model.dto.auth.UpdatePasswordRequest;
import com.cinestory.model.dto.auth.UpdateProfileRequest;
import com.cinestory.model.dto.auth.RefreshTokenRequest;
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
@RequestMapping("/auth")
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
        return ResponseEntity.ok(ApiResponse.success(UserResponse.fromUser(user)));
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
        if (token.equals("Bearer ")) {
            return ResponseEntity.ok(ApiResponse.error(401, "Missing token"));
        }
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
            return ResponseEntity.ok(ApiResponse.error(401, "未登录"));
        }
        return ResponseEntity.ok(ApiResponse.success(UserResponse.fromUserDetail(user)));
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    @Operation(summary = "用户退出", description = "用户退出登录")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.success("退出成功", null));
    }

    /**
     * 更新密码
     */
    @PutMapping("/password")
    @Operation(summary = "修改密码", description = "修改当前用户密码")
    public ResponseEntity<ApiResponse<Void>> updatePassword(@Valid @RequestBody UpdatePasswordRequest request) {
        authService.updatePassword(request);
        return ResponseEntity.ok(ApiResponse.success("密码修改成功", null));
    }

    /**
     * 更新用户信息
     */
    @PatchMapping("/profile")
    @Operation(summary = "更新用户信息", description = "更新昵称、头像等用户信息")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        User user = authService.updateProfile(request);
        return ResponseEntity.ok(ApiResponse.success(UserResponse.fromUser(user)));
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
        return ResponseEntity.ok(ApiResponse.success("API Key 已重新生成", result));
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
}
