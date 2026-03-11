import request from '@/utils/request'

/**
 * 风格模板 API
 */
export const styleApi = {
  /**
   * 获取风格列表
   */
  getList(params) {
    return request.get('/styles', { params })
  },

  /**
   * 获取风格详情
   */
  getDetail(id) {
    return request.get(`/styles/${id}`)
  },

  /**
   * 创建自定义风格
   */
  create(data) {
    return request.post('/styles', data)
  },

  /**
   * 更新风格
   */
  update(id, data) {
    return request.put(`/styles/${id}`, data)
  },

  /**
   * 删除风格
   */
  delete(id) {
    return request.delete(`/styles/${id}`)
  }
}
