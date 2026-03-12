# 待办事项

## 高优先级 (P0) - MVP 必须完成

### REST API
- [x] `ProjectController` 项目管理 API
- [x] `StyleController` 风格模板 API
- [x] 请求/响应 DTO
- [x] 全局异常处理

### 提示词生成模块
- [x] `PromptGenerationService` 服务接口
- [x] `PromptGenerationServiceImpl` 实现类
- [x] LLM API 客户端
- [x] 提示词模板系统
- [x] 批量处理优化

### 视频生成集成
- [x] `VideoGenerationService` 服务接口
- [x] 视频生成提供商抽象
- [x] Runway/Pika/Luma API 集成框架
- [x] 异步任务处理
- [x] 失败重试机制
- [ ] 进度轮询任务

### 视频拼接模块
- [x] `VideoCompositionService` 服务接口
- [x] `VideoCompositionServiceImpl` 实现类
- [x] FFmpeg 命令生成
- [x] 视频拼接处理
- [ ] 字幕添加功能
- [ ] 过渡效果实现

---

## 中优先级 (P1) - 改进体验

### WebSocket 进度推送
- [ ] WebSocket 配置
- [ ] 进度消息格式定义
- [ ] 进度推送消息处理器
- [ ] 前端 WebSocket 客户端集成

### 文件处理
- [ ] 文件上传接口
- [ ] 小说文本解析器
- [ ] 视频下载接口

### 前端界面
- [x] 项目创建页面（基础）
- [ ] 进度监控页面完善
- [ ] 结果预览页面
- [ ] 风格选择组件完善

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

- [x] 添加 Validation 注解
- [x] 统一异常处理
- [ ] 添加日志记录
- [ ] 代码注释完善
- [ ] 配置外部化

---

**最后更新**: 2025-03-12
