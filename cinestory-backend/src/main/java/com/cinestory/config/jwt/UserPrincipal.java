package com.cinestory.config.jwt;

import com.cinestory.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * 用户认证信息（实现 UserDetails）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

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

    @Builder.Default
    private Collection<? extends GrantedAuthority> authorities = new HashSet<>();

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

    // ========== UserDetails 实现 ==========

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != User.UserStatus.LOCKED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == User.UserStatus.ACTIVE;
    }
}
