<template>
  <Teleport to="body">
    <Transition name="fade">
      <div
        v-if="show"
        class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4"
        @click.self="handleCancel"
      >
        <div
          class="bg-white dark:bg-gray-800 rounded-2xl shadow-2xl max-w-md w-full overflow-hidden"
          @click.stop
        >
          <!-- 头部 -->
          <div v-if="title" class="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">{{ title }}</h3>
          </div>

          <!-- 内容 -->
          <div class="px-6 py-4">
            <p class="text-gray-700 dark:text-gray-300">{{ message }}</p>

            <!-- 警告信息 -->
            <div
              v-if="warning"
              class="mt-4 p-3 bg-yellow-50 dark:bg-yellow-900/20 border border-yellow-200 dark:border-yellow-800 rounded-lg"
            >
              <div class="flex items-start gap-2">
                <svg class="w-5 h-5 text-yellow-600 dark:text-yellow-400 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>
                <p class="text-sm text-yellow-700 dark:text-yellow-300">{{ warning }}</p>
              </div>
            </div>

            <!-- 输入框（用于危险操作确认） -->
            <div v-if="confirmText" class="mt-4">
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                请输入 <code class="px-1 py-0.5 bg-gray-100 dark:bg-gray-700 rounded">{{ confirmText }}</code> 以确认
              </label>
              <input
                v-model="inputValue"
                type="text"
                class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent bg-white dark:bg-gray-700 dark:text-white"
                :placeholder="`输入 ${confirmText}`"
              >
            </div>
          </div>

          <!-- 按钮 -->
          <div class="px-6 py-4 bg-gray-50 dark:bg-gray-800/50 flex justify-end gap-3">
            <button
              @click="handleCancel"
              class="px-4 py-2 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-700 rounded-lg transition-colors"
            >
              {{ cancelText }}
            </button>
            <button
              @click="handleConfirm"
              :disabled="confirmText && inputValue !== confirmText"
              :class="[
                'px-4 py-2 rounded-lg transition-colors',
                type === 'danger'
                  ? 'bg-red-600 text-white hover:bg-red-700 disabled:opacity-50 disabled:cursor-not-allowed'
                  : 'bg-purple-600 text-white hover:bg-purple-700 disabled:opacity-50 disabled:cursor-not-allowed'
              ]"
            >
              {{ confirmTextValue }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  show: Boolean,
  title: String,
  message: {
    type: String,
    required: true
  },
  warning: String,
  confirmText: String,
  confirmButtonText: {
    type: String,
    default: '确认'
  },
  cancelButtonText: {
    type: String,
    default: '取消'
  },
  type: {
    type: String,
    default: 'primary', // 'primary' | 'danger'
    validator: (value) => ['primary', 'danger'].includes(value)
  }
})

const emit = defineEmits(['update:show', 'confirm', 'cancel'])

const inputValue = ref('')

const confirmTextValue = props.confirmButtonText
const cancelText = props.cancelButtonText

watch(() => props.show, (newVal) => {
  if (newVal) {
    inputValue.value = ''
  }
})

function handleConfirm() {
  if (props.confirmText && inputValue.value !== props.confirmText) {
    return
  }
  emit('confirm')
  emit('update:show', false)
}

function handleCancel() {
  emit('cancel')
  emit('update:show', false)
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
