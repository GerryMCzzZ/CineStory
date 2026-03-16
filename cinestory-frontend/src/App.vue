<template>
  <div id="app" class="min-h-screen bg-gray-50 dark:bg-gray-900">
    <!-- 顶部导航 -->
    <header class="bg-white dark:bg-gray-800 shadow-sm">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-center h-16">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 bg-gradient-to-br from-purple-500 to-pink-500 rounded-lg flex items-center justify-center">
              <span class="text-white font-bold text-xl">C</span>
            </div>
            <h1 class="text-xl font-bold text-gray-900 dark:text-white">CineStory</h1>
          </div>
          <nav class="flex items-center gap-6">
            <router-link
              to="/"
              class="text-gray-600 dark:text-gray-300 hover:text-purple-600 dark:hover:text-purple-400 transition-colors"
            >
              首页
            </router-link>
            <router-link
              to="/projects"
              class="text-gray-600 dark:text-gray-300 hover:text-purple-600 dark:hover:text-purple-400 transition-colors"
            >
              项目
            </router-link>
            <router-link
              to="/styles"
              class="text-gray-600 dark:text-gray-300 hover:text-purple-600 dark:hover:text-purple-400 transition-colors"
            >
              风格模板
            </router-link>

            <!-- 用户区域 -->
            <div class="relative">
              <!-- 已登录 -->
              <template v-if="authStore.isAuthenticated">
                <button
                  @click="showUserMenu = !showUserMenu"
                  class="flex items-center gap-2 px-3 py-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
                >
                  <div class="w-8 h-8 bg-gradient-to-br from-purple-500 to-pink-500 rounded-full flex items-center justify-center">
                    <span class="text-white text-sm font-medium">{{ authStore.nickname?.charAt(0).toUpperCase() || 'U' }}</span>
                  </div>
                  <span class="text-gray-700 dark:text-gray-300 text-sm">{{ authStore.nickname }}</span>
                  <svg class="w-4 h-4 text-gray-500" :class="{ 'rotate-180': showUserMenu }" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
                  </svg>
                </button>

                <!-- 用户下拉菜单 -->
                <div
                  v-if="showUserMenu"
                  class="absolute right-0 mt-2 w-48 bg-white dark:bg-gray-800 rounded-lg shadow-lg border border-gray-200 dark:border-gray-700 py-2 z-50"
                >
                  <div class="px-4 py-2 border-b border-gray-200 dark:border-gray-700">
                    <p class="text-sm font-medium text-gray-900 dark:text-white">{{ authStore.nickname }}</p>
                    <p class="text-xs text-gray-500 dark:text-gray-400">{{ authStore.quota.used }} / {{ authStore.quota.total }} 配额</p>
                  </div>
                  <router-link
                    to="/profile"
                    @click="showUserMenu = false"
                    class="flex items-center gap-2 px-4 py-2 text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
                  >
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                    </svg>
                    个人中心
                  </router-link>
                  <button
                    @click="handleLogout"
                    class="flex items-center gap-2 w-full px-4 py-2 text-red-600 hover:bg-red-50 dark:hover:bg-red-900/20 transition-colors"
                  >
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                    </svg>
                    退出登录
                  </button>
                </div>
              </template>

              <!-- 未登录 -->
              <router-link
                v-else
                to="/login"
                class="px-5 py-2.5 bg-purple-600 hover:bg-purple-700 text-white text-sm font-medium rounded-xl transition-all duration-200 border-0"
              >
                登录 / 注册
              </router-link>
            </div>
          </nav>
        </div>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <router-view />
    </main>

    <!-- 底部 -->
    <footer class="bg-white dark:bg-gray-800 border-t border-gray-200 dark:border-gray-700 mt-12">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <p class="text-center text-gray-500 dark:text-gray-400 text-sm">
          © 2025 CineStory. All rights reserved.
        </p>
      </div>
    </footer>

    <!-- 全局组件 -->
    <Toast />
    <ConfirmDialog
      :show="confirmState.show"
      :title="confirmState.title"
      :message="confirmState.message"
      :warning="confirmState.warning"
      :confirm-text="confirmState.confirmText"
      :confirm-button-text="confirmState.confirmButtonText"
      :cancel-button-text="confirmState.cancelButtonText"
      :type="confirmState.type"
      @update:show="confirmHandlers.handleCancel"
      @confirm="confirmHandlers.handleConfirm"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/useAuthStore'
import Toast from '@/components/Toast.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import { initConfirm, getConfirmState, getConfirmHandlers } from '@/composables/useConfirm'

const router = useRouter()
const authStore = useAuthStore()

const showUserMenu = ref(false)

// 初始化确认对话框
const confirmState = getConfirmState()
const confirmHandlers = getConfirmHandlers()

// 点击外部关闭用户菜单
const handleClickOutside = (event) => {
  const userMenu = document.querySelector('[data-user-menu]')
  if (userMenu && !userMenu.contains(event.target)) {
    showUserMenu.value = false
  }
}

onMounted(async () => {
  // 初始化确认对话框
  initConfirm()

  // 检查系统主题偏好
  if (window.matchMedia('(prefers-color-scheme: dark)').matches) {
    document.documentElement.classList.add('dark')
  }

  // 尝试获取当前用户信息
  if (authStore.token) {
    await authStore.fetchCurrentUser()
  }

  // 添加点击外部监听
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})

const handleLogout = async () => {
  const confirmed = await confirmDialog({
    message: '确定要退出登录吗？',
    title: '退出登录'
  })

  if (confirmed) {
    authStore.logout()
    showUserMenu.value = false
    router.push('/login')
  }
}
</script>
