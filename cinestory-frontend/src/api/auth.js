/**
 * 认证相关 API
 */
import request from '@/utils/request'

const AUTH_BASE_URL = '/auth'

/**
 * 用户登录
 */
export function login(data) {
  return request({
    url: `${AUTH_BASE_URL}/login`,
    method: 'POST',
    data
  })
}

/**
 * 用户注册
 */
export function register(data) {
  return request({
    url: `${AUTH_BASE_URL}/register`,
    method: 'POST',
    data
  })
}

/**
 * 刷新 Token
 */
export function refreshToken(refreshToken) {
  return request({
    url: `${AUTH_BASE_URL}/refresh`,
    method: 'POST',
    data: { refreshToken }
  })
}

/**
 * 获取当前用户信息
 */
export function getCurrentUser() {
  return request({
    url: `${AUTH_BASE_URL}/me`,
    method: 'GET'
  })
}

/**
 * 修改密码
 */
export function updatePassword(data) {
  return request({
    url: `${AUTH_BASE_URL}/password`,
    method: 'PUT',
    data
  })
}

/**
 * 更新用户信息
 */
export function updateProfile(data) {
  return request({
    url: `${AUTH_BASE_URL}/profile`,
    method: 'PATCH',
    data
  })
}

/**
 * 重新生成 API Key
 */
export function regenerateApiKey() {
  return request({
    url: `${AUTH_BASE_URL}/api-key/regenerate`,
    method: 'POST'
  })
}

/**
 * 切换 API Key 状态
 */
export function toggleApiKey(enabled) {
  return request({
    url: `${AUTH_BASE_URL}/api-key/toggle`,
    method: 'PUT',
    params: { enabled }
  })
}

/**
 * 验证 Token
 */
export function validateToken() {
  return request({
    url: `${AUTH_BASE_URL}/validate`,
    method: 'POST'
  })
}

/**
 * 退出登录
 */
export function logout() {
  return request({
    url: `${AUTH_BASE_URL}/logout`,
    method: 'POST'
  })
}
