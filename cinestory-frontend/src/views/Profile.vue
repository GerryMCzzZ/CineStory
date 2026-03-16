<template>
  <div class="max-w-4xl mx-auto">
    <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-8">个人中心</h1>

    <!-- 用户信息卡片 -->
    <div class="card mb-6">
      <div class="flex items-center gap-6 mb-6">
        <div class="w-20 h-20 bg-gradient-to-br from-purple-500 to-pink-500 rounded-full flex items-center justify-center">
          <span class="text-white text-2xl font-bold">{{ authStore.nickname?.charAt(0).toUpperCase() || 'U' }}</span>
        </div>
        <div>
          <h2 class="text-2xl font-bold text-gray-900 dark:text-white">{{ authStore.nickname }}</h2>
          <p class="text-gray-600 dark:text-gray-400">@{{ authStore.username }}</p>
          <div class="flex gap-2 mt-2">
            <span v-if="authStore.isAdmin" class="px-2 py-1 bg-red-100 dark:bg-red-900 text-red-600 dark:text-red-400 text-xs rounded-full">管理员</span>
            <span v-else-if="authStore.isVip" class="px-2 py-1 bg-yellow-100 dark:bg-yellow-900 text-yellow-600 dark:text-yellow-400 text-xs rounded-full">VIP</span>
            <span v-else class="px-2 py-1 bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-400 text-xs rounded-full">普通用户</span>
          </div>
        </div>
      </div>

      <!-- 配额信息 -->
      <div class="bg-gray-50 dark:bg-gray-800 rounded-lg p-4">
        <div class="flex justify-between items-center mb-2">
          <span class="text-gray-700 dark:text-gray-300">本月配额</span>
          <span class="text-gray-900 dark:text-white font-medium">{{ authStore.quota.used }} / {{ authStore.quota.total }}</span>
        </div>
        <div class="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2">
          <div
            class="bg-gradient-to-r from-purple-500 to-pink-500 h-2 rounded-full transition-all"
            :style="{ width: `${Math.min(100, (authStore.quota.used / authStore.quota.total) * 100)}%` }"
          ></div>
        </div>
        <p v-if="authStore.isQuotaExceeded" class="text-red-600 dark:text-red-400 text-sm mt-2">配额已用完，请升级账户或等待下月重置</p>
      </div>
    </div>

    <!-- 标签页 -->
    <div class="mb-6">
      <div class="flex gap-2 border-b border-gray-200 dark:border-gray-700">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          @click="activeTab = tab.key"
          :class="[
            'px-4 py-2 text-sm font-medium transition-colors',
            activeTab === tab.key
              ? 'text-purple-600 dark:text-purple-400 border-b-2 border-purple-600 dark:border-purple-400'
              : 'text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white'
          ]"
        >
          {{ tab.label }}
        </button>
      </div>
    </div>

    <!-- 基本信息标签 -->
    <div v-if="activeTab === 'profile'" class="card">
      <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">基本信息</h3>
      <form @submit.prevent="handleUpdateProfile" class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">用户名</label>
          <input
            v-model="profileForm.username"
            type="text"
            disabled
            class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-gray-100 dark:bg-gray-700 text-gray-500 dark:text-gray-400 cursor-not-allowed"
          >
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">邮箱</label>
          <input
            v-model="profileForm.email"
            type="email"
            disabled
            class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-gray-100 dark:bg-gray-700 text-gray-500 dark:text-gray-400 cursor-not-allowed"
          >
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">昵称</label>
          <input
            v-model="profileForm.nickname"
            type="text"
            maxlength="50"
            class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent bg-white dark:bg-gray-700 dark:text-white"
          >
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">个人简介</label>
          <textarea
            v-model="profileForm.bio"
            rows="3"
            maxlength="200"
            class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent bg-white dark:bg-gray-700 dark:text-white resize-none"
          ></textarea>
        </div>
        <button
          type="submit"
          :disabled="loading"
          class="px-6 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
        >
          {{ loading ? '保存中...' : '保存修改' }}
        </button>
      </form>

      <!-- 消息提示 -->
      <div v-if="message" class="mt-4 p-3 rounded-lg" :class="messageType === 'success' ? 'bg-green-50 dark:bg-green-900/20 text-green-600 dark:text-green-400' : 'bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400'">
        {{ message }}
      </div>
    </div>

    <!-- 修改密码标签 -->
    <div v-if="activeTab === 'password'" class="card">
      <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">修改密码</h3>
      <form @submit.prevent="handleUpdatePassword" class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">当前密码</label>
          <input
            v-model="passwordForm.oldPassword"
            type="password"
            required
            class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent bg-white dark:bg-gray-700 dark:text-white"
          >
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">新密码</label>
          <input
            v-model="passwordForm.newPassword"
            type="password"
            required
            minlength="6"
            class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent bg-white dark:bg-gray-700 dark:text-white"
          >
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">确认新密码</label>
          <input
            v-model="passwordForm.confirmPassword"
            type="password"
            required
            class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent bg-white dark:bg-gray-700 dark:text-white"
          >
        </div>
        <button
          type="submit"
          :disabled="loading"
          class="px-6 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
        >
          {{ loading ? '修改中...' : '修改密码' }}
        </button>
      </form>

      <!-- 消息提示 -->
      <div v-if="message" class="mt-4 p-3 rounded-lg" :class="messageType === 'success' ? 'bg-green-50 dark:bg-green-900/20 text-green-600 dark:text-green-400' : 'bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400'">
        {{ message }}
      </div>
    </div>

    <!-- API Key 标签 -->
    <div v-if="activeTab === 'apikey'" class="card">
      <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">API Key</h3>
      <p class="text-gray-600 dark:text-gray-400 text-sm mb-4">
        API Key 用于调用 CineStory API。请妥善保管，不要泄露给他人。
      </p>

      <div class="space-y-4">
        <!-- 状态开关 -->
        <div class="flex items-center justify-between p-4 bg-gray-50 dark:bg-gray-800 rounded-lg">
          <div>
            <p class="font-medium text-gray-900 dark:text-white">API Key 状态</p>
            <p class="text-sm text-gray-600 dark:text-gray-400">{{ apiKeyEnabled ? '已启用' : '已禁用' }}</p>
          </div>
          <button
            @click="handleToggleApiKey"
            :class="[
              'px-4 py-2 rounded-lg transition-colors',
              apiKeyEnabled
                ? 'bg-red-600 text-white hover:bg-red-700'
                : 'bg-green-600 text-white hover:bg-green-700'
            ]"
          >
            {{ apiKeyEnabled ? '禁用' : '启用' }}
          </button>
        </div>

        <!-- API Key 显示 -->
        <div class="p-4 bg-gray-50 dark:bg-gray-800 rounded-lg">
          <p class="font-medium text-gray-900 dark:text-white mb-2">当前 API Key</p>
          <div class="flex gap-2">
            <input
              :value="showApiKey ? apiKey : '••••••••••••••••'"
              readonly
              class="flex-1 px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 dark:text-white font-mono text-sm"
            >
            <button
              @click="showApiKey = !showApiKey"
              class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-700 dark:text-gray-300 transition-colors"
            >
              {{ showApiKey ? '隐藏' : '显示' }}
            </button>
            <button
              v-if="showApiKey"
              @click="copyApiKey"
              class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-700 dark:text-gray-300 transition-colors"
            >
              复制
            </button>
          </div>
        </div>

        <!-- 重新生成 -->
        <div class="p-4 border border-yellow-200 dark:border-yellow-800 bg-yellow-50 dark:bg-yellow-900/20 rounded-lg">
          <p class="text-yellow-800 dark:text-yellow-200 font-medium mb-2">重新生成 API Key</p>
          <p class="text-yellow-700 dark:text-yellow-300 text-sm mb-3">
            重新生成后，旧的 API Key 将立即失效，请确保更新所有使用旧 Key 的代码。
          </p>
          <button
            @click="handleRegenerateApiKey"
            :disabled="loading"
            class="px-4 py-2 bg-yellow-600 text-white rounded-lg hover:bg-yellow-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            {{ loading ? '生成中...' : '重新生成 API Key' }}
          </button>
        </div>
      </div>

      <!-- 消息提示 -->
      <div v-if="message" class="mt-4 p-3 rounded-lg" :class="messageType === 'success' ? 'bg-green-50 dark:bg-green-900/20 text-green-600 dark:text-green-400' : 'bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400'">
        {{ message }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useAuthStore } from '@/store/useAuthStore'
import { updateProfile, updatePassword, regenerateApiKey, toggleApiKey } from '@/api/auth'
import { confirmDialog } from '@/composables/useConfirm'

const authStore = useAuthStore()

const tabs = [
  { key: 'profile', label: '基本信息' },
  { key: 'password', label: '修改密码' },
  { key: 'apikey', label: 'API Key' }
]

const activeTab = ref('profile')
const loading = ref(false)
const message = ref('')
const messageType = ref('success')

const profileForm = ref({
  username: '',
  email: '',
  nickname: '',
  bio: ''
})

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const apiKey = ref('')
const showApiKey = ref(false)
const apiKeyEnabled = ref(false)

const showMessage = (msg, type = 'success') => {
  // 使用 toast 代替本地消息
  if (type === 'success') {
    window.toast?.success(msg)
  } else if (type === 'error') {
    window.toast?.error(msg)
  } else if (type === 'warning') {
    window.toast?.warning(msg)
  } else {
    window.toast?.info(msg)
  }
  // 同时保留本地消息以兼容现有代码
  message.value = msg
  messageType.value = type
  setTimeout(() => {
    message.value = ''
  }, 3000)
}

const handleUpdateProfile = async () => {
  loading.value = true
  message.value = ''

  try {
    const updates = {}
    if (profileForm.value.nickname !== authStore.nickname) {
      updates.nickname = profileForm.value.nickname
    }
    if (profileForm.value.bio !== authStore.user?.bio) {
      updates.bio = profileForm.value.bio
    }

    if (Object.keys(updates).length === 0) {
      showMessage('没有需要修改的内容', 'info')
      return
    }

    await authStore.updateUserProfile(updates)
    showMessage('个人信息更新成功')
  } catch (error) {
    showMessage(error.response?.data?.message || '更新失败，请稍后重试', 'error')
  } finally {
    loading.value = false
  }
}

const handleUpdatePassword = async () => {
  message.value = ''

  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    showMessage('两次输入的新密码不一致', 'error')
    return
  }

  if (passwordForm.value.newPassword.length < 6) {
    showMessage('新密码至少需要6位字符', 'error')
    return
  }

  loading.value = true

  try {
    await authStore.changePassword(passwordForm.value.oldPassword, passwordForm.value.newPassword)
    showMessage('密码修改成功，请重新登录', 'success')
    passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  } catch (error) {
    showMessage(error.response?.data?.message || '密码修改失败', 'error')
  } finally {
    loading.value = false
  }
}

const handleRegenerateApiKey = async () => {
  const confirmed = await confirmDialog({
    message: '确定要重新生成 API Key 吗？',
    warning: '旧的 API Key 将立即失效，请确保更新所有使用旧 Key 的代码。',
    type: 'danger'
  })

  if (!confirmed) return

  loading.value = true
  message.value = ''

  try {
    const response = await regenerateApiKey()
    apiKey.value = response.data.apiKey
    showApiKey.value = true
    showMessage('API Key 重新生成成功', 'success')
  } catch (error) {
    showMessage(error.response?.data?.message || 'API Key 生成失败', 'error')
  } finally {
    loading.value = false
  }
}

const handleToggleApiKey = async () => {
  message.value = ''

  try {
    await toggleApiKey(!apiKeyEnabled.value)
    apiKeyEnabled.value = !apiKeyEnabled.value
    showMessage(`API Key 已${apiKeyEnabled.value ? '启用' : '禁用'}`, 'success')
  } catch (error) {
    showMessage(error.response?.data?.message || '操作失败', 'error')
  }
}

const copyApiKey = () => {
  navigator.clipboard.writeText(apiKey.value)
  showMessage('API Key 已复制到剪贴板', 'success')
}

onMounted(async () => {
  // 初始化表单数据
  await authStore.fetchCurrentUser()

  if (authStore.user) {
    profileForm.value = {
      username: authStore.user.username,
      email: authStore.user.email,
      nickname: authStore.user.nickname || '',
      bio: authStore.user.bio || ''
    }
    apiKey.value = authStore.user.apiKey || ''
    apiKeyEnabled.value = authStore.user.apiKeyEnabled || false
  }
})
</script>
