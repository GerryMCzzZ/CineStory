<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="show" class="fixed inset-0 z-50 flex items-center justify-center" @click.self="close">
        <!-- 遮罩 -->
        <div class="absolute inset-0 bg-black/70 backdrop-blur-sm"></div>

        <!-- 内容 -->
        <div class="relative bg-gray-900 dark:bg-black rounded-xl shadow-2xl w-full max-w-4xl mx-4 overflow-hidden">
          <!-- 关闭按钮 -->
          <button
            @click="close"
            class="absolute top-4 right-4 z-10 w-10 h-10 flex items-center justify-center bg-black/50 hover:bg-black/70 rounded-full text-white transition-colors"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>

          <!-- 视频播放器 -->
          <div class="aspect-video bg-black">
            <video
              v-if="videoUrl"
              ref="videoRef"
              :src="videoUrl"
              controls
              controlsList="nodownload"
              class="w-full h-full"
              @play="isPlaying = true"
              @pause="isPlaying = false"
              @ended="isPlaying = false"
              @timeupdate="handleTimeUpdate"
              @loadedmetadata="handleLoadedMetadata"
            >
              您的浏览器不支持视频播放
            </video>

            <!-- 加载状态 -->
            <div v-else class="w-full h-full flex items-center justify-center">
              <div class="text-center">
                <div class="w-16 h-16 border-4 border-purple-500 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
                <p class="text-gray-400">加载中...</p>
              </div>
            </div>
          </div>

          <!-- 视频信息 -->
          <div class="p-4 bg-gray-800">
            <div class="flex items-center justify-between">
              <div>
                <h3 class="text-white font-medium">视频预览</h3>
                <p class="text-gray-400 text-sm mt-1">
                  <span v-if="duration > 0">{{ formatTime(duration) }}</span>
                  <span v-if="currentTime > 0"> / {{ formatTime(currentTime) }}</span>
                </p>
              </div>

              <!-- 播放控制 -->
              <div class="flex items-center gap-2">
                <button
                  @click="togglePlay"
                  class="w-12 h-12 flex items-center justify-center bg-purple-600 hover:bg-purple-700 rounded-full text-white transition-colors"
                >
                  <svg v-if="!isPlaying" xmlns="http://www.w3.org/2000/svg" class="w-6 h-6" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M8 5v14l11-7z"/>
                  </svg>
                  <svg v-else xmlns="http://www.w3.org/2000/svg" class="w-6 h-6" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M6 4h4v16H6V4zm8 0h4v16h-4V4z"/>
                  </svg>
                </button>

                <!-- 下载按钮 -->
                <a
                  v-if="videoUrl"
                  :href="videoUrl"
                  download
                  class="w-10 h-10 flex items-center justify-center bg-gray-700 hover:bg-gray-600 rounded-full text-white transition-colors"
                  title="下载视频"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
                  </svg>
                </a>
              </div>
            </div>

            <!-- 进度条 -->
            <div v-if="duration > 0" class="mt-4">
              <div class="h-1 bg-gray-700 rounded-full overflow-hidden cursor-pointer" @click="seek">
                <div
                  class="h-full bg-purple-600 transition-all"
                  :style="{ width: (currentTime / duration * 100) + '%' }"
                ></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, watch, nextTick, computed } from 'vue'

const props = defineProps({
  videoUrl: {
    type: String,
    default: ''
  }
})

const show = computed(() => !!props.videoUrl)

const emit = defineEmits(['close'])

const videoRef = ref(null)
const isPlaying = ref(false)
const currentTime = ref(0)
const duration = ref(0)

const close = () => {
  if (videoRef.value) {
    videoRef.value.pause()
  }
  emit('close')
}

const togglePlay = () => {
  if (!videoRef.value) return
  if (isPlaying.value) {
    videoRef.value.pause()
  } else {
    videoRef.value.play()
  }
}

const handleTimeUpdate = () => {
  if (videoRef.value) {
    currentTime.value = videoRef.value.currentTime
  }
}

const handleLoadedMetadata = () => {
  if (videoRef.value) {
    duration.value = videoRef.value.duration
  }
}

const seek = (event) => {
  if (!videoRef.value) return
  const rect = event.currentTarget.getBoundingClientRect()
  const percent = (event.clientX - rect.left) / rect.width
  videoRef.value.currentTime = percent * duration.value
}

const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins}:${secs.toString().padStart(2, '0')}`
}

// 监听显示状态变化
watch(() => props.videoUrl, async (newUrl) => {
  if (newUrl) {
    await nextTick()
    if (videoRef.value) {
      videoRef.value.load()
      // 自动播放
      videoRef.value.play().catch(() => {
        // 自动播放可能被浏览器阻止
      })
    }
  }
})
</script>

<style scoped>
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-active .relative,
.modal-leave-active .relative {
  transition: transform 0.3s ease, opacity 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .relative,
.modal-leave-to .relative {
  transform: scale(0.95);
  opacity: 0;
}
</style>
