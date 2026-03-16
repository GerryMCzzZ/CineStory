<template>
  <div class="projects">
    <div class="flex justify-between items-center mb-8">
      <h1 class="text-3xl font-bold text-gray-900 dark:text-white">我的项目</h1>
      <router-link to="/projects/create" class="px-5 py-2.5 bg-purple-600 hover:bg-purple-700 text-white rounded-xl transition-all duration-200 font-medium flex items-center gap-2">
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
        </svg>
        新建项目
      </router-link>
    </div>

    <!-- 状态统计 -->
    <div class="grid grid-cols-4 gap-4 mb-8">
      <div class="card">
        <div class="text-3xl font-bold text-gray-800 dark:text-gray-200">{{ projectStore.projectCount }}</div>
        <div class="text-gray-600 dark:text-gray-400 text-sm">全部项目</div>
      </div>
      <div class="card">
        <div class="text-3xl font-bold text-blue-600">{{ projectStore.processingCount }}</div>
        <div class="text-gray-600 dark:text-gray-400 text-sm">处理中</div>
      </div>
      <div class="card">
        <div class="text-3xl font-bold text-green-600">{{ projectStore.completedCount }}</div>
        <div class="text-gray-600 dark:text-gray-400 text-sm">已完成</div>
      </div>
      <div class="card">
        <div class="text-3xl font-bold text-gray-600 dark:text-gray-400">
          {{ projectStore.projectCount - projectStore.processingCount - projectStore.completedCount }}
        </div>
        <div class="text-gray-600 dark:text-gray-400 text-sm">草稿/失败</div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="projectStore.loading" class="text-center py-12">
      <div class="inline-block w-8 h-8 border-4 border-purple-600 border-t-transparent rounded-full animate-spin"></div>
      <p class="mt-4 text-gray-600 dark:text-gray-400">加载中...</p>
    </div>

    <!-- 项目列表 -->
    <div v-else-if="projectList.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div
        v-for="project in projectList"
        :key="project.id"
        class="card hover:shadow-md transition-shadow cursor-pointer"
        @click="goToDetail(project.id)"
      >
        <div class="flex items-start justify-between mb-4">
          <h3 class="text-lg font-semibold text-gray-800 dark:text-gray-200 truncate">
            {{ project.name }}
          </h3>
          <span :class="getStatusClass(project.status)" class="px-2 py-1 rounded text-xs font-medium">
            {{ getStatusText(project.status) }}
          </span>
        </div>

        <p class="text-gray-600 dark:text-gray-400 text-sm mb-4 line-clamp-2">
          {{ project.description || '暂无描述' }}
        </p>

        <div class="flex items-center justify-between text-sm text-gray-500 dark:text-gray-500">
          <span>{{ formatDate(project.createdAt) }}</span>
          <span v-if="project.totalDuration">{{ project.totalDuration }}秒</span>
        </div>

        <!-- 进度条 -->
        <div v-if="project.status === 'PROCESSING'" class="mt-4">
          <div class="flex justify-between text-xs text-gray-500 mb-1">
            <span>生成进度</span>
            <span>{{ project.progress }}%</span>
          </div>
          <div class="h-2 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden">
            <div
              class="h-full bg-gradient-primary progress-bar-animated"
              :style="{ width: project.progress + '%' }"
            ></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="text-center py-16">
      <div class="text-6xl mb-4">📁</div>
      <h3 class="text-xl font-semibold text-gray-900 dark:text-white mb-2">暂无项目</h3>
      <p class="text-gray-500 dark:text-gray-400 mb-6">创建你的第一个项目开始创作吧</p>
      <router-link to="/projects/create" class="inline-flex items-center gap-2 px-6 py-3 bg-purple-600 hover:bg-purple-700 text-white rounded-xl transition-all duration-200 font-medium">
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
        </svg>
        新建项目
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useProjectStore } from '@/store/useProjectStore'

const router = useRouter()
const projectStore = useProjectStore()

const projectList = computed(() => projectStore.projects || [])

onMounted(() => {
  projectStore.fetchProjects()
})

const goToDetail = (id) => {
  router.push(`/projects/${id}`)
}

const getStatusClass = (status) => {
  const classes = {
    DRAFT: 'bg-gray-100 text-gray-600 dark:bg-gray-700 dark:text-gray-400',
    PROCESSING: 'bg-blue-100 text-blue-600 dark:bg-blue-900 dark:text-blue-400',
    COMPLETED: 'bg-green-100 text-green-600 dark:bg-green-900 dark:text-green-400',
    FAILED: 'bg-red-100 text-red-600 dark:bg-red-900 dark:text-red-400',
    CANCELLED: 'bg-gray-100 text-gray-600 dark:bg-gray-700 dark:text-gray-400'
  }
  return classes[status] || classes.DRAFT
}

const getStatusText = (status) => {
  const texts = {
    DRAFT: '草稿',
    PROCESSING: '处理中',
    COMPLETED: '已完成',
    FAILED: '失败',
    CANCELLED: '已取消'
  }
  return texts[status] || status
}

const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getMonth() + 1}月${d.getDate()}日`
}
</script>
