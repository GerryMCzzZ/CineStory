package com.cinestory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cinestory.model.entity.Project;
import org.apache.ibatis.annotations.Mapper;

/**
 * 项目 Mapper 接口
 *
 * @author CineStory
 */
@Mapper
public interface ProjectMapper extends BaseMapper<Project> {
}
