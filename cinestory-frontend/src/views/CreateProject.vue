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
          />
        </div>

        <!-- 项目描述 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            项目描述
          </label>
          <textarea
            v-model="form.description"
            class="input"
            rows="3"
            placeholder="简单描述一下这个项目"
          ></textarea>
        </div>

        <!-- 小说来源 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            小说内容 <span class="text-red-500">*</span>
          </label>
          <div class="space-y-4">
            <!-- 选项卡 -->
            <div class="flex border-b border-gray-200 dark:border-gray-700">
              <button
                type="button"
                @click="inputMethod = 'paste'"
                :class="[
                  'px-4 py-2 border-b-2 transition-colors',
                  inputMethod === 'paste'
                    ? 'border-purple-600 text-purple-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 dark:hover:text-gray-300'
                ]"
              >
                粘贴文本
              </button>
              <button
                type="button"
                @click="inputMethod = 'file'"
                :class="[
                  'px-4 py-2 border-b-2 transition-colors',
                  inputMethod === 'file'
                    ? 'border-purple-600 text-purple-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 dark:hover:text-gray-300'
                ]"
              >
                上传文件
              </button>
            </div>

            <!-- 粘贴文本输入 -->
            <div v-if="inputMethod === 'paste'">
              <textarea
                v-model="form.content"
                class="input"
                rows="12"
                placeholder="粘贴小说文本内容..."
                required
              ></textarea>
              <div class="text-right text-sm text-gray-500 mt-1">
                {{ form.content.length }} 字
              </div>
            </div>

            <!-- 文件上传 -->
            <div v-else class="border-2 border-dashed border-gray-300 dark:border-gray-600 rounded-lg p-8 text-center">
              <input
                ref="fileInput"
                type="file"
                accept=".txt,.md"
                class="hidden"
                @change="handleFileChange"
              />
              <div v-if="uploadedFile" class="text-green-600 dark:text-green-400">
                <span class="text-2xl">✓</span>
                <p class="mt-2">{{ uploadedFile.name }}</p>
                <p class="text-sm text-gray-500">{{ (uploadedFile.size / 1024).toFixed(0) }} KB</p>
                <button type="button" @click="clearFile" class="text-red-500 text-sm mt-2">重新选择</button>
              </div>
              <div v-else>
                <div class="text-4xl mb-4">📄</div>
                <p class="text-gray-600 dark:text-gray-400 mb-4">
                  支持 .txt、.md 格式
                </p>
                <button
                  type="button"
                  @click="$refs.fileInput.click()"
                  class="btn-primary"
                >
                  选择文件
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 风格选择 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            风格模板
          </label>
          <div class="grid grid-cols-2 md:grid-cols-3 gap-3">
            <div
              v-for="style in availableStyles"
              :key="style.id"
              @click="form.styleId = style.id"
              :class="[
                'p-4 rounded-lg border-2 cursor-pointer transition-all',
                form.styleId === style.id
                  ? 'border-purple-600 bg-purple-50 dark:bg-purple-900/20'
                  : 'border-gray-200 dark:border-gray-700 hover:border-gray-300'
              ]"
            >
              <div class="text-2xl mb-2">{{ style.icon }}</div>
              <div class="font-medium text-gray-800 dark:text-gray-200">{{ style.name }}</div>
              <div class="text-xs text-gray-500">{{ style.nameEn }}</div>
            </div>
          </div>
        </div>

        <!-- 提交按钮 -->
        <div class="flex justify-end gap-4 pt-4">
          <button type="button" @click="goBack" class="btn btn-secondary">
            取消
          </button>
          <button type="submit" :disabled="submitting" class="btn-primary">
            <span v-if="submitting">创建中...</span>
            <span v-else>创建项目</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useProjectStore } from '@/store/useProjectStore'

const router = useRouter()
const projectStore = useProjectStore()

const inputMethod = ref('paste')
const submitting = ref(false)
const uploadedFile = ref(null)

const form = reactive({
  name: '',
  description: '',
  content: '',
  styleId: 1
})

// 可用风格列表（实际应从API获取）
const availableStyles = [
  { id: 1, name: '日式动漫风', nameEn: 'Japanese Anime', icon: '🌸' },
  { id: 2, name: '3D动漫风', nameEn: '3D Anime', icon: '✨' },
  { id: 3, name: '水墨国风', nameEn: 'Chinese Ink', icon: '🎨' },
  { id: 4, name: '赛博朋克', nameEn: 'Cyberpunk', icon: '🌆' },
  { id: 5, name: '吉卜力风', nameEn: 'Ghibli Style', icon: '🏞️' }
]

const goBack = () => {
  router.push('/projects')
}

const handleFileChange = (e) => {
  const file = e.target.files[0]
  if (file) {
    uploadedFile.value = file
    // 读取文件内容
    const reader = new FileReader()
    reader.onload = (e) => {
      form.content = e.target.result
    }
    reader.readAsText(file)
  }
}

const clearFile = () => {
  uploadedFile.value = null
  form.content = ''
}

const handleSubmit = async () => {
  if (!form.name || !form.content) {
    alert('请填写项目名称和内容')
    return
  }

  submitting.value = true
  try {
    const project = await projectStore.createProject({
      name: form.name,
      description: form.description,
      novelContent: form.content,
      styleTemplateId: form.styleId
    })
    router.push(`/projects/${project.id}`)
  } catch (err) {
    alert('创建失败：' + err.message)
  } finally {
    submitting.value = false
  }
}
</script>
