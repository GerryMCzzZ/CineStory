/**
 * WebSocket 连接管理
 * 用于接收任务进度实时更新
 */
class WebSocketClient {
  constructor() {
    this.ws = null
    this.listeners = new Map()
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectDelay = 3000
  }

  /**
   * 连接 WebSocket
   */
  connect(url) {
    if (this.ws?.readyState === WebSocket.OPEN) {
      return
    }

    const wsUrl = url || this.getWebSocketUrl()
    this.ws = new WebSocket(wsUrl)

    this.ws.onopen = () => {
      console.log('WebSocket connected')
      this.reconnectAttempts = 0
      this.emit('connected')
    }

    this.ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        this.emit('message', data)

        // 根据消息类型分发
        if (data.type === 'progress') {
          this.emit('progress', data)
        } else if (data.type === 'task_completed') {
          this.emit('taskCompleted', data)
        } else if (data.type === 'task_failed') {
          this.emit('taskFailed', data)
        }
      } catch (err) {
        console.error('Failed to parse WebSocket message:', err)
      }
    }

    this.ws.onerror = (error) => {
      console.error('WebSocket error:', error)
      this.emit('error', error)
    }

    this.ws.onclose = () => {
      console.log('WebSocket disconnected')
      this.emit('disconnected')
      this.attemptReconnect()
    }
  }

  /**
   * 断开连接
   */
  disconnect() {
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
  }

  /**
   * 订阅任务进度
   */
  subscribe(taskId, callback) {
    const channel = `/topic/progress/${taskId}`
    this.send({ type: 'subscribe', channel })
    this.on(`progress_${taskId}`, callback)
  }

  /**
   * 取消订阅
   */
  unsubscribe(taskId) {
    const channel = `/topic/progress/${taskId}`
    this.send({ type: 'unsubscribe', channel })
    this.off(`progress_${taskId}`)
  }

  /**
   * 发送消息
   */
  send(data) {
    if (this.ws?.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(data))
    } else {
      console.warn('WebSocket is not connected')
    }
  }

  /**
   * 注册事件监听器
   */
  on(event, callback) {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, [])
    }
    this.listeners.get(event).push(callback)
  }

  /**
   * 移除事件监听器
   */
  off(event, callback) {
    if (!this.listeners.has(event)) return
    if (callback) {
      this.listeners.set(
        event,
        this.listeners.get(event).filter(cb => cb !== callback)
      )
    } else {
      this.listeners.delete(event)
    }
  }

  /**
   * 触发事件
   */
  emit(event, data) {
    if (this.listeners.has(event)) {
      this.listeners.get(event).forEach(callback => callback(data))
    }
  }

  /**
   * 尝试重连
   */
  attemptReconnect() {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++
      console.log(`Attempting to reconnect (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`)
      setTimeout(() => {
        this.connect()
      }, this.reconnectDelay)
    } else {
      console.error('Max reconnect attempts reached')
      this.emit('reconnectFailed')
    }
  }

  /**
   * 获取 WebSocket URL
   */
  getWebSocketUrl() {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const host = import.meta.env.VITE_WS_HOST || window.location.host
    return `${protocol}//${host}/ws`
  }

  /**
   * 获取连接状态
   */
  get readyState() {
    if (!this.ws) return WebSocket.CLOSED
    return this.ws.readyState
  }

  /**
   * 是否已连接
   */
  get isConnected() {
    return this.ws?.readyState === WebSocket.OPEN
  }
}

// 创建单例
export const wsClient = new WebSocketClient()

// 导出便捷方法
export function useWebSocket() {
  return {
    connect: (url) => wsClient.connect(url),
    disconnect: () => wsClient.disconnect(),
    subscribe: (taskId, callback) => wsClient.subscribe(taskId, callback),
    unsubscribe: (taskId) => wsClient.unsubscribe(taskId),
    on: (event, callback) => wsClient.on(event, callback),
    off: (event, callback) => wsClient.off(event, callback),
    isConnected: () => wsClient.isConnected
  }
}
