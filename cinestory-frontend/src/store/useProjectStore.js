import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { projectApi } from '@/api/project'

/**
 * 项目状态管理
 */
export const useProjectStore = defineStore('project', () => {
  // 状态
  const projects = ref([])
  const currentProject = ref(null)
  const loading = ref(false)
  const error = ref(null)

  // 计算属性
  const projectCount = computed(() => projects.value.length)
  const processingCount = computed(() =>
    projects.value.filter(p => p.status === 'PROCESSING').length
  )
  const completedCount = computed(() =>
    projects.value.filter(p => p.status === 'COMPLETED').length
  )

  // 操作
  async function fetchProjects() {
    loading.value = true
    error.value = null
    try {
      projects.value = await projectApi.getList()
    } catch (err) {
      error.value = err.message
      throw err
    } finally {
      loading.value = false
    }
  }

  async function fetchProject(id) {
    loading.value = true
    error.value = null
    try {
      currentProject.value = await projectApi.getDetail(id)
      return currentProject.value
    } catch (err) {
      error.value = err.message
      throw err
    } finally {
      loading.value = false
    }
  }

  async function createProject(data) {
    loading.value = true
    error.value = null
    try {
      const project = await projectApi.create(data)
      projects.value.push(project)
      return project
    } catch (err) {
      error.value = err.message
      throw err
    } finally {
      loading.value = false
    }
  }

  async function deleteProject(id) {
    loading.value = true
    error.value = null
    try {
      await projectApi.delete(id)
      projects.value = projects.value.filter(p => p.id !== id)
      if (currentProject.value?.id === id) {
        currentProject.value = null
      }
    } catch (err) {
      error.value = err.message
      throw err
    } finally {
      loading.value = false
    }
  }

  async function startTask(projectId, config) {
    loading.value = true
    error.value = null
    try {
      const task = await projectApi.startTask(projectId, config)
      // 更新项目状态
      const project = projects.value.find(p => p.id === projectId)
      if (project) {
        project.status = 'PROCESSING'
        project.currentTask = task
      }
      return task
    } catch (err) {
      error.value = err.message
      throw err
    } finally {
      loading.value = false
    }
  }

  function setCurrentProject(project) {
    currentProject.value = project
  }

  function clearError() {
    error.value = null
  }

  return {
    // 状态
    projects,
    currentProject,
    loading,
    error,
    // 计算属性
    projectCount,
    processingCount,
    completedCount,
    // 操作
    fetchProjects,
    fetchProject,
    createProject,
    deleteProject,
    startTask,
    setCurrentProject,
    clearError
  }
})
