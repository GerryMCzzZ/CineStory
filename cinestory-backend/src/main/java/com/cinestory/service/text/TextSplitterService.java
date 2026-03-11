package com.cinestory.service.text;

import com.cinestory.model.dto.NovelInput;
import com.cinestory.model.dto.SplitConfig;
import com.cinestory.model.entity.TextSlice;

import java.util.List;

/**
 * 文本切片服务接口
 * 负责将小说文本智能分割成适合视频生成的切片
 */
public interface TextSplitterService {

    /**
     * 将小说内容分割成切片
     *
     * @param input 小说输入
     * @param config 切片配置
     * @return 切片列表，按顺序排列
     */
    List<TextSlice> split(NovelInput input, SplitConfig config);

    /**
     * 将小说内容分割成切片（使用默认配置）
     *
     * @param input 小说输入
     * @return 切片列表，按顺序排列
     */
    List<TextSlice> split(NovelInput input);

    /**
     * 为项目生成切片并保存到数据库
     *
     * @param projectId 项目ID
     * @param input 小说输入
     * @param config 切片配置
     * @return 保存后的切片列表
     */
    List<TextSlice> splitAndSave(Long projectId, NovelInput input, SplitConfig config);

    /**
     * 检测切片的场景类型
     *
     * @param content 切片内容
     * @return 场景类型
     */
    TextSlice.SceneType detectSceneType(String content);

    /**
     * 提取切片中的对话内容
     *
     * @param content 切片内容
     * @return 对话内容列表
     */
    List<String> extractDialogues(String content);

    /**
     * 估算切片对应的视频时长
     *
     * @param characterCount 字符数
     * @param sceneType 场景类型
     * @return 估算时长（秒）
     */
    int estimateDuration(int characterCount, TextSlice.SceneType sceneType);
}
