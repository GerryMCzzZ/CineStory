import request from '@/utils/request'

/**
 * 项目管理 API
 */
export const projectApi = {
  /**
   * 获取项目列表
   */
  getList(params) {
    return request.get('/projects', { params })
  },

  /**
   * 获取项目详情
   */
  getDetail(id) {
    return request.get(`/projects/${id}`)
  },

  /**
   * 创建项目
   */
  create(data) {
    return request.post('/projects', data)
  },

  /**
   * 更新项目
   */
  update(id, data) {
    return request.put(`/projects/${id}`, data)
  },

  /**
   * 删除项目
   */
  delete(id) {
    return request.delete(`/projects/${id}`)
  },

  /**
   * 启动生成任务
   */
  startTask(id, config) {
    return request.post(`/projects/${id}/tasks`, config)
  },

  /**
   * 获取任务状态
   */
  getTaskStatus(taskId) {
    return request.get(`/tasks/${taskId}`)
  },

  /**
   * 获取任务进度
   */
  getTaskProgress(taskId) {
    return request.get(`/tasks/${taskId}/progress`)
  },

  /**
   * 取消任务
   */
  cancelTask(taskId) {
    return request.post(`/tasks/${taskId}/cancel`)
  },

  /**
   * 上传小说文件
   */
  uploadNovel(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  /**
   * 下载最终视频
   */
  downloadVideo(id) {
    return request.get(`/projects/${id}/download`, {
      responseType: 'blob'
    })
  }
}
