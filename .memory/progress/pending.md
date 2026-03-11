# 待办事项

## 高优先级 (P0) - MVP 必须完成

### 提示词生成模块
- [ ] `PromptGenerationService` 服务接口
- [ ] `PromptGenerationServiceImpl` 实现类
- [ ] LLM API 客户端（OpenAI/Claude）
- [ ] 提示词模板系统
- [ ] 批量处理优化

### 视频生成集成
- [ ] `VideoGenerationService` 服务接口
- [ ] `VideoGenerationServiceImpl` 实现类
- [ ] Runway API 集成
- [ ] 异步任务处理
- [ ] 失败重试机制
- [ ] 进度轮询机制

### 视频拼接模块
- [ ] `VideoCompositionService` 服务接口
- [ ] `VideoCompositionServiceImpl` 实现类
- [ ] FFmpeg 命令生成
- [ ] 视频拼接处理
- [ ] 字幕添加（可选）

### REST API
- [ ] `ProjectController` 项目管理 API
- [ ] `TaskController` 任务管理 API
- [ ] `StyleController` 风格模板 API
- [ ] 请求/响应 DTO
- [ ] 全局异常处理

---

## 中优先级 (P1) - 改进体验

### 前端界面
- [ ] 项目创建页面
- [ ] 进度监控页面
- [ ] 结果预览页面
- [ ] 风格选择组件

### WebSocket 进度推送
- [ ] WebSocket 配置
- [ ] 进度消息格式定义
- [ ] 前端 WebSocket 客户端

### 文件处理
- [ ] 文件上传接口
- [ ] MinIO 集成
- [ ] 视频下载接口

---

## 低优先级 (P2) - 优化提升

### 测试
- [ ] 单元测试
- [ ] 集成测试
- [ ] 端到端测试

### 监控
- [ ] 日志规范
- [ ] 性能监控
- [ ] 错误追踪

### 文档
- [ ] API 文档
- [ ] 部署文档
- [ ] 用户手册

---

## 技术债务

- [ ] 添加 Validation 注解
- [ ] 统一异常处理
- [ ] 添加日志记录
- [ ] 代码注释完善
- [ ] 配置外部化

---

**最后更新**: 2025-03-11
