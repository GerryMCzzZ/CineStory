/**
 * 文件 API
 */
import request from '@/utils/request'

/**
 * 上传小说文件
 */
export function uploadNovel(file, onProgress) {
  const formData = new FormData()
  formData.append('file', file)

  return request({
    url: '/files/upload-novel',
    method: 'POST',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress: (progressEvent) => {
      if (onProgress && progressEvent.total) {
        const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        onProgress(percent)
      }
    }
  })
}

/**
 * 上传并预览文件
 */
export function previewFile(file) {
  const formData = new FormData()
  formData.append('file', file)

  return request({
    url: '/files/preview',
    method: 'POST',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 上传风格图片
 */
export function uploadStyleImage(file, onProgress) {
  const formData = new FormData()
  formData.append('file', file)

  return request({
    url: '/files/upload-style-image',
    method: 'POST',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress: (progressEvent) => {
      if (onProgress && progressEvent.total) {
        const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        onProgress(percent)
      }
    }
  })
}

/**
 * 获取上传配置
 */
export function getUploadConfig() {
  return request({
    url: '/files/config',
    method: 'GET'
  })
}

// 通用上传方法
export function uploadFile(formData, onProgress) {
  return request({
    url: '/files/upload-novel',
    method: 'POST',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress: (progressEvent) => {
      if (onProgress && progressEvent.total) {
        const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        onProgress(percent)
      }
    }
  })
}
