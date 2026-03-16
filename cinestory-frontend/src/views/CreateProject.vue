<template>
  <div class="create-project max-w-3xl mx-auto">
    <div class="flex items-center gap-4 mb-8">
      <button @click="goBack" class="p-2 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg">
        ← 返回
      </button>
      <h1 class="text-3xl font-bold text-gray-800 dark:text-gray-200">创建新项目</h1>
    </div>

    <div class="card">
      <form @submit.prevent="handleSubmit" class="space-y-6">
        <!-- 项目名称 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            项目名称 <span class="text-red-500">*</span>
          </label>
          <input
            v-model="form.name"
            type="text"
            class="input"
            placeholder="为你的项目起个名字"
            required
            maxlength="255"
          />
          <p class="text-xs text-gray-500 mt-1">{{ form.name.length }} / 255</p>
        </div>

        <!-- 项目描述 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            项目描述
          </label>
          <textarea
            v-model="form.description"
            class="input"
            rows="2"
            placeholder="简单描述一下这个项目"
            maxlength="1000"
          ></textarea>
          <p class="text-xs text-gray-500 mt-1 text-right">{{ form.description.length }} / 1000</p>
        </div>

        <!-- 小说内容 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            小说内容 <span class="text-red-500">*</span>
          </label>

          <FileUploader
            ref="fileUploaderRef"
            v-model="form.content"
            :show-preview="true"
            :auto-upload="false"
            @change="handleContentChange"
          />

          <!-- 内容统计 -->
          <div v-if="form.content" class="mt-3 flex gap-4 text-sm text-gray-500">
            <span>字数: {{ contentStats.chars }}</span>
            <span>预估时长: {{ contentStats.duration }}秒</span>
            <span>预估切片: {{ contentStats.slices }}个</span>
          </div>
        </div>

        <!-- 风格选择 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            风格模板
          </label>
          <div v-if="loadingStyles" class="text-center py-8">
            <div class="inline-block w-6 h-6 border-2 border-purple-600 border-t-transparent rounded-full animate-spin"></div>
          </div>
          <div v-else class="grid grid-cols-2 md:grid-cols-3 gap-3">
            <div
              v-for="style in availableStyles"
              :key="style.id"
              @click="form.styleId = style.id"
              :class="[
                'p-4 rounded-lg border-2 cursor-pointer transition-all relative',
                form.styleId === style.id
                  ? 'border-purple-600 bg-purple-50 dark:bg-purple-900/20'
                  : 'border-gray-200 dark:border-gray-700 hover:border-gray-300'
              ]"
            >
              <div v-if="form.styleId === style.id" class="absolute top-2 right-2 w-4 h-4 bg-purple-600 rounded-full flex items-center justify-center">
                <svg xmlns="http://www.w3.org/2000/svg" class="w-3 h-3 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7" />
                </svg>
              </div>
              <div class="text-3xl mb-2">{{ style.icon }}</div>
              <div class="font-medium text-gray-800 dark:text-gray-200">{{ style.name }}</div>
              <div class="text-xs text-gray-500">{{ style.category }}</div>
            </div>
          </div>
        </div>

        <!-- 高级选项（折叠） -->
        <div>
          <button
            type="button"
            @click="showAdvanced = !showAdvanced"
            class="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-400 hover:text-gray-800 dark:hover:text-gray-200"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              class="w-4 h-4 transition-transform"
              :class="{ 'rotate-90': showAdvanced }"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
            </svg>
            高级选项
          </button>

          <div v-if="showAdvanced" class="mt-4 space-y-4 p-4 bg-gray-50 dark:bg-gray-800/50 rounded-lg">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                小说标题
              </label>
              <input
                v-model="form.novelTitle"
                type="text"
                class="input"
                placeholder="可选，留空则自动提取"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                小说作者
              </label>
              <input
                v-model="form.novelAuthor"
                type="text"
                class="input"
                placeholder="可选"
              />
            </div>
          </div>
        </div>

        <!-- 提交按钮 -->
        <div class="flex justify-end gap-4 pt-4">
          <button type="button" @click="goBack" class="px-5 py-2.5 text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-xl transition-colors font-medium">
            取消
          </button>
          <button type="submit" :disabled="submitting || !form.content" class="px-6 py-2.5 bg-purple-600 hover:bg-purple-700 text-white rounded-xl transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed font-medium flex items-center gap-2">
            <svg v-if="submitting" class="w-5 h-5 animate-spin" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
            </svg>
            <span>{{ submitting ? '创建中...' : '创建项目' }}</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useProjectStore } from '@/store/useProjectStore'
import FileUploader from '@/components/FileUploader.vue'
import { fetchStyles } from '@/api/style'

const router = useRouter()
const projectStore = useProjectStore()

const fileUploaderRef = ref(null)
const submitting = ref(false)
const loadingStyles = ref(false)
const showAdvanced = ref(false)

const form = reactive({
  name: '',
  description: '',
  content: '',
  novelTitle: '',
  novelAuthor: '',
  styleId: null
})

// 风格列表
const availableStyles = ref([])

// 内容统计
const contentStats = computed(() => {
  const chars = form.content.length
  // 简单估算：每秒约50字，每个切片约500字
  return {
    chars,
    duration: Math.ceil(chars / 50),
    slices: Math.ceil(chars / 500)
  }
})

// 加载风格列表
onMounted(async () => {
  loadingStyles.value = true
  try {
    const styles = await fetchStyles()
    availableStyles.value = styles.map((s, index) => ({
      id: s.id || index + 1,
      name: s.name || '未命名风格',
      category: s.category || '默认',
      icon: getStyleIcon(s.category),
      previewImage: s.previewImage
    }))
    // 默认选择第一个
    if (availableStyles.value.length > 0 && !form.styleId) {
      form.styleId = availableStyles.value[0].id
    }
  } catch (err) {
    console.error('Failed to load styles:', err)
    // 使用默认风格
    availableStyles.value = [
      { id: 1, name: '日式动漫风', category: 'Anime', icon: '🌸' },
      { id: 2, name: '3D动漫风', category: '3D', icon: '✨' },
      { id: 3, name: '水墨国风', category: 'Chinese', icon: '🎨' },
      { id: 4, name: '赛博朋克', category: 'Sci-Fi', icon: '🌆' },
      { id: 5, name: '吉卜力风', category: 'Ghibli', icon: '🏞️' }
    ]
    form.styleId = 1
  } finally {
    loadingStyles.value = false
  }
})

const getStyleIcon = (category) => {
  const icons = {
    'Anime': '🌸',
    '3D': '✨',
    'Chinese': '🎨',
    'Sci-Fi': '🌆',
    'Ghibli': '🏞️'
  }
  return icons[category] || '🎬'
}

const goBack = () => {
  router.push('/projects')
}

const handleContentChange = (file, content) => {
  // 从文件名提取标题（如果没有手动填写）
  if (!form.novelTitle && file) {
    const fileName = file.name.replace(/\.[^/.]+$/, '')
    form.novelTitle = fileName
  }
}

const handleSubmit = async () => {
  if (!form.name || !form.content) {
    window.toast?.warning('请填写项目名称和内容')
    return
  }

  submitting.value = true
  try {
    const project = await projectStore.createProject({
      name: form.name,
      description: form.description,
      novelContent: form.content,
      novelTitle: form.novelTitle,
      novelAuthor: form.novelAuthor,
      styleTemplateId: form.styleId
    })
    window.toast?.success('项目创建成功')
    router.push(`/projects/${project.id}`)
  } catch (err) {
    window.toast?.error(err.response?.data?.message || err.message || '创建失败')
  } finally {
    submitting.value = false
  }
}
</script>
