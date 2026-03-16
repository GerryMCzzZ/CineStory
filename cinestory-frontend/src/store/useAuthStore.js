import { defineStore } from 'pinia'
import { login, register, getCurrentUser, refreshToken, updatePassword, updateProfile } from '@/api/auth'

/**
 * 用户认证状态管理
 */
export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    token: localStorage.getItem('access_token'),
    refreshToken: localStorage.getItem('refresh_token'),
    isAuthenticated: false,
    loading: false
  }),

  getters: {
    // 用户名
    username: (state) => state.user?.username || '',

    // 用户昵称
    nickname: (state) => state.user?.nickname || state.user?.username || '',

    // 用户角色
    role: (state) => state.user?.role || 'USER',

    // 是否为管理员
    isAdmin: (state) => state.user?.role === 'ADMIN',

    // 是否为 VIP
    isVip: (state) => state.user?.role === 'VIP' || state.user?.role === 'ADMIN',

    // 配额信息
    quota: (state) => ({
      total: state.user?.quotaTotal || 0,
      used: state.user?.quotaUsed || 0,
      remaining: (state.user?.quotaTotal || 0) - (state.user?.quotaUsed || 0)
    }),

    // 配额是否已用完
    isQuotaExceeded: (state) => (state.user?.quotaUsed || 0) >= (state.user?.quotaTotal || 0)
  },

  actions: {
    /**
     * 设置 Token
     */
    setTokens(accessToken, refreshTokenValue) {
      this.token = accessToken
      this.refreshToken = refreshTokenValue
      localStorage.setItem('access_token', accessToken)
      localStorage.setItem('refresh_token', refreshTokenValue)
    },

    /**
     * 清除 Token
     */
    clearTokens() {
      this.token = null
      this.refreshToken = null
      localStorage.removeItem('access_token')
      localStorage.removeItem('refresh_token')
    },

    /**
     * 用户登录
     */
    async login(credentials) {
      this.loading = true
      try {
        const response = await login(credentials)

        this.setTokens(response.data.accessToken, response.data.refreshToken)
        this.user = response.data
        this.isAuthenticated = true

        return response.data
      } catch (error) {
        this.clearTokens()
        this.user = null
        this.isAuthenticated = false
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 用户注册
     */
    async register(userData) {
      this.loading = true
      try {
        const response = await register(userData)

        // 注册成功后自动登录
        await this.login({
          username: userData.username,
          password: userData.password
        })

        return response.data
      } catch (error) {
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 获取当前用户信息
     */
    async fetchCurrentUser() {
      if (!this.token) {
        return
      }

      try {
        const response = await getCurrentUser()
        this.user = response.data
        this.isAuthenticated = true
      } catch (error) {
        // Token 可能已过期，尝试刷新
        await this.refreshAccessToken()
      }
    },

    /**
     * 刷新访问 Token
     */
    async refreshAccessToken() {
      if (!this.refreshToken) {
        this.logout()
        return
      }

      try {
        const response = await refreshToken(this.refreshToken)
        this.setTokens(response.data.accessToken, response.data.refreshToken)
        this.user = response.data
        this.isAuthenticated = true
      } catch (error) {
        // 刷新失败，退出登录
        this.logout()
        throw error
      }
    },

    /**
     * 修改密码
     */
    async changePassword(oldPassword, newPassword) {
      return await updatePassword({ oldPassword, newPassword })
    },

    /**
     * 更新用户信息
     */
    async updateUserProfile(updates) {
      const response = await updateProfile(updates)
      this.user = { ...this.user, ...response.data }
      return response.data
    },

    /**
     * 退出登录
     */
    logout() {
      this.clearTokens()
      this.user = null
      this.isAuthenticated = false
    },

    /**
     * 检查并使用配额
     */
    checkQuota() {
      if (this.isQuotaExceeded) {
        throw new Error('配额已用完，请升级账户或等待下月重置')
      }
      return true
    }
  }
})
