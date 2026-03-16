import { ref } from 'vue'

const state = ref({
  show: false,
  title: '',
  message: '',
  warning: '',
  confirmText: '',
  confirmButtonText: '确认',
  cancelButtonText: '取消',
  type: 'primary'
})

let resolvePromise = null
let rejectPromise = null

/**
 * 确认对话框 composable
 */
export function useConfirm() {
  /**
   * 显示确认对话框
   * @param {Object} options
   * @param {string} options.message - 确认消息
   * @param {string} options.title - 标题
   * @param {string} options.warning - 警告信息
   * @param {string} options.confirmText - 需要输入的确认文本
   * @param {string} options.confirmButtonText - 确认按钮文本
   * @param {string} options.cancelButtonText - 取消按钮文本
   * @param {string} options.type - 类型 'primary' | 'danger'
   * @returns {Promise<boolean>}
   */
  function confirm(options) {
    return new Promise((resolve, reject) => {
      state.value = {
        show: true,
        title: options.title || '',
        message: options.message || '',
        warning: options.warning || '',
        confirmText: options.confirmText || '',
        confirmButtonText: options.confirmButtonText || '确认',
        cancelButtonText: options.cancelButtonText || '取消',
        type: options.type || 'primary'
      }

      resolvePromise = resolve
      rejectPromise = reject
    })
  }

  /**
   * 危险操作确认
   */
  function confirmDanger(options) {
    return confirm({
      ...options,
      type: 'danger',
      confirmButtonText: options.confirmButtonText || '删除',
      title: options.title || '危险操作'
    })
  }

  function handleConfirm() {
    state.value.show = false
    if (resolvePromise) {
      resolvePromise(true)
      resolvePromise = null
    }
  }

  function handleCancel() {
    state.value.show = false
    if (resolvePromise) {
      resolvePromise(false)
      resolvePromise = null
    }
  }

  return {
    state,
    confirm,
    confirmDanger,
    handleConfirm,
    handleCancel
  }
}

/**
 * 全局确认对话框实例
 */
let globalConfirm = null

export function initConfirm() {
  globalConfirm = useConfirm()
}

export function confirmDialog(options) {
  if (!globalConfirm) {
    console.warn('Confirm dialog not initialized')
    return Promise.resolve(false)
  }
  return globalConfirm.confirm(options)
}

export function confirmDanger(options) {
  if (!globalConfirm) {
    console.warn('Confirm dialog not initialized')
    return Promise.resolve(false)
  }
  return globalConfirm.confirmDanger(options)
}

export function getConfirmState() {
  return globalConfirm?.state || ref({ show: false })
}

export function getConfirmHandlers() {
  return {
    handleConfirm: () => globalConfirm?.handleConfirm(),
    handleCancel: () => globalConfirm?.handleCancel()
  }
}
