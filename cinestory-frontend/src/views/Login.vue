<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900 px-4">
    <div class="max-w-md w-full">
      <!-- Logo -->
      <div class="text-center mb-10">
        <div class="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-br from-purple-600 to-pink-600 rounded-2xl shadow-lg mb-4">
          <span class="text-white font-bold text-2xl">C</span>
        </div>
        <h1 class="text-2xl font-bold text-gray-900 dark:text-white">CineStory</h1>
        <p class="text-gray-500 dark:text-gray-400 mt-1">小说转动漫视频</p>
      </div>

      <!-- 卡片 -->
      <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-sm border border-gray-100 dark:border-gray-700 p-8">
        <!-- 切换按钮 -->
        <div class="flex mb-8 bg-gray-100 dark:bg-gray-700/50 rounded-xl p-1">
          <button
            @click="isLoginMode = true"
            :class="[
              'flex-1 py-2.5 rounded-lg text-sm font-medium transition-all duration-200',
              isLoginMode
                ? 'bg-white dark:bg-gray-600 text-gray-900 dark:text-white shadow-sm'
                : 'text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300'
            ]"
          >
            登录
          </button>
          <button
            @click="isLoginMode = false"
            :class="[
              'flex-1 py-2.5 rounded-lg text-sm font-medium transition-all duration-200',
              !isLoginMode
                ? 'bg-white dark:bg-gray-600 text-gray-900 dark:text-white shadow-sm'
                : 'text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300'
            ]"
          >
            注册
          </button>
        </div>

        <!-- 错误提示 -->
        <div v-if="errorMessage" class="mb-6 p-4 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-xl flex items-start gap-3">
          <svg class="w-5 h-5 text-red-500 dark:text-red-400 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <p class="text-sm text-red-600 dark:text-red-400">{{ errorMessage }}</p>
        </div>

        <!-- 登录表单 -->
        <form v-if="isLoginMode" @submit.prevent="handleLogin" class="space-y-5">
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">用户名</label>
            <input
              v-model="loginForm.username"
              type="text"
              required
              class="w-full px-4 py-3 border border-gray-200 dark:border-gray-600 rounded-xl focus:ring-2 focus:ring-purple-500 focus:border-transparent bg-white dark:bg-gray-700 dark:text-white transition-all placeholder:text-gray-400"
              placeholder="请输入用户名"
            >
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">密码</label>
            <input
              v-model="loginForm.password"
              type="password"
              required
              class="w-full px-4 py-3 border border-gray-200 dark:border-gray-600 rounded-xl focus:ring-2 focus:ring-purple-500 focus:border-transparent bg-white dark:bg-gray-700 dark:text-white transition-all placeholder:text-gray-400"
              placeholder="请输入密码"
            >
          </div>

          <div class="flex items-center justify-between text-sm">
            <label class="flex items-center text-gray-600 dark:text-gray-400 cursor-pointer">
              <input type="checkbox" v-model="loginForm.remember" class="mr-2 rounded border-gray-300 text-purple-600 focus:ring-purple-500">
              记住我
            </label>
            <a href="#" class="text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300 transition-colors">忘记密码？</a>
          </div>

          <button
            type="submit"
            :disabled="loading"
            class="w-full py-3 bg-purple-600 hover:bg-purple-700 text-white font-medium rounded-xl transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed border-0 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:ring-offset-2 flex items-center justify-center gap-2"
          >
            <svg v-if="loading" class="w-5 h-5 animate-spin" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
            </svg>
            <span>{{ loading ? '登录中...' : '登录' }}</span>
          </button>
        </form>

        <!-- 注册表单 -->
        <form v-else @submit.prevent="handleRegister" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">用户名</label>
            <input
              v-model="registerForm.username"
              type="text"
              required
              minlength="3"
              maxlength="50"
              pattern="[a-zA-Z0-9_]+"
              class="w-full px-4 py-3 border border-gray-200 dark:border-gray-600 rounded-xl focus:ring-2 focus:ring-purple-500 focus:border-transparent bg-white dark:bg-gray-700 dark:text-white transition-all placeholder:text-gray-400"
              placeholder="3-50位字母、数字或下划线"
            >
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">邮箱</label>
            <input
              v-model="registerForm.email"
              type="email"
              required
              class="w-full px-4 py-3 border border-gray-200 dark:border-gray-600 rounded-xl focus:ring-2 focus:ring-purple-500 focus:border-transparent bg-white dark:bg-gray-700 dark:text-white transition-all placeholder:text-gray-400"
              placeholder="请输入邮箱"
            >
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">密码</label>
            <input
              v-model="registerForm.password"
              type="password"
              required
              minlength="6"
              class="w-full px-4 py-3 border border-gray-200 dark:border-gray-600 rounded-xl focus:ring-2 focus:ring-purple-500 focus:border-transparent bg-white dark:bg-gray-700 dark:text-white transition-all placeholder:text-gray-400"
              placeholder="至少6位字符"
            >
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">确认密码</label>
            <input
              v-model="registerForm.confirmPassword"
              type="password"
              required
              class="w-full px-4 py-3 border border-gray-200 dark:border-gray-600 rounded-xl focus:ring-2 focus:ring-purple-500 focus:border-transparent bg-white dark:bg-gray-700 dark:text-white transition-all placeholder:text-gray-400"
              placeholder="请再次输入密码"
            >
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">昵称（可选）</label>
            <input
              v-model="registerForm.nickname"
              type="text"
              maxlength="50"
              class="w-full px-4 py-3 border border-gray-200 dark:border-gray-600 rounded-xl focus:ring-2 focus:ring-purple-500 focus:border-transparent bg-white dark:bg-gray-700 dark:text-white transition-all placeholder:text-gray-400"
              placeholder="显示名称"
            >
          </div>

          <div class="flex items-start text-sm pt-2">
            <input
              v-model="registerForm.agree"
              type="checkbox"
              required
              class="mt-0.5 mr-2 rounded border-gray-300 text-purple-600 focus:ring-purple-500"
            >
            <label class="text-gray-600 dark:text-gray-400">
              我已阅读并同意
              <a href="#" class="text-purple-600 hover:text-purple-700 dark:text-purple-400 font-medium">用户协议</a>
              和
              <a href="#" class="text-purple-600 hover:text-purple-700 dark:text-purple-400 font-medium">隐私政策</a>
            </label>
          </div>

          <button
            type="submit"
            :disabled="loading"
            class="w-full py-3 bg-purple-600 hover:bg-purple-700 text-white font-medium rounded-xl transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed border-0 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:ring-offset-2 flex items-center justify-center gap-2"
          >
            <svg v-if="loading" class="w-5 h-5 animate-spin" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
            </svg>
            <span>{{ loading ? '注册中...' : '注册' }}</span>
          </button>
        </form>
      </div>

      <!-- 底部提示 -->
      <p class="text-center text-sm text-gray-500 dark:text-gray-400 mt-6">
        {{ isLoginMode ? '还没有账号？' : '已有账号？' }}
        <button
          @click="isLoginMode = !isLoginMode"
          class="text-purple-600 hover:text-purple-700 dark:text-purple-400 font-semibold ml-1 transition-colors"
        >
          {{ isLoginMode ? '立即注册' : '立即登录' }}
        </button>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/useAuthStore'

const router = useRouter()
const authStore = useAuthStore()

const isLoginMode = ref(true)
const loading = ref(false)
const errorMessage = ref('')

const loginForm = ref({
  username: '',
  password: '',
  remember: false
})

const registerForm = ref({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  agree: false
})

const handleLogin = async () => {
  errorMessage.value = ''
  loading.value = true

  try {
    await authStore.login({
      username: loginForm.value.username,
      password: loginForm.value.password
    })

    // 登录成功，跳转到首页
    window.toast?.success('登录成功')
    router.push('/')
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '登录失败，请检查用户名和密码'
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  errorMessage.value = ''

  // 验证密码
  if (registerForm.value.password !== registerForm.value.confirmPassword) {
    errorMessage.value = '两次输入的密码不一致'
    return
  }

  loading.value = true

  try {
    await authStore.register({
      username: registerForm.value.username,
      email: registerForm.value.email,
      password: registerForm.value.password,
      nickname: registerForm.value.nickname || registerForm.value.username
    })

    // 注册成功会自动登录，跳转到首页
    window.toast?.success('注册成功')
    router.push('/')
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '注册失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>
