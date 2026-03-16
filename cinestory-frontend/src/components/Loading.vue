<template>
  <Teleport to="body">
    <Transition name="fade">
      <div
        v-if="show"
        class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm"
        @click.self="!lockClose && close()"
      >
        <div
          class="bg-white dark:bg-gray-800 rounded-2xl shadow-2xl p-8 flex flex-col items-center gap-4"
          :class="{ 'min-w-[300px]': message }"
        >
          <!-- 加载动画 -->
          <div class="relative">
            <div class="w-16 h-16 border-4 border-purple-200 dark:border-purple-900 rounded-full"></div>
            <div class="absolute top-0 left-0 w-16 h-16 border-4 border-transparent border-t-purple-600 rounded-full animate-spin"></div>
          </div>

          <!-- 消息 -->
          <p v-if="message" class="text-gray-700 dark:text-gray-300 text-center font-medium">
            {{ message }}
          </p>

          <!-- 进度 -->
          <div v-if="progress !== null" class="w-full">
            <div class="h-2 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden">
              <div
                class="h-full bg-gradient-to-r from-purple-600 to-pink-600 transition-all duration-300"
                :style="{ width: progress + '%' }"
              ></div>
            </div>
            <p class="text-center text-sm text-gray-500 dark:text-gray-400 mt-2">{{ progress }}%</p>
          </div>

          <!-- 取消按钮 -->
          <button
            v-if="cancellable"
            @click="cancel"
            class="mt-2 px-4 py-2 text-sm text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-200 transition-colors"
          >
            取消
          </button>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  show: Boolean,
  message: String,
  progress: {
    type: Number,
    default: null
  },
  cancellable: Boolean,
  lockClose: Boolean
})

const emit = defineEmits(['update:show', 'cancel'])

function close() {
  emit('update:show', false)
}

function cancel() {
  emit('cancel')
  close()
}
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
