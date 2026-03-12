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
            v-if="canCancel"
            @click="cancelGeneration"
            :disabled="cancelling"
            class="btn-warning"
          >
            {{ cancelling ? '取消中...' : '⏹️ 取消任务' }}
          </button>
          <button
            v-if="canDownload"
            @click="downloadVideo"
            class="btn-secondary"
          >
            📥 下载视频
          </button>
          <button
            v-if="canPreview"
            @click="showPreview = true"
            class="btn-secondary"
          >
            🎬 预览视频
          </button>
          <button
            @click="showSlices = !showSlices"
            class="btn btn-secondary"
          >
            {{ showSlices ? '收起' : '展开' }}切片详情
          </button>
        </div>
      </div>

      <!-- 实时进度跟踪 -->
      <div v-if="isProcessing || wsProgress.isProcessing" class="card mb-6">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-xl font-semibold text-gray-800 dark:text-gray-200">生成进度</h2>
          <div v-if="wsConnected" class="flex items-center gap-2 text-sm text-green-500">
            <span class="w-2 h-2 bg-green-500 rounded-full animate-pulse"></span>
            实时连接
          </div>
        </div>

        <div class="space-y-4">
          <!-- 进度条 -->
          <div>
            <div class="flex justify-between text-sm mb-1">
              <span>总进度</span>
              <span class="font-medium">{{ displayProgress }}%</span>
            </div>
            <div class="h-4 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden relative">
              <div
                class="h-full bg-gradient-to-r from-purple-500 to-pink-500 transition-all duration-300 ease-out"
                :style="{ width: displayProgress + '%' }"
              >
                <div class="absolute inset-0 bg-white/20 animate-pulse" v-if="isProcessing"></div>
              </div>
            </div>
          </div>

          <!-- 统计信息 -->
          <div class="grid grid-cols-4 gap-4 text-center text-sm">
            <div class="p-3 bg-gray-100 dark:bg-gray-700 rounded-lg">
              <div class="text-2xl font-bold text-purple-600 dark:text-purple-400">{{ wsProgress.stats.processed || project.processedSlices || 0 }}</div>
              <div class="text-gray-500">已处理</div>
            </div>
            <div class="p-3 bg-gray-100 dark:bg-gray-700 rounded-lg">
              <div class="text-2xl font-bold text-green-600 dark:text-green-400">{{ wsProgress.stats.succeeded || project.succeededSlices || 0 }}</div>
              <div class="text-gray-500">成功</div>
            </div>
            <div class="p-3 bg-gray-100 dark:bg-gray-700 rounded-lg">
              <div class="text-2xl font-bold text-red-600 dark:text-red-400">{{ wsProgress.stats.failed || project.failedSlices || 0 }}</div>
              <div class="text-gray-500">失败</div>
            </div>
            <div class="p-3 bg-gray-100 dark:bg-gray-700 rounded-lg">
              <div class="text-2xl font-bold text-gray-600 dark:text-gray-400">{{ wsProgress.stats.total || project.totalSlices || 0 }}</div>
              <div class="text-gray-500">总数</div>
            </div>
          </div>

          <!-- 当前步骤 -->
          <div class="p-4 bg-blue-50 dark:bg-blue-900/20 rounded-lg border border-blue-200 dark:border-blue-800">
            <div class="flex items-center gap-2">
              <div v-if="isProcessing" class="w-4 h-4 border-2 border-blue-500 border-t-transparent rounded-full animate-spin"></div>
              <span class="font-medium text-blue-700 dark:text-blue-300">{{ displayStep }}</span>
            </div>
          </div>

          <!-- 错误信息 -->
          <div v-if="wsProgress.hasError || project.status === 'FAILED'" class="p-4 bg-red-50 dark:bg-red-900/20 rounded-lg border border-red-200 dark:border-red-800">
            <div class="text-red-700 dark:text-red-300">
              <div class="font-medium mb-1">⚠️ 处理失败</div>
              <div class="text-sm">{{ wsProgress.error || project.errorMessage }}</div>
            </div>
          </div>

          <!-- 完成提示 -->
          <div v-if="wsProgress.isCompleted || project.status === 'COMPLETED'" class="p-4 bg-green-50 dark:bg-green-900/20 rounded-lg border border-green-200 dark:border-green-800">
            <div class="text-green-700 dark:text-green-300">
              <div class="font-medium mb-1">✅ 任务完成</div>
              <div class="text-sm">视频已生成完成，可以下载或预览</div>
            </div>
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

    <!-- 视频预览弹窗 -->
    <VideoPreview
      v-if="showPreview"
      :video-url="project?.outputVideoUrl"
      @close="showPreview = false"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useProjectStore } from '@/store/useProjectStore'
import { useTaskProgress } from '@/composables/useTaskProgress'
import VideoPreview from '@/components/VideoPreview.vue'

const router = useRouter()
const route = useRoute()
const projectStore = useProjectStore()

const loading = ref(true)
const starting = ref(false)
const cancelling = ref(false)
const showSlices = ref(false)
const showPreview = ref(false)
const project = ref(null)
const slices = ref([])

// WebSocket 进度
const wsProgress = useTaskProgress(null)
const wsConnected = ref(false)

const sliceCount = computed(() => slices.value.length)
const canStart = computed(() => {
  return project.value && project.value.status === 'DRAFT'
})
const canCancel = computed(() => {
  return project.value && project.value.status === 'PROCESSING'
})
const canDownload = computed(() => {
  return project.value && project.value.status === 'COMPLETED'
})
const canPreview = computed(() => {
  return project.value && (project.value.status === 'COMPLETED' || project.value.outputVideoUrl)
})
const isProcessing = computed(() => {
  return project.value?.status === 'PROCESSING'
})

// 显示的进度（优先使用 WebSocket 实时进度）
const displayProgress = computed(() => {
  return wsProgress.progress.value || project.value?.progress || 0
})

// 显示的步骤（优先使用 WebSocket 实时步骤）
const displayStep = computed(() => {
  return wsProgress.currentStep.value || project.value?.currentStep || '准备中...'
})

// 监听 WebSocket 连接状态
wsProgress.on?.('connected', () => {
  wsConnected.value = true
})

wsProgress.on?.('disconnected', () => {
  wsConnected.value = false
})

onMounted(async () => {
  const id = route.params.id
  try {
    project.value = await projectStore.fetchProject(id)

    // 如果正在处理，启动 WebSocket 监听
    if (project.value.status === 'PROCESSING') {
      wsProgress.projectId = id
      wsProgress.startListening?.()
    }

    // 加载切片列表
    // slices.value = await projectStore.fetchSlices(id)
    slices.value = []
  } catch (err) {
    alert('加载失败：' + err.message)
  } finally {
    loading.value = false
  }
})

onUnmounted(() => {
  wsProgress.stopListening?.()
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

    // 启动 WebSocket 监听
    wsProgress.projectId = project.value.id
    wsProgress.startListening?.()

    // 刷新项目状态
    project.value = await projectStore.fetchProject(project.value.id)
  } catch (err) {
    alert('启动失败：' + err.message)
  } finally {
    starting.value = false
  }
}

const cancelGeneration = async () => {
  if (!confirm('确定要取消当前任务吗？')) return

  cancelling.value = true
  try {
    await projectStore.cancelTask(project.value.id)
    project.value = await projectStore.fetchProject(project.value.id)
    wsProgress.stopListening?.()
  } catch (err) {
    alert('取消失败：' + err.message)
  } finally {
    cancelling.value = false
  }
}

const downloadVideo = () => {
  if (project.value?.outputVideoUrl) {
    window.open(project.value.outputVideoUrl, '_blank')
  } else {
    alert('视频文件不可用')
  }
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
