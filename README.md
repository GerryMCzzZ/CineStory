# CineStory

> 小说转动漫视频生成工具

输入小说文本，自动生成动漫风格的短视频。支持可配置的风格模板、多种视频生成源和智能文本切片。

## 功能特性

### 核心功能
- **智能文本切片**：多级切片策略，保持语义完整和上下文连贯
- **小说文件解析**：支持 TXT/EPUB 格式小说文件上传和解析
- **提示词生成**：基于 LLM 自动生成视频生成提示词
- **多源视频生成**：支持 Runway、Pika、Luma 等多种 API
- **视频拼接**：自动拼接视频片段，支持字幕和转场效果
- **风格配置**：内置多种动漫风格，支持自定义
- **实时进度**：WebSocket 实时推送任务进度

### 技术亮点
- Java 17 + Spring Boot 3.x 后端
- Vue 3 + Vite 前端
- Docker 容器化部署
- WebSocket (STOMP) 实时进度推送
- 异步任务处理
- MySQL + Redis + MinIO 存储
- SpringDoc OpenAPI (Swagger) API 文档

## 快速开始

### 环境要求

- Docker & Docker Compose
- Java 17+ (本地开发)
- Node.js 18+ (前端开发)

### 一键启动

```bash
# 克隆项目
git clone https://github.com/yourusername/cinestory.git
cd cinestory

# 配置环境变量
cp .env.example .env
# 编辑 .env 填入 API 密钥

# 使用 Makefile 启动（推荐）
make build    # 构建镜像
make up       # 启动所有服务
make logs     # 查看日志

# 或直接使用 docker-compose
docker-compose up -d --build
docker-compose logs -f backend
```

### Makefile 命令

| 命令 | 说明 |
|------|------|
| `make help` | 显示所有可用命令 |
| `make build` | 构建所有 Docker 镜像 |
| `make up` | 启动所有服务 |
| `make down` | 停止所有服务 |
| `make restart` | 重启所有服务 |
| `make logs` | 查看所有服务日志 |
| `make logs-backend` | 查看后端服务日志 |
| `make clean` | 清理容器和卷 |
| `make health-backend` | 检查后端健康状态 |

### 本地开发

**后端**
```bash
cd cinestory-backend
mvn spring-boot:run
```

**前端**
```bash
cd cinestory-frontend
npm install
npm run dev
```

## API 文档

### Swagger UI

启动服务后访问 http://localhost:8080/swagger-ui.html 查看完整的 API 文档。

### 项目管理
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/projects` | 创建项目 |
| GET | `/api/projects/{id}` | 获取项目详情 |
| GET | `/api/projects` | 获取项目列表（分页） |
| PUT | `/api/projects/{id}` | 更新项目 |
| DELETE | `/api/projects/{id}` | 删除项目 |

### 任务管理
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/projects/{id}/tasks` | 启动生成任务 |
| GET | `/api/tasks/{id}` | 获取任务状态 |
| GET | `/api/tasks` | 获取任务列表（分页） |
| POST | `/api/tasks/{id}/cancel` | 取消任务 |

### 风格模板
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/styles` | 获取风格列表 |
| GET | `/api/styles/system` | 获取系统内置风格 |
| GET | `/api/styles/{id}` | 获取风格详情 |
| POST | `/api/styles` | 创建自定义风格 |
| PUT | `/api/styles/{id}` | 更新风格 |
| DELETE | `/api/styles/{id}` | 删除风格 |

### 文件上传
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/upload/novel` | 上传小说文件 |
| POST | `/api/projects/{id}/upload` | 上传项目小说文件 |

### WebSocket
| 端点 | 说明 |
|------|------|
| `ws://localhost:8080/ws` | WebSocket 连接端点 |
| `/topic/progress/{taskId}` | 订阅任务进度 |
| `/app/progress` | 发送进度消息 |

## 项目结构

```
cinestory/
├── cinestory-backend/           # Spring Boot 后端
│   ├── src/main/java/
│   │   └── com/cinestory/
│   │       ├── config/          # 配置类
│   │       ├── controller/      # REST API
│   │       ├── service/         # 业务服务
│   │       │   ├── text/        # 文本切片
│   │       │   ├── prompt/      # 提示词生成
│   │       │   ├── video/       # 视频生成
│   │       │   └── workflow/    # 工作流编排
│   │       ├── model/           # 数据模型
│   │       └── repository/      # 数据访问
│   └── src/main/resources/
│       ├── application.yml      # 应用配置
│       └── db/migration/        # 数据库迁移
├── cinestory-frontend/          # 前端
├── docker/                      # Docker 配置
└── docker-compose.yml           # 编排配置
```

## 配置说明

### 视频生成 API

在 `.env` 文件中配置视频生成 API 密钥：

```bash
RUNWAY_API_KEY=your_key_here
PIKA_API_KEY=your_key_here
LUMA_API_KEY=your_key_here
```

### LLM 配置

```yaml
llm:
  provider: openai  # 或其他提供商
  api-key: ${LLM_API_KEY}
  model: gpt-4
```

## 支持的视频生成源

| 提供商 | 模型 | 时长 | 状态 |
|--------|------|------|------|
| Runway ML | Gen-3 Alpha | 5-18s | 支持 |
| Pika Labs | Pika-1.0 | 3-4s | 支持 |
| Luma AI | Dream Machine | 5s | 支持 |
| 可灵 AI | Kling | 5-10s | 计划中 |
| 自部署 | SVD | 自定义 | 计划中 |

## 内置风格

- 日式动漫风
- 3D 动漫风
- 水墨国风
- 赛博朋克
- 吉卜力风

## 开发路线图

### 后端
- [x] 基础项目结构
- [x] 数据库设计（Flyway 迁移）
- [x] 文本切片模块
- [x] 风格模板管理
- [x] 文件上传与解析
- [x] WebSocket 进度推送
- [x] REST API 控制器
- [x] 异常处理机制
- [x] API 文档（Swagger）
- [ ] 提示词生成模块（LLM 集成）
- [ ] 视频生成 API 集成
- [ ] 视频拼接模块（FFmpeg）
- [ ] AI 配音（TTS）

### 前端
- [x] 项目结构搭建
- [x] WebSocket 依赖配置
- [ ] 页面组件开发
- [ ] 状态管理
- [ ] 视频播放器集成
- [ ] 进度可视化

### 基础设施
- [x] Docker 容器化
- [x] MySQL 持久化
- [x] Redis 缓存
- [x] MinIO 对象存储
- [x] 健康检查
- [ ] CI/CD 流水线
- [ ] 监控告警

## 贡献指南

欢迎提交 Issue 和 Pull Request！

## 许可证

© [2026] [gerrymc]。保留所有权利。未经许可，不得用于商业用途。
