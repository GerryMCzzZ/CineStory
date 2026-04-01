package com.cinestory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cinestory.model.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户 Mapper 接口
 *
 * @author CineStory
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据 API Key 查找启用的用户
     */
    User selectByActiveApiKey(@Param("apiKey") String apiKey);

    /**
     * 查找需要重置配额的用户
     */
    List<User> selectUsersNeedingQuotaReset(@Param("now") LocalDateTime now);
}
