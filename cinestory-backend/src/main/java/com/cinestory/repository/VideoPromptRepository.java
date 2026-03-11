package com.cinestory.repository;

import com.cinestory.model.entity.VideoPrompt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 视频提示词数据访问接口
 */
@Repository
public interface VideoPromptRepository extends JpaRepository<VideoPrompt, Long> {

    /**
     * 根据切片ID查询提示词
     */
    Optional<VideoPrompt> findBySliceId(Long sliceId);

    /**
     * 检查切片是否已生成提示词
     */
    boolean existsBySliceId(Long sliceId);

    /**
     * 根据切片ID列表查询提示词
     */
    List<VideoPrompt> findBySliceIdIn(List<Long> sliceIds);
}
