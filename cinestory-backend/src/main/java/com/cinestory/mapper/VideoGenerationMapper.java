package com.cinestory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cinestory.model.entity.VideoGeneration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 视频生成记录 Mapper 接口
 *
 * @author CineStory
 */
@Mapper
public interface VideoGenerationMapper extends BaseMapper<VideoGeneration> {

    /**
     * 查询待处理的生成记录
     */
    List<VideoGeneration> selectPendingGenerations();

    /**
     * 查询处理中超时的记录
     */
    List<VideoGeneration> selectTimeoutProcessing(@Param("timeoutBefore") LocalDateTime timeoutBefore);

    /**
     * 统计指定提供商前缀的记录数
     */
    long countByProviderStartingWith(@Param("prefix") String prefix);
}
