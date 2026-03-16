import axios from 'axios'
import { useAuthStore } from '@/store/useAuthStore'

// 创建 axios 实例
const request = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 添加 token
    const token = localStorage.getItem('access_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 是否正在刷新 token
let isRefreshing = false
// 等待队列
let failedQueue = []

const processQueue = (error, token = null) => {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error)
    } else {
      prom.resolve(token)
    }
  })
  failedQueue = []
}

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    return response.data
  },
  async (error) => {
    const originalRequest = error.config

    // 如果是 401 且未重试过
    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        // 如果正在刷新，将请求加入队列
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        }).then(token => {
          originalRequest.headers.Authorization = `Bearer ${token}`
          return request(originalRequest)
        }).catch(err => {
          return Promise.reject(err)
        })
      }

      originalRequest._retry = true
      isRefreshing = true

      const authStore = useAuthStore()
      const refreshToken = localStorage.getItem('refresh_token')

      if (refreshToken) {
        try {
          // 尝试刷新 token
          const { data } = await axios.post('/api/auth/refresh', { refreshToken })
          const newAccessToken = data.data.accessToken

          localStorage.setItem('access_token', newAccessToken)
          authStore.token = newAccessToken

          processQueue(null, newAccessToken)

          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`
          return request(originalRequest)
        } catch (refreshError) {
          // 刷新失败，清除用户信息并跳转到登录页
          processQueue(refreshError, null)
          authStore.logout()
          window.location.href = '/login'
          return Promise.reject(refreshError)
        } finally {
          isRefreshing = false
        }
      } else {
        // 没有 refresh token，直接退出登录
        authStore.logout()
        window.location.href = '/login'
      }
    }

    // 统一错误处理
    const message = error.response?.data?.message || error.message || '请求失败'
    console.error('API Error:', message)

    // 根据状态码显示不同类型的错误提示
    if (error.response?.status === 403) {
      window.toast?.error('权限不足，请联系管理员', '错误')
    } else if (error.response?.status === 404) {
      window.toast?.error('请求的资源不存在', '错误')
    } else if (error.response?.status >= 500) {
      window.toast?.error('服务器错误，请稍后重试', '错误')
    } else if (error.code === 'ECONNABORTED') {
      window.toast?.error('请求超时，请检查网络连接', '错误')
    } else if (error.code === 'ERR_NETWORK') {
      window.toast?.error('网络连接失败，请检查网络', '错误')
    }
    // 其他错误不显示 toast，由调用方处理

    return Promise.reject(error)
  }
)

export default request

/**
 * 封装的请求方法，带有加载提示
 */
export function requestWithLoading(requestFn, loadingMessage = '加载中...') {
  // 可以在这里扩展全局加载状态
  return requestFn()
}
