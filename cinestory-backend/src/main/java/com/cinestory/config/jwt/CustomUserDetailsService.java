package com.cinestory.config.jwt;

import com.cinestory.model.entity.User;
import com.cinestory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 用户详情服务（用于 Spring Security）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new UsernameNotFoundException("User is not active: " + username);
        }

        return UserPrincipal.create(user);
    }

    /**
     * 根据 Email 加载用户
     */
    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new UsernameNotFoundException("User is not active: " + email);
        }

        return UserPrincipal.create(user);
    }

    /**
     * 根据 ID 加载用户
     */
    public UserDetails loadUserById(Long id) throws UsernameNotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new UsernameNotFoundException("User is not active: " + id);
        }

        return UserPrincipal.create(user);
    }

    /**
     * 根据 API Key 加载用户
     */
    public UserDetails loadUserByApiKey(String apiKey) throws UsernameNotFoundException {
        User user = userRepository.findByActiveApiKey(apiKey)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid API key"));

        return UserPrincipal.create(user);
    }
}
