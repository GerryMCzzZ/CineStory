<template>
  <Teleport to="body">
    <TransitionGroup
      tag="div"
      name="toast"
      class="fixed top-4 right-4 z-50 flex flex-col gap-2 pointer-events-none"
    >
      <div
        v-for="toast in toasts"
        :key="toast.id"
        :class="[
          'pointer-events-auto max-w-sm w-full shadow-lg rounded-lg p-4 flex items-start gap-3',
          toastClasses[toast.type]
        ]"
      >
        <!-- 图标 -->
        <div class="flex-shrink-0">
          <svg v-if="toast.type === 'success'" class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <svg v-else-if="toast.type === 'error'" class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <svg v-else-if="toast.type === 'warning'" class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
          </svg>
          <svg v-else class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </div>

        <!-- 内容 -->
        <div class="flex-1 min-w-0">
          <p v-if="toast.title" class="font-medium text-sm">{{ toast.title }}</p>
          <p class="text-sm" :class="toast.title ? 'mt-1' : ''">{{ toast.message }}</p>
        </div>

        <!-- 关闭按钮 -->
        <button
          @click="removeToast(toast.id)"
          class="flex-shrink-0 opacity-70 hover:opacity-100 transition-opacity"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>

        <!-- 进度条 -->
        <div
          v-if="toast.duration > 0"
          class="absolute bottom-0 left-0 h-1 bg-current opacity-30 transition-all"
          :style="{ width: toast.progress + '%' }"
        ></div>
      </div>
    </TransitionGroup>
  </Teleport>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const toasts = ref([])
let toastId = 0
let progressIntervals = new Map()

const toastClasses = {
  success: 'bg-green-500 text-white',
  error: 'bg-red-500 text-white',
  warning: 'bg-yellow-500 text-white',
  info: 'bg-blue-500 text-white'
}

function addToast({ message, title = '', type = 'info', duration = 3000 }) {
  const id = ++toastId
  const toast = {
    id,
    message,
    title,
    type,
    duration,
    progress: 100
  }

  toasts.value.push(toast)

  if (duration > 0) {
    const interval = 50
    const steps = duration / interval
    let currentStep = 0

    const progressInterval = setInterval(() => {
      currentStep++
      toast.progress = ((steps - currentStep) / steps) * 100

      if (currentStep >= steps) {
        clearInterval(progressInterval)
        removeToast(id)
      }
    }, interval)

    progressIntervals.set(id, progressInterval)
  }

  return id
}

function removeToast(id) {
  const index = toasts.value.findIndex(t => t.id === id)
  if (index > -1) {
    toasts.value.splice(index, 1)
  }

  const interval = progressIntervals.get(id)
  if (interval) {
    clearInterval(interval)
    progressIntervals.delete(id)
  }
}

function clearAll() {
  toasts.value.forEach(toast => {
    const interval = progressIntervals.get(toast.id)
    if (interval) {
      clearInterval(interval)
    }
  })
  toasts.value = []
  progressIntervals.clear()
}

// 暴露方法给全局使用
const toast = {
  success: (message, title = '成功') => addToast({ message, title, type: 'success' }),
  error: (message, title = '错误') => addToast({ message, title, type: 'error', duration: 5000 }),
  warning: (message, title = '警告') => addToast({ message, title, type: 'warning' }),
  info: (message, title = '') => addToast({ message, title, type: 'info' }),
  clear: clearAll
}

// 挂载到全局
window.toast = toast

defineExpose({
  addToast,
  removeToast,
  clearAll,
  toast
})
</script>

<style scoped>
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from {
  opacity: 0;
  transform: translateX(100%);
}

.toast-leave-to {
  opacity: 0;
  transform: translateX(100%) scale(0.9);
}

.toast-move {
  transition: transform 0.3s ease;
}
</style>
