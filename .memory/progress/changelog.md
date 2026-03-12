# 变更日志

记录项目的所有重要变更，包括功能新增、修改、删除等。

---

## v0.2.0 (规划中) - 用户认证版本

### 新增
- [ ] 用户注册/登录功能
- [ ] JWT Token 认证
- [ ] 用户权限管理
- [ ] 项目数据权限隔离

### 变更
- [ ] API 添加认证检查
- [ ] 前端添加登录状态管理

---

## v0.1.1 (2025-03-12) - 核心功能实现

### 新增 🎉

#### REST API 层
- ✅ ProjectController - 项目管理 API
  - 创建/查询/更新/删除项目
  - 启动/取消任务
  - 进度查询

- ✅ StyleController - 风格模板 API
  - 查询风格列表
  - 获取系统预设/自定义风格
  - 风格分类查询

- ✅ 请求/响应 DTO
  - CreateProjectRequest, UpdateProjectRequest, StartTaskRequest
  - ProjectResponse, TaskResponse, StyleTemplateResponse
  - ApiResponse 通用响应封装
  - PageResponse 分页响应封装

#### 服务层
- ✅ ProjectService/StyleTemplateService 实现
- ✅ PromptGenerationService - 提示词生成服务
  - 基于场景类型构建提示词
  - LLM 增强提示词
  - 提示词优化和重试

- ✅ LLM 服务
  - 支持多种 LLM 提供商
  - 提示词增强和优化

#### 视频生成
- ✅ VideoGenerationProvider 抽象接口
- ✅ VideoGenerationService - 异步视频生成
  - 多提供商自动切换
  - 状态检查和重试机制

- ✅ 提供商实现
  - RunwayProvider
  - PikaProvider
  - LumaProvider

#### 视频拼接
- ✅ VideoCompositionService - FFmpeg 视频拼接
  - 视频片段拼接
  - 片头片尾添加
  - 时长计算

- ✅ StorageService - 存储服务
  - MinIO 对象存储
  - 本地存储回退

#### 异常处理
- ✅ ResourceNotFoundException
- ✅ BusinessException
- ✅ GlobalExceptionHandler
  - 资源未找到处理
  - 业务异常处理
  - 验证异常处理
  - 通用异常处理

#### 配置
- ✅ RestTemplateConfig
- ✅ WebConfig (CORS)

### 技术变更

**新增依赖**:
- 无需额外依赖（使用 Spring Boot 内置功能）

**架构改进**:
- 服务层抽象更加清晰
- 支持多提供商的视频生成
- 异步任务处理框架

### 已知限制

- [ ] 无用户认证（单用户模式）
- [ ] WebSocket 配置未完成
- [ ] 视频生成 API 需要配置真实密钥
- [ ] FFmpeg 需要在运行环境中安装
- [ ] 无测试覆盖

---

## v0.1.0 (2025-03-11) - 初始版本

### 新增 🎉
- ✅ 项目初始化
  - Spring Boot 后端项目
  - Vue 3 前端项目
  - Docker 编排配置

- ✅ 项目记忆系统
  - 目标管理
  - 进度追踪
  - 决策记录
  - 代码自检
  - 会话记录

- ✅ 数据库设计
  - 6 张核心表
  - Flyway 版本管理

- ✅ 文本切片服务
  - 多级切片策略
  - 场景类型检测
  - 对话提取功能

- ✅ 前端架构
  - Vue Router 路由配置
  - Pinia 状态管理
  - Axios API 封装
  - WebSocket 客户端
  - 5 个页面组件

### 技术栈总结

**后端**:
- Java 17 + Spring Boot 3.x
- MySQL 8.0 + Redis + MinIO
- Spring Batch + WebSocket

**前端**:
- Vue 3 + Vite
- Vue Router 4 + Pinia
- UnoCSS + Axios

**部署**:
- Docker + Docker Compose
- Nginx 反向代理

---

**文档创建时间**: 2025-03-11
**最后更新**: 2025-03-12
