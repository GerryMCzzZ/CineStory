package com.cinestory.repository;

import com.cinestory.model.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 项目数据访问接口
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /**
     * 根据用户ID查询项目列表
     */
    List<Project> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 根据用户ID和状态查询项目列表
     */
    List<Project> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, Project.ProjectStatus status);

    /**
     * 根据状态查询项目列表
     */
    List<Project> findByStatusOrderByCreatedAtDesc(Project.ProjectStatus status);

    /**
     * 统计用户的项目数量
     */
    long countByUserId(Long userId);

    /**
     * 查询处理中的项目
     */
    @Query("SELECT p FROM Project p WHERE p.status = 'PROCESSING'")
    List<Project> findProcessingProjects();

    /**
     * 检查用户是否是项目所有者
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Project p WHERE p.id = :projectId AND p.userId = :userId")
    boolean existsByIdAndUserId(Long projectId, Long userId);
}
