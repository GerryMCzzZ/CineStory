<template>
  <div class="home">
    <!-- Hero Section -->
    <section class="text-center py-20">
      <h1 class="text-5xl font-bold mb-6">
        <span class="text-gradient">小说转动漫</span>
        <br>
        <span class="text-gray-800 dark:text-gray-200">一键生成视频</span>
      </h1>
      <p class="text-xl text-gray-600 dark:text-gray-400 mb-8 max-w-2xl mx-auto">
        CineStory 帮助你将小说文字自动转换为动漫风格的视频。
        支持多种风格选择，智能场景识别，让创作变得简单。
      </p>
      <div class="flex justify-center gap-4">
        <router-link to="/projects/create" class="btn-primary">
          开始创作
        </router-link>
        <router-link to="/projects" class="btn btn-secondary">
          查看项目
        </router-link>
      </div>
    </section>

    <!-- Features -->
    <section class="py-16">
      <h2 class="text-3xl font-bold text-center mb-12 text-gray-800 dark:text-gray-200">
        核心功能
      </h2>
      <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
        <div class="card text-center">
          <div class="w-16 h-16 mx-auto mb-4 bg-purple-100 dark:bg-purple-900 rounded-full flex-center">
            <span class="text-3xl">📝</span>
          </div>
          <h3 class="text-xl font-semibold mb-2 text-gray-800 dark:text-gray-200">智能文本切片</h3>
          <p class="text-gray-600 dark:text-gray-400">
            多级切片策略，自动识别章节、场景、对话，保持语义完整
          </p>
        </div>

        <div class="card text-center">
          <div class="w-16 h-16 mx-auto mb-4 bg-pink-100 dark:bg-pink-900 rounded-full flex-center">
            <span class="text-3xl">🎨</span>
          </div>
          <h3 class="text-xl font-semibold mb-2 text-gray-800 dark:text-gray-200">多样化风格</h3>
          <p class="text-gray-600 dark:text-gray-400">
            日式动漫、3D风格、水墨国风、赛博朋克等多种风格可选
          </p>
        </div>

        <div class="card text-center">
          <div class="w-16 h-16 mx-auto mb-4 bg-blue-100 dark:bg-blue-900 rounded-full flex-center">
            <span class="text-3xl">🚀</span>
          </div>
          <h3 class="text-xl font-semibold mb-2 text-gray-800 dark:text-gray-200">自动化生成</h3>
          <p class="text-gray-600 dark:text-gray-400">
            集成多个视频生成API，自动拼接合成，一键输出成品
          </p>
        </div>
      </div>
    </section>

    <!-- How it works -->
    <section class="py-16 bg-gray-100 dark:bg-gray-800 -mx-4 px-4 sm:-mx-6 sm:px-6 lg:-mx-8 lg:px-8">
      <h2 class="text-3xl font-bold text-center mb-12 text-gray-800 dark:text-gray-200">
        使用流程
      </h2>
      <div class="max-w-4xl mx-auto">
        <div class="flex flex-col md:flex-row items-center justify-between gap-4">
          <div class="flex-1 text-center">
            <div class="w-12 h-12 mx-auto mb-2 bg-purple-600 text-white rounded-full flex-center text-xl font-bold">1</div>
            <h4 class="font-semibold text-gray-800 dark:text-gray-200">上传小说</h4>
            <p class="text-sm text-gray-600 dark:text-gray-400">粘贴文本或上传文件</p>
          </div>
          <div class="hidden md:block text-gray-400">→</div>
          <div class="flex-1 text-center">
            <div class="w-12 h-12 mx-auto mb-2 bg-purple-600 text-white rounded-full flex-center text-xl font-bold">2</div>
            <h4 class="font-semibold text-gray-800 dark:text-gray-200">选择风格</h4>
            <p class="text-sm text-gray-600 dark:text-gray-400">选择动漫风格模板</p>
          </div>
          <div class="hidden md:block text-gray-400">→</div>
          <div class="flex-1 text-center">
            <div class="w-12 h-12 mx-auto mb-2 bg-purple-600 text-white rounded-full flex-center text-xl font-bold">3</div>
            <h4 class="font-semibold text-gray-800 dark:text-gray-200">生成视频</h4>
            <p class="text-sm text-gray-600 dark:text-gray-400">AI自动生成并拼接</p>
          </div>
          <div class="hidden md:block text-gray-400">→</div>
          <div class="flex-1 text-center">
            <div class="w-12 h-12 mx-auto mb-2 bg-purple-600 text-white rounded-full flex-center text-xl font-bold">4</div>
            <h4 class="font-semibold text-gray-800 dark:text-gray-200">下载成品</h4>
            <p class="text-sm text-gray-600 dark:text-gray-400">获取最终视频</p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
// Home page component
</script>
