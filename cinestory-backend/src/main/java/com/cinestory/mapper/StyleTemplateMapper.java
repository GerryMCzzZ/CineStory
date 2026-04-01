package com.cinestory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cinestory.model.entity.StyleTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 风格模板 Mapper 接口
 *
 * @author CineStory
 */
@Mapper
public interface StyleTemplateMapper extends BaseMapper<StyleTemplate> {

    /**
     * 查询用户可用的风格模板（系统模板 + 用户自定义模板）
     */
    List<StyleTemplate> selectAvailableForUser(@Param("userId") Long userId);
}
