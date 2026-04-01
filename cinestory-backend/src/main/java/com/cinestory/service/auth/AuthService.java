package com.cinestory.service.auth;

import com.cinestory.config.jwt.JwtAuthenticationResponse;
import com.cinestory.model.dto.auth.LoginRequest;
import com.cinestory.model.dto.auth.RegisterRequest;
import com.cinestory.model.dto.auth.UpdatePasswordRequest;
import com.cinestory.model.dto.auth.UpdateProfileRequest;
import com.cinestory.model.entity.User;

import java.util.Map;

/**
 * 认证服务接口
 *
 * @author CineStory
 */
public interface AuthService {

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 注册成功的用户信息
     */
    User register(RegisterRequest request);

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return JWT 认证响应（含 accessToken、refreshToken 等）
     */
    JwtAuthenticationResponse login(LoginRequest request);

    /**
     * 刷新 Token
     *
     * @param refreshToken 刷新令牌
     * @return 新的 JWT 认证响应
     */
    JwtAuthenticationResponse refreshToken(String refreshToken);

    /**
     * 验证 Token 有效性
     *
     * @param token 待验证的令牌
     * @return 验证结果（valid、userId、expiringSoon）
     */
    Map<String, Object> validateToken(String token);

    /**
     * 获取当前登录用户
     *
     * @return 当前用户实体，未登录时返回 null
     */
    User getCurrentUser();

    /**
     * 获取当前登录用户 ID
     *
     * @return 用户 ID，未登录时返回 null
     */
    Long getCurrentUserId();

    /**
     * 更新密码
     *
     * @param request 修改密码请求
     */
    void updatePassword(UpdatePasswordRequest request);

    /**
     * 重新生成 API Key
     *
     * @return 新的 API Key
     */
    String regenerateApiKey();

    /**
     * 切换 API Key 启用/禁用状态
     *
     * @param enabled 是否启用
     */
    void toggleApiKey(boolean enabled);

    /**
     * 更新用户资料
     *
     * @param request 更新资料请求
     * @return 更新后的用户信息
     */
    User updateProfile(UpdateProfileRequest request);

    /**
     * 检查用户配额是否充足
     *
     * @param userId 用户 ID
     * @return true 表示配额充足
     */
    boolean checkQuota(Long userId);

    /**
     * 消耗用户配额
     *
     * @param userId 用户 ID
     */
    void consumeQuota(Long userId);

    /**
     * 重置用户配额
     *
     * @param userId  用户 ID
     * @param newTotal 新的配额总量
     */
    void resetQuota(Long userId, int newTotal);
}
