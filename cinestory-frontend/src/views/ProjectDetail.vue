<template>
  <div class="project-detail">
    <!-- 返回按钮 -->
    <button @click="goBack" class="flex items-center gap-2 text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-200 mb-6">
      ← 返回列表
    </button>

    <!-- 加载状态 -->
    <div v-if="loading" class="text-center py-12">
      <div class="inline-block w-8 h-8 border-4 border-purple-600 border-t-transparent rounded-full animate-spin"></div>
    </div>

    <!-- 项目详情 -->
    <div v-else-if="project">
      <!-- 头部 -->
      <div class="card mb-6">
        <div class="flex justify-between items-start">
          <div>
            <h1 class="text-3xl font-bold text-gray-800 dark:text-gray-200 mb-2">
              {{ project.name }}
            </h1>
            <p class="text-gray-600 dark:text-gray-400">{{ project.description }}</p>
          </div>
          <div class="flex items-center gap-2">
            <span :class="getStatusClass(project.status)" class="px-3 py-1 rounded-full text-sm font-medium">
              {{ getStatusText(project.status) }}
            </span>
            <button @click="deleteProject" class="p-2 text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-lg">
              🗑️
            </button>
          </div>
        </div>

        <!-- 项目信息 -->
        <div class="grid grid-cols-4 gap-4 mt-6 pt-6 border-t border-gray-200 dark:border-gray-700">
          <div>
            <div class="text-sm text-gray-500">创建时间</div>
            <div class="font-medium">{{ formatDate(project.createdAt) }}</div>
          </div>
          <div>
            <div class="text-sm text-gray-500">总字数</div>
            <div class="font-medium">{{ project.totalCharacters || 0 }} 字</div>
          </div>
          <div>
            <div class="text-sm text-gray-500">视频时长</div>
            <div class="font-medium">{{ project.totalDuration || 0 }} 秒</div>
          </div>
          <div>
            <div class="text-sm text-gray-500">切片数量</div>
            <div class="font-medium">{{ sliceCount }} 个</div>
          </div>
        </div>
      </div>

      <!-- 操作区域 -->
      <div class="card mb-6">
        <h2 class="text-xl font-semibold mb-4 text-gray-800 dark:text-gray-200">操作</h2>
        <div class="flex gap-4">
          <button
            v-if="canStart"
            @click="startGeneration"
            :disabled="starting"
            class="btn-primary"
          >
            {{ starting ? '启动中...' : '🚀 开始生成视频' }}
          </button>
          <button
            v-if="canDownload"
            @click="downloadVideo"
            class="btn-secondary"
          >
            📥 下载视频
          </button>
          <button
            @click="showSlices = !showSlices"
            class="btn btn-secondary"
          >
            {{ showSlices ? '收起' : '展开' }}切片详情
          </button>
        </div>
      </div>

      <!-- 进度跟踪 -->
      <div v-if="project.status === 'PROCESSING'" class="card mb-6">
        <h2 class="text-xl font-semibold mb-4 text-gray-800 dark:text-gray-200">生成进度</h2>
        <div class="space-y-4">
          <div>
            <div class="flex justify-between text-sm mb-1">
              <span>总进度</span>
              <span>{{ project.progress || 0 }}%</span>
            </div>
            <div class="h-3 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden">
              <div
                class="h-full bg-gradient-primary progress-bar-animated"
                :style="{ width: (project.progress || 0) + '%' }"
              ></div>
            </div>
          </div>

          <div class="grid grid-cols-3 gap-4 text-center text-sm">
            <div class="p-3 bg-gray-100 dark:bg-gray-700 rounded-lg">
              <div class="font-medium">{{ progress.completed || 0 }}</div>
              <div class="text-gray-500">已完成</div>
            </div>
            <div class="p-3 bg-gray-100 dark:bg-gray-700 rounded-lg">
              <div class="font-medium">{{ progress.total || 0 }}</div>
              <div class="text-gray-500">总数</div>
            </div>
            <div class="p-3 bg-gray-100 dark:bg-gray-700 rounded-lg">
              <div class="font-medium">{{ progress.failed || 0 }}</div>
              <div class="text-gray-500">失败</div>
            </div>
          </div>

          <!-- 当前任务 -->
          <div v-if="currentStep" class="text-sm text-gray-600 dark:text-gray-400">
            当前: {{ currentStep }}
          </div>
        </div>
      </div>

      <!-- 切片列表 -->
      <div v-if="showSlices" class="card">
        <h2 class="text-xl font-semibold mb-4 text-gray-800 dark:text-gray-200">
          文本切片 ({{ slices.length }} 个)
        </h2>
        <div class="space-y-3 max-h-96 overflow-y-auto">
          <div
            v-for="(slice, index) in slices"
            :key="slice.id"
            class="p-3 bg-gray-50 dark:bg-gray-700/50 rounded-lg"
          >
            <div class="flex items-center gap-2 mb-2">
              <span class="text-xs font-medium px-2 py-0.5 bg-purple-100 dark:bg-purple-900/30 text-purple-600 dark:text-purple-400 rounded">
                #{{ index + 1 }}
              </span>
              <span class="text-xs text-gray-500">{{ getSceneTypeText(slice.sceneType) }}</span>
              <span class="text-xs text-gray-400">{{ slice.characterCount }} 字</span>
            </div>
            <p class="text-sm text-gray-700 dark:text-gray-300 line-clamp-2">
              {{ slice.content }}
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useProjectStore } from '@/store/useProjectStore'

const router = useRouter()
const route = useRoute()
const projectStore = useProjectStore()

const loading = ref(true)
const starting = ref(false)
const showSlices = ref(false)
const project = ref(null)
const slices = ref([])
const progress = ref({})

const sliceCount = computed(() => slices.value.length)
const canStart = computed(() => {
  return project.value && project.value.status === 'DRAFT'
})
const canDownload = computed(() => {
  return project.value && project.value.status === 'COMPLETED'
})
const currentStep = computed(() => {
  return project.value?.currentStep
})

onMounted(async () => {
  const id = route.params.id
  try {
    project.value = await projectStore.fetchProject(id)
    // TODO: 加载切片列表
    slices.value = []
  } catch (err) {
    alert('加载失败：' + err.message)
  } finally {
    loading.value = false
  }
})

const goBack = () => {
  router.push('/projects')
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

const getSceneTypeText = (type) => {
  const types = {
    DIALOGUE: '对话',
    DESCRIPTION: '描写',
    ACTION: '动作',
    TRANSITION: '转场',
    MONOLOGUE: '独白',
    NARRATION: '旁白'
  }
  return types[type] || type
}

const formatDate = (date) => {
  if (!date) return '-'
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const startGeneration = async () => {
  starting.value = true
  try {
    await projectStore.startTask(project.value.id, {})
    alert('任务已启动，请关注进度')
    // TODO: 连接WebSocket获取实时进度
  } catch (err) {
    alert('启动失败：' + err.message)
  } finally {
    starting.value = false
  }
}

const downloadVideo = () => {
  // TODO: 实现视频下载
  alert('下载功能开发中')
}

const deleteProject = async () => {
  if (!confirm('确定要删除这个项目吗？')) return
  try {
    await projectStore.deleteProject(project.value.id)
    router.push('/projects')
  } catch (err) {
    alert('删除失败：' + err.message)
  }
}
</script>
