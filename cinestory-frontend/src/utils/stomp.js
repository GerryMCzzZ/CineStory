/**
 * STOMP over WebSocket 客户端
 * 用于与 Spring Boot WebSocket 通信
 */

class StompClient {
  constructor() {
    this.client = null
    this.connected = false
    this.subscriptions = new Map()
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectDelay = 3000
    this.listeners = new Map()
  }

  /**
   * 连接到 WebSocket 服务器
   */
  connect() {
    if (this.connected) {
      console.log('STOMP client already connected')
      return Promise.resolve()
    }

    return new Promise((resolve, reject) => {
      // 动态导入 SockJS 和 Stomp
      Promise.all([
        import('sockjs-client'),
        import('@stomp/stompjs')
      ]).then(([/* SockJS */, { Stomp }]) => {
        Stomp.WebSocketClass = SockJS

        const url = this.getWebSocketUrl()

        this.client = Stomp.client(url)

        this.client.connect(
          {},
          () => {
            console.log('STOMP connected')
            this.connected = true
            this.reconnectAttempts = 0
            this.emit('connected')
            resolve()
          },
          (error) => {
            console.error('STOMP connection error:', error)
            this.connected = false
            this.emit('error', error)
            this.attemptReconnect()
            reject(error)
          }
        )

        // 设置断开处理
        this.client.onclose = () => {
          console.log('STOMP disconnected')
          this.connected = false
          this.emit('disconnected')
          this.attemptReconnect()
        }
      }).catch((error) => {
        console.error('Failed to load STOMP libraries:', error)
        // Fallback to simple mock
        this.initMockClient()
        resolve()
      })
    })
  }

  /**
   * 初始化模拟客户端（用于开发）
   */
  initMockClient() {
    console.warn('Using mock STOMP client for development')
    this.client = {
      send: (destination, headers, body) => {
        console.log('Mock send:', destination, body)
      },
      subscribe: (destination, callback) => ({
        id: Math.random(),
        unsubscribe: () => {}
      }),
      disconnect: () => {
        this.connected = false
      }
    }
    this.connected = true
    this.emit('connected')
  }

  /**
   * 断开连接
   */
  disconnect() {
    if (this.client && this.connected) {
      this.client.disconnect()
    }
    this.connected = false
    this.subscriptions.clear()
  }

  /**
   * 订阅目的地
   */
  subscribe(destination, callback) {
    if (!this.connected) {
      console.warn('STOMP client not connected, queuing subscription')
      // 队列订阅，连接后自动订阅
      this.on('connected', () => {
        this.subscribe(destination, callback)
      })
      return
    }

    const subscription = this.client.subscribe(destination, (message) => {
      try {
        const data = JSON.parse(message.body)
        callback(data)
      } catch (err) {
        console.error('Failed to parse STOMP message:', err)
        callback(message.body)
      }
    })

    this.subscriptions.set(destination, subscription)
    return subscription
  }

  /**
   * 取消订阅
   */
  unsubscribe(destination) {
    const subscription = this.subscriptions.get(destination)
    if (subscription) {
      subscription.unsubscribe()
      this.subscriptions.delete(destination)
    }
  }

  /**
   * 发送消息
   */
  send(destination, headers = {}, body = {}) {
    if (!this.connected) {
      console.warn('STOMP client not connected')
      return
    }

    this.client.send(destination, headers, JSON.stringify(body))
  }

  /**
   * 订阅项目进度
   */
  subscribeProgress(projectId, callback) {
    const destination = `/topic/progress/${projectId}`
    return this.subscribe(destination, callback)
  }

  /**
   * 发送心跳
   */
  sendPing() {
    this.send('/app/ws/ping', {}, { message: 'ping' })
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
      console.log(`Attempting STOMP reconnect (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`)
      setTimeout(() => {
        this.connect()
      }, this.reconnectDelay)
    } else {
      console.error('Max STOMP reconnect attempts reached')
      this.emit('reconnectFailed')
    }
  }

  /**
   * 获取 WebSocket URL
   */
  getWebSocketUrl() {
    const protocol = window.location.protocol === 'https:' ? 'https:' : 'http:'
    const host = import.meta.env.VITE_API_HOST || window.location.host
    return `${protocol}//${host}/api/ws`
  }
}

// 创建单例
export const stompClient = new StompClient()

// 导出 composable
export function useStomp() {
  return {
    client: stompClient,
    connect: () => stompClient.connect(),
    disconnect: () => stompClient.disconnect(),
    subscribe: (destination, callback) => stompClient.subscribe(destination, callback),
    unsubscribe: (destination) => stompClient.unsubscribe(destination),
    subscribeProgress: (projectId, callback) => stompClient.subscribeProgress(projectId, callback),
    send: (destination, headers, body) => stompClient.send(destination, headers, body),
    sendPing: () => stompClient.sendPing(),
    on: (event, callback) => stompClient.on(event, callback),
    off: (event, callback) => stompClient.off(event, callback),
    isConnected: () => stompClient.connected
  }
}
