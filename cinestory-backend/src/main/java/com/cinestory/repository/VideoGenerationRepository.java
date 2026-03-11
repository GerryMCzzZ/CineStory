package com.cinestory.repository;

import com.cinestory.model.entity.VideoGeneration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 视频生成记录数据访问接口
 */
@Repository
public interface VideoGenerationRepository extends JpaRepository<VideoGeneration, Long> {

    /**
     * 根据切片ID查询生成记录
     */
    List<VideoGeneration> findBySliceId(Long sliceId);

    /**
     * 根据状态查询生成记录
     */
    List<VideoGeneration> findByStatusOrderByCreatedAtAsc(VideoGeneration.GenerationStatus status);

    /**
     * 根据第三方任务ID查询
     */
    Optional<VideoGeneration> findByProviderTaskId(String providerTaskId);

    /**
     * 根据切片ID查询最新的一条记录
     */
    Optional<VideoGeneration> findFirstBySliceIdOrderByCreatedAtDesc(Long sliceId);

    /**
     * 查询待处理的生成记录
     */
    @Query("SELECT vg FROM VideoGeneration vg WHERE vg.status = 'PENDING' ORDER BY vg.createdAt ASC")
    List<VideoGeneration> findPendingGenerations();

    /**
     * 查询处理中超时的记录
     */
    @Query("SELECT vg FROM VideoGeneration vg WHERE vg.status = 'PROCESSING' AND vg.createdAt < :timeoutBefore")
    List<VideoGeneration> findTimeoutProcessing(java.time.LocalDateTime timeoutBefore);

    /**
     * 统计各状态的记录数
     */
    long countByStatus(VideoGeneration.GenerationStatus status);

    /**
     * 根据提供商和状态统计
     */
    long countByProviderAndStatus(String provider, VideoGeneration.GenerationStatus status);
}
