package com.cinestory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cinestory.model.entity.VideoPrompt;
import org.apache.ibatis.annotations.Mapper;

/**
 * 视频提示词 Mapper 接口
 *
 * @author CineStory
 */
@Mapper
public interface VideoPromptMapper extends BaseMapper<VideoPrompt> {
}
