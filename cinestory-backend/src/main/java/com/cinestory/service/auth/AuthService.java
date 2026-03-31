package com.cinestory.service.auth;

import com.cinestory.config.jwt.JwtAuthenticationResponse;
import com.cinestory.config.jwt.JwtTokenProvider;
import com.cinestory.config.jwt.UserPrincipal;
import com.cinestory.model.dto.auth.LoginRequest;
import com.cinestory.model.dto.auth.RegisterRequest;
import com.cinestory.model.dto.auth.UpdatePasswordRequest;
import com.cinestory.model.dto.auth.UpdateProfileRequest;
import com.cinestory.model.entity.User;
import com.cinestory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 认证服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    /**
     * 用户注册
     */
    @Transactional
    public User register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("用户名已存在");
        }

        // 检查 Email 是否已存在
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("邮箱已被注册");
        }

        // 创建新用户
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname() != null ? request.getNickname() : request.getUsername())
                .role(User.Role.USER)
                .status(User.UserStatus.ACTIVE)
                .quotaTotal(100)
                .quotaUsed(0)
                .quotaResetDate(LocalDateTime.now().plusMonths(1))
                .apiKey(generateApiKey())
                .apiKeyEnabled(false)
                .build();

        user = userRepository.save(user);
        log.info("New user registered: {}", user.getUsername());

        return user;
    }

    /**
     * 用户登录
     */
    public JwtAuthenticationResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // 生成 Token
        String accessToken = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);

        // 更新最后登录时间
        User user = userRepository.findById(userPrincipal.getId()).orElseThrow();
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        return JwtAuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(86400000L) // 24 小时
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    /**
     * 获取当前用户信息
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userRepository.findById(userPrincipal.getId()).orElse(null);
    }

    /**
     * 获取当前用户 ID
     */
    public Long getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    /**
     * 更新密码
     */
    @Transactional
    public void updatePassword(UpdatePasswordRequest request) {
        User user = getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("未登录");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("旧密码错误");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password updated for user: {}", user.getUsername());
    }

    /**
     * 生成新的 API Key
     */
    @Transactional
    public String regenerateApiKey() {
        User user = getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("未登录");
        }

        String newApiKey = generateApiKey();
        user.setApiKey(newApiKey);
        userRepository.save(user);

        log.info("API key regenerated for user: {}", user.getUsername());
        return newApiKey;
    }

    /**
     * 切换 API Key 状态
     */
    @Transactional
    public void toggleApiKey(boolean enabled) {
        User user = getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("未登录");
        }

        user.setApiKeyEnabled(enabled);
        userRepository.save(user);
    }

    /**
     * 更新用户信息
     */
    @Transactional
    public User updateProfile(UpdateProfileRequest request) {
        User user = getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("未登录");
        }

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        user = userRepository.save(user);
        return user;
    }

    /**
     * 检查用户配额
     */
    public boolean checkQuota(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }

        // 检查是否需要重置配额
        if (user.getQuotaResetDate() != null && user.getQuotaResetDate().isBefore(LocalDateTime.now())) {
            user.resetQuota(user.getQuotaTotal());
            userRepository.save(user);
        }

        return !user.isQuotaExceeded();
    }

    /**
     * 使用配额
     */
    @Transactional
    public void consumeQuota(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new IllegalArgumentException("User not found: " + userId));

        if (user.isQuotaExceeded()) {
            throw new IllegalStateException("配额已用完");
        }

        user.incrementQuotaUsed();
        userRepository.save(user);
    }

    /**
     * 重置配额
     */
    @Transactional
    public void resetQuota(Long userId, int newTotal) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new IllegalArgumentException("User not found: " + userId));
        user.resetQuota(newTotal);
        userRepository.save(user);
    }

    /**
     * 验证 Token
     */
    public Map<String, Object> validateToken(String token) {
        boolean valid = tokenProvider.validateToken(token);
        Map<String, Object> result = new HashMap<>();
        result.put("valid", valid);

        if (valid) {
            Long userId = tokenProvider.getUserIdFromToken(token);
            result.put("userId", userId);
            result.put("expiringSoon", tokenProvider.isTokenExpiringSoon(token));
        }

        return result;
    }

    /**
     * 刷新 Token
     */
    public JwtAuthenticationResponse refreshToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        Long userId = tokenProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId).orElseThrow(
                () -> new IllegalArgumentException("User not found"));

        // 创建虚拟认证对象
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, userPrincipal.getAuthorities());

        String accessToken = tokenProvider.generateToken(authentication);
        String newRefreshToken = tokenProvider.generateRefreshToken(authentication);

        return JwtAuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(86400000L)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    /**
     * 生成 API Key
     */
    private String generateApiKey() {
        return "cs_" + UUID.randomUUID().toString().replace("-", "");
    }
}
