package com.cinestory.config.jwt;

import com.cinestory.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户认证信息（实现 UserDetails）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal {

    private Long id;
    private String username;
    private String email;
    private String password;
    private String nickname;
    private String avatarUrl;
    private String role;
    private User.UserStatus status;
    private Integer quotaTotal;
    private Integer quotaUsed;
    private String apiKey;
    private Boolean apiKeyEnabled;

    private Collection<? extends GrantedAuthority> authorities;

    /**
     * 从 User 实体创建 UserPrincipal
     */
    public static UserPrincipal create(User user) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole()));

        return UserPrincipal.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .password(user.getPassword())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .status(user.getStatus())
                .quotaTotal(user.getQuotaTotal())
                .quotaUsed(user.getQuotaUsed())
                .apiKey(user.getApiKey())
                .apiKeyEnabled(user.getApiKeyEnabled())
                .authorities(authorities)
                .build();
    }

    /**
     * 检查配额
     */
    public boolean isQuotaAvailable() {
        return quotaUsed < quotaTotal;
    }

    /**
     * 检查是否为管理员
     */
    public boolean isAdmin() {
        return User.Role.ADMIN.equals(role);
    }

    /**
     * 检查是否为 VIP 用户
     */
    public boolean isVip() {
        return User.Role.VIP.equals(role) || User.Role.ADMIN.equals(role);
    }
}
