<template>
  <div class="file-uploader">
    <div
      @drop.prevent="handleDrop"
      @dragover.prevent="isDragging = true"
      @dragleave.prevent="isDragging = false"
      @click="selectFile"
      :class="[
        'border-2 border-dashed rounded-xl p-8 text-center cursor-pointer transition-all',
        isDragging ? 'border-purple-500 bg-purple-50 dark:bg-purple-900/20' : 'border-gray-300 dark:border-gray-600 hover:border-purple-400'
      ]"
    >
      <!-- 拖拽上传区域 -->
      <div v-if="!file" class="space-y-4">
        <div class="w-16 h-16 mx-auto flex items-center justify-center bg-purple-100 dark:bg-purple-900/30 rounded-full">
          <svg xmlns="http://www.w3.org/2000/svg" class="w-8 h-8 text-purple-600 dark:text-purple-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
          </svg>
        </div>
        <div>
          <p class="text-lg font-medium text-gray-700 dark:text-gray-300">
            {{ isDragging ? '释放文件以上传' : '拖拽文件到此处' }}
          </p>
          <p class="text-sm text-gray-500 mt-1">或者点击选择文件</p>
        </div>
        <p class="text-xs text-gray-400">
          支持 .txt, .md 文件，最大 {{ maxSizeMB }}MB
        </p>
      </div>

      <!-- 已选择文件 -->
      <div v-else class="space-y-4">
        <div class="w-16 h-16 mx-auto flex items-center justify-center bg-green-100 dark:bg-green-900/30 rounded-full">
          <svg xmlns="http://www.w3.org/2000/svg" class="w-8 h-8 text-green-600 dark:text-green-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </div>
        <div>
          <p class="text-lg font-medium text-gray-700 dark:text-gray-300 truncate max-w-xs mx-auto">
            {{ file.name }}
          </p>
          <p class="text-sm text-gray-500 mt-1">{{ formatSize(file.size) }}</p>
        </div>
        <button
          @click.stop="removeFile"
          class="text-red-500 hover:text-red-600 text-sm font-medium"
        >
          移除文件
        </button>
      </div>

      <input
        ref="fileInput"
        type="file"
        :accept="accept"
        class="hidden"
        @change="handleFileSelect"
      />
    </div>

    <!-- 文件预览 -->
    <div v-if="showPreview && previewContent" class="mt-4 p-4 bg-gray-100 dark:bg-gray-800 rounded-lg">
      <div class="flex items-center justify-between mb-2">
        <h4 class="font-medium text-gray-700 dark:text-gray-300">内容预览</h4>
        <span class="text-xs text-gray-500">{{ previewContent.length }} / {{ contentLength }} 字符</span>
      </div>
      <div class="max-h-48 overflow-y-auto text-sm text-gray-600 dark:text-gray-400 whitespace-pre-wrap">{{ previewContent }}</div>
      <div v-if="previewContent.length < contentLength" class="mt-2 text-center">
        <span class="text-purple-600 dark:text-purple-400 text-sm cursor-pointer hover:underline" @click="showFullPreview = true">
          查看全部内容
        </span>
      </div>
    </div>

    <!-- 进度条 -->
    <div v-if="uploading" class="mt-4">
      <div class="flex justify-between text-sm mb-1">
        <span>上传中...</span>
        <span>{{ uploadProgress }}%</span>
      </div>
      <div class="h-2 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden">
        <div
          class="h-full bg-gradient-to-r from-purple-500 to-pink-500 transition-all"
          :style="{ width: uploadProgress + '%' }"
        ></div>
      </div>
    </div>

    <!-- 错误提示 -->
    <div v-if="error" class="mt-4 p-3 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg text-red-600 dark:text-red-400 text-sm">
      {{ error }}
    </div>

    <!-- 全部内容弹窗 -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="showFullPreview" class="fixed inset-0 z-50 flex items-center justify-center p-4" @click.self="showFullPreview = false">
          <div class="absolute inset-0 bg-black/50 backdrop-blur-sm"></div>
          <div class="relative bg-white dark:bg-gray-800 rounded-xl shadow-xl max-w-2xl w-full max-h-[80vh] overflow-hidden">
            <div class="p-4 border-b border-gray-200 dark:border-gray-700 flex justify-between items-center">
              <h3 class="font-medium">完整内容</h3>
              <button @click="showFullPreview = false" class="text-gray-500 hover:text-gray-700 dark:hover:text-gray-300">
                <svg xmlns="http://www.w3.org/2000/svg" class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>
            <div class="p-4 overflow-y-auto max-h-[60vh] text-sm text-gray-700 dark:text-gray-300 whitespace-pre-wrap">
              {{ fullContent }}
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { uploadFile } from '@/api/file'

const props = defineProps({
  modelValue: {
    type: [String, File],
    default: ''
  },
  accept: {
    type: String,
    default: '.txt,.md,text/plain,text/markdown'
  },
  maxSize: {
    type: Number,
    default: 10 * 1024 * 1024 // 10MB
  },
  showPreview: {
    type: Boolean,
    default: true
  },
  autoUpload: {
    type: Boolean,
    default: false
  },
  uploadType: {
    type: String,
    default: 'novel' // novel, preview, style-image
  }
})

const emit = defineEmits(['update:modelValue', 'change', 'upload-success', 'upload-error'])

const fileInput = ref(null)
const file = ref(null)
const isDragging = ref(false)
const uploading = ref(false)
const uploadProgress = ref(0)
const error = ref('')
const previewContent = ref('')
const fullContent = ref('')
const showFullPreview = ref(false)

const maxSizeMB = computed(() => Math.round(props.maxSize / 1024 / 1024))
const contentLength = computed(() => fullContent.value.length)

const selectFile = () => {
  fileInput.value?.click()
}

const handleFileSelect = (event) => {
  const selected = event.target.files?.[0]
  if (selected) {
    processFile(selected)
  }
}

const handleDrop = (event) => {
  isDragging.value = false
  const dropped = event.dataTransfer.files?.[0]
  if (dropped) {
    processFile(dropped)
  }
}

const processFile = (selectedFile) {
  // 清除之前的错误
  error.value = ''

  // 验证文件类型
  const validTypes = ['text/plain', 'text/markdown', 'text/html', 'application/octet-stream']
  const fileExtension = selectedFile.name.split('.').pop()?.toLowerCase()
  const validExtensions = ['txt', 'md', 'html', 'htm']

  if (!validTypes.includes(selectedFile.type) && !validExtensions.includes(fileExtension)) {
    error.value = '不支持的文件类型，请上传文本文件'
    return
  }

  // 验证文件大小
  if (selectedFile.size > props.maxSize) {
    error.value = `文件大小超过限制（最大 ${maxSizeMB.value}MB）`
    return
  }

  file.value = selectedFile

  // 读取文件内容
  const reader = new FileReader()
  reader.onload = (e) => {
    const content = e.target.result
    fullContent.value = content

    // 生成预览（前 500 字）
    const previewLength = 500
    previewContent.value = content.length > previewLength
      ? content.substring(0, previewLength) + '\n\n...'
      : content

    emit('update:modelValue', content)
    emit('change', selectedFile, content)

    // 自动上传
    if (props.autoUpload) {
      uploadFileContent()
    }
  }
  reader.onerror = () => {
    error.value = '读取文件失败'
  }
  reader.readAsText(selectedFile)
}

const removeFile = () => {
  file.value = null
  previewContent.value = ''
  fullContent.value = ''
  error.value = ''
  uploadProgress.value = 0
  emit('update:modelValue', '')
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

const uploadFileContent = async () => {
  if (!file.value) return

  uploading.value = true
  uploadProgress.value = 0
  error.value = ''

  try {
    const formData = new FormData()
    formData.append('file', file.value)

    const response = await uploadFile(formData, (progress) => {
      uploadProgress.value = progress
    })

    emit('upload-success', response)
  } catch (err) {
    error.value = err.message || '上传失败'
    emit('upload-error', err)
  } finally {
    uploading.value = false
  }
}

const formatSize = (bytes) => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(1) + ' MB'
}

// 暴露方法
defineExpose({
  removeFile,
  upload: uploadFileContent,
  getFile: () => file.value,
  getContent: () => fullContent.value
})

// 监听外部值变化
watch(() => props.modelValue, (newValue) => {
  if (!newValue && file.value) {
    removeFile()
  }
})
</script>

<style scoped>
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}
</style>
