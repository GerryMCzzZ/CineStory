package com.cinestory.repository;

import com.cinestory.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 用户数据访问接口
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据 Email 查找用户
     */
    Optional<User> findByEmail(String email);

    /**
     * 根据 API Key 查找用户
     */
    Optional<User> findByApiKey(String apiKey);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查 Email 是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 查找需要重置配额的用户
     */
    @Query("SELECT u FROM User u WHERE u.quotaResetDate <= :now AND u.quotaUsed > 0")
    java.util.List<User> findUsersNeedingQuotaReset(@Param("now") LocalDateTime now);

    /**
     * 根据 API Key 查找启用的用户
     */
    @Query("SELECT u FROM User u WHERE u.apiKey = :apiKey AND u.apiKeyEnabled = true")
    Optional<User> findByActiveApiKey(@Param("apiKey") String apiKey);

    /**
     * 统计活跃用户数
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.status = 'ACTIVE'")
    long countActiveUsers();

    /**
     * 分页查询用户
     */
    Page<User> findByStatus(User.UserStatus status, Pageable pageable);
}
