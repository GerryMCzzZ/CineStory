package com.cinestory.config.jwt;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cinestory.mapper.UserMapper;
import com.cinestory.model.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 用户详情服务（用于 Spring Security）
 *
 * @author CineStory
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new UsernameNotFoundException("User is not active: " + username);
        }

        return UserPrincipal.create(user);
    }

    /**
     * 根据 Email 加载用户
     */
    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new UsernameNotFoundException("User is not active: " + email);
        }

        return UserPrincipal.create(user);
    }

    /**
     * 根据 ID 加载用户
     */
    public UserDetails loadUserById(Long id) throws UsernameNotFoundException {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with id: " + id);
        }

        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new UsernameNotFoundException("User is not active: " + id);
        }

        return UserPrincipal.create(user);
    }

    /**
     * 根据 API Key 加载用户
     */
    public UserDetails loadUserByApiKey(String apiKey) throws UsernameNotFoundException {
        User user = userMapper.selectByActiveApiKey(apiKey);
        if (user == null) {
            throw new UsernameNotFoundException("Invalid API key");
        }

        return UserPrincipal.create(user);
    }
}
