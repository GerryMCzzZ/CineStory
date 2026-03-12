/**
 * 任务进度 composable
 * 用于监听和显示任务进度
 */
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useStomp } from '@/utils/stomp'

export function useTaskProgress(projectId) {
  const { client, subscribeProgress, unsubscribe } = useStomp()

  // 响应式状态
  const progress = ref(0)
  const currentStep = ref('')
  const status = ref('')
  const error = ref(null)
  const stats = ref({
    total: 0,
    processed: 0,
    succeeded: 0,
    failed: 0
  })

  let subscription = null

  /**
   * 处理进度消息
   */
  const handleProgressMessage = (message) => {
    if (message.projectId !== projectId) return

    progress.value = message.progress || 0
    currentStep.value = message.currentStep || ''
    status.value = message.status || ''

    if (message.total !== undefined) {
      stats.value.total = message.total
      stats.value.processed = message.processed || 0
      stats.value.succeeded = message.succeeded || 0
      stats.value.failed = message.failed || 0
    }

    // 处理完成状态
    if (message.status === 'COMPLETED') {
      progress.value = 100
      currentStep.value = '已完成'
    }

    // 处理失败状态
    if (message.status === 'FAILED') {
      error.value = message.errorMessage || '任务失败'
    }
  }

  /**
   * 开始监听进度
   */
  const startListening = () => {
    if (subscription) return

    subscription = subscribeProgress(projectId, handleProgressMessage)
  }

  /**
   * 停止监听进度
   */
  const stopListening = () => {
    if (subscription) {
      unsubscribe(`/topic/progress/${projectId}`)
      subscription = null
    }
  }

  /**
   * 重置状态
   */
  const reset = () => {
    progress.value = 0
    currentStep.value = ''
    status.value = ''
    error.value = null
    stats.value = {
      total: 0,
      processed: 0,
      succeeded: 0,
      failed: 0
    }
  }

  // 计算属性
  const isProcessing = computed(() => status.value === 'PROCESSING' || status.value === 'STARTED')
  const isCompleted = computed(() => status.value === 'COMPLETED')
  const isFailed = computed(() => status.value === 'FAILED')
  const hasError = computed(() => error.value !== null)

  // 生命周期钩子
  onMounted(() => {
    // 确保客户端已连接
    if (!client.connected) {
      client.connect().then(() => {
        startListening()
      })
    } else {
      startListening()
    }
  })

  onUnmounted(() => {
    stopListening()
  })

  return {
    // 状态
    progress,
    currentStep,
    status,
    error,
    stats,

    // 计算属性
    isProcessing,
    isCompleted,
    isFailed,
    hasError,

    // 方法
    startListening,
    stopListening,
    reset
  }
}
