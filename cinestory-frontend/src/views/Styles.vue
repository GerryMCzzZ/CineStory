<template>
  <div class="styles">
    <div class="flex justify-between items-center mb-8">
      <h1 class="text-3xl font-bold text-gray-800 dark:text-gray-200">风格模板</h1>
      <button class="btn-primary">+ 自定义风格</button>
    </div>

    <!-- 风格列表 -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div
        v-for="style in styles"
        :key="style.id"
        class="card overflow-hidden"
      >
        <!-- 预览图 -->
        <div class="aspect-video bg-gradient-to-br from-purple-500 to-pink-500 relative">
          <div class="absolute inset-0 flex items-center justify-center">
            <span class="text-6xl">{{ style.icon }}</span>
          </div>
          <div v-if="!style.isSystem" class="absolute top-2 right-2 px-2 py-1 bg-black/50 rounded text-xs text-white">
            自定义
          </div>
        </div>

        <!-- 信息 -->
        <div class="p-4">
          <h3 class="text-lg font-semibold text-gray-800 dark:text-gray-200 mb-1">
            {{ style.name }}
          </h3>
          <p class="text-sm text-gray-500 mb-4">{{ style.nameEn }}</p>
          <p class="text-sm text-gray-600 dark:text-gray-400 line-clamp-2">
            {{ style.description }}
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

// 风格列表（实际应从API获取）
const styles = ref([
  {
    id: 1,
    name: '日式动漫风',
    nameEn: 'Japanese Anime',
    description: '传统日本动漫风格，色彩鲜艳，线条清晰',
    icon: '🌸',
    isSystem: true
  },
  {
    id: 2,
    name: '3D动漫风',
    nameEn: '3D Anime',
    description: '现代3D动画风格，类似新海诚电影',
    icon: '✨',
    isSystem: true
  },
  {
    id: 3,
    name: '水墨国风',
    nameEn: 'Chinese Ink',
    description: '中国传统水墨画风格',
    icon: '🎨',
    isSystem: true
  },
  {
    id: 4,
    name: '赛博朋克',
    nameEn: 'Cyberpunk',
    description: '未来科技感风格，霓虹灯光，暗色调',
    icon: '🌆',
    isSystem: true
  },
  {
    id: 5,
    name: '吉卜力风',
    nameEn: 'Ghibli Style',
    description: '宫崎骏吉卜力工作室风格',
    icon: '🏞️',
    isSystem: true
  }
])
</script>
