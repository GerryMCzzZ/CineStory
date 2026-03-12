package com.cinestory.service.storage;

import java.nio.file.Path;

/**
 * 存储服务接口
 */
public interface StorageService {

    /**
     * 上传视频
     *
     * @param videoPath 视频文件路径
     * @param projectId 项目 ID
     * @return 视频 URL
     */
    String uploadVideo(Path videoPath, String projectId);

    /**
     * 下载视频
     *
     * @param videoUrl 视频 URL
     * @param targetPath 目标路径
     */
    void downloadVideo(String videoUrl, Path targetPath);

    /**
     * 删除视频
     *
     * @param videoUrl 视频 URL
     */
    void deleteVideo(String videoUrl);

    /**
     * 获取临时存储路径
     *
     * @return 临时路径
     */
    Path getTempStoragePath();
}
