package com.cinestory.repository;

import com.cinestory.model.entity.TextSlice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 文本切片数据访问接口
 */
@Repository
public interface TextSliceRepository extends JpaRepository<TextSlice, Long> {

    /**
     * 根据项目ID查询所有切片，按顺序排序
     */
    List<TextSlice> findByProjectIdOrderByOrderIndexAsc(Long projectId);

    /**
     * 根据项目ID和场景类型查询切片
     */
    List<TextSlice> findByProjectIdAndSceneTypeOrderByOrderIndexAsc(Long projectId, TextSlice.SceneType sceneType);

    /**
     * 统计项目的切片数量
     */
    long countByProjectId(Long projectId);

    /**
     * 删除项目的所有切片
     */
    void deleteByProjectId(Long projectId);
}
