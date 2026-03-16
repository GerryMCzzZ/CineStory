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
- **用户认证**：JWT 认证，配额管理，API Key

### 技术亮点
- Java 17 + Spring Boot 3.x 后端
- Vue 3 + Vite + Pinia 前端
- Spring Security + JWT 认证
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

# 配置环境变量（可选，默认配置可直接启动）
cp cinestory-backend/.env.example cinestory-backend/.env
cp cinestory-frontend/.env.example cinestory-frontend/.env.local

# 使用 Makefile 启动（推荐）
make build    # 构建镜像
make up       # 启动所有服务
make logs     # 查看日志

# 或直接使用 docker-compose
docker-compose up -d --build
docker-compose logs -f backend
```

### 服务地址

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端 | http://localhost:3000 | Vue 前端应用 |
| 后端 API | http://localhost:8080/api | Spring Boot API |
| Swagger 文档 | http://localhost:8080/api/swagger-ui.html | API 文档 |
| MinIO 控制台 | http://localhost:9001 | 对象存储管理 |

### 默认账号

- 用户名: `admin`
- 密码: `admin123`

### Makefile 命令

| 命令 | 说明 |
|------|------|
| `make help` | 显示所有可用命令 |
| `make build` | 构建所有 Docker 镜像 |
| `make up` | 启动所有服务 |
| `make down` | 停止所有服务 |
| `make up-infra` | 仅启动基础设施 (MySQL, Redis, MinIO) |
| `make restart` | 重启所有服务 |
| `make logs` | 查看所有服务日志 |
| `make logs-backend` | 查看后端服务日志 |
| `make clean` | 清理容器和卷 |
| `make health-backend` | 检查后端健康状态 |
| `make show-users` | 查看数据库用户 |
| `make reset-db` | 重置数据库（危险操作） |

### 本地开发

**启动基础设施**
```bash
make up-infra
```

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

启动服务后访问 http://localhost:8080/api/swagger-ui.html 查看完整的 API 文档。

### 认证 API
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/register` | 用户注册 |
| POST | `/api/auth/login` | 用户登录 |
| POST | `/api/auth/logout` | 用户登出 |
| POST | `/api/auth/refresh` | 刷新 Token |
| GET | `/api/auth/me` | 获取当前用户信息 |
| PUT | `/api/auth/password` | 修改密码 |
| PATCH | `/api/auth/profile` | 更新用户信息 |
| POST | `/api/auth/api-key/regenerate` | 重新生成 API Key |
| PUT | `/api/auth/api-key/toggle` | 切换 API Key 状态 |

### 项目管理
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/projects` | 创建项目 | 必需 |
| GET | `/api/projects/{id}` | 获取项目详情 | - |
| GET | `/api/projects` | 获取项目列表（分页） | - |
| PUT | `/api/projects/{id}` | 更新项目 | 必需 |
| DELETE | `/api/projects/{id}` | 删除项目 | 必需 |

### 任务管理
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/projects/{id}/tasks` | 启动生成任务 | 必需 |
| GET | `/api/tasks/{id}` | 获取任务状态 | 必需 |
| GET | `/api/tasks` | 获取任务列表（分页） | 必需 |
| POST | `/api/tasks/{id}/cancel` | 取消任务 | 必需 |

### 风格模板
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/styles` | 获取风格列表 | - |
| GET | `/api/styles/system` | 获取系统内置风格 | - |
| GET | `/api/styles/{id}` | 获取风格详情 | - |
| POST | `/api/styles` | 创建自定义风格 | 必需 |
| PUT | `/api/styles/{id}` | 更新风格 | 必需 |
| DELETE | `/api/styles/{id}` | 删除风格 | 必需 |

### 文件上传
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/upload/novel` | 上传小说文件 | 必需 |
| POST | `/api/projects/{id}/upload` | 上传项目小说文件 | 必需 |

### WebSocket
| 端点 | 说明 |
|------|------|
| `ws://localhost:8080/api/ws` | WebSocket 连接端点 |
| `/topic/progress/{projectId}` | 订阅项目进度 |
| `/topic/progress/{taskId}` | 订阅任务进度 |

## 项目结构

```
cinestory/
├── cinestory-backend/           # Spring Boot 后端
│   ├── src/main/java/
│   │   └── com/cinestory/
│   │       ├── config/          # 配置类 (Security, JWT, WebSocket)
│   │       ├── controller/      # REST API
│   │       ├── service/         # 业务服务
│   │       │   ├── auth/        # 认证服务
│   │       │   ├── text/        # 文本切片
│   │       │   ├── prompt/      # 提示词生成
│   │       │   ├── llm/         # LLM 集成
│   │       │   ├── video/       # 视频生成
│   │       │   │   └── provider/ # Runway, Pika, Luma
│   │       │   ├── workflow/    # 工作流编排
│   │       │   ├── file/        # 文件解析
│   │       │   └── websocket/   # WebSocket 服务
│   │       ├── model/           # 数据模型 (Entity, DTO)
│   │       ├── repository/      # 数据访问
│   │       └── config/          # Spring 配置
│   └── src/main/resources/
│       ├── application.yml      # 应用配置
│       └── db/migration/        # 数据库迁移
├── cinestory-frontend/          # Vue 3 前端
│   ├── src/
│   │   ├── api/                # API 客户端
│   │   ├── components/         # Vue 组件
│   │   ├── views/              # 页面视图
│   │   ├── store/              # Pinia 状态管理
│   │   ├── router/             # Vue Router 配置
│   │   ├── utils/              # 工具函数
│   │   └── composables/        # 组合式函数
│   └── package.json
├── docker-compose.yml           # Docker 编排
├── Makefile                     # 快捷命令
└── README.md
```

## 配置说明

### 环境变量配置

**后端** (`cinestory-backend/.env`)

```bash
# 数据库
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/cinestory
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=your_password

# JWT
JWT_SECRET=your_very_long_and_secure_secret_key

# 视频生成 API
RUNWAY_API_KEY=your_runway_key
PIKA_API_KEY=your_pika_key
LUMA_API_KEY=your_luma_key

# LLM (可选)
LLM_ENABLED=true
LLM_API_KEY=your_openai_key
LLM_BASE_URL=https://api.openai.com/v1

# MinIO
MINIO_ENDPOINT=localhost:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin
```

**前端** (`cinestory-frontend/.env.local`)

```bash
VITE_API_HOST=localhost:8080
VITE_API_BASE_URL=http://localhost:8080/api
VITE_WS_BASE_URL=ws://localhost:8080/api/ws
```

### 视频生成 API 配置

在 `.env` 文件中配置视频生成 API 密钥：

```bash
# Runway ML - https://dev.runwayml.com/
RUNWAY_API_KEY=your_key_here

# Pika Labs - https://pika.art/
PIKA_API_KEY=your_key_here

# Luma AI - https://lumalabs.ai/
LUMA_API_KEY=your_key_here
```

### LLM 配置

支持 OpenAI 兼容的 API（包括国内 LLM 服务）：

```bash
# OpenAI
LLM_ENABLED=true
LLM_BASE_URL=https://api.openai.com/v1
LLM_MODEL=gpt-4

# 阿里云通义千问
LLM_BASE_URL=https://dashscope.aliyuncs.com/compatible-mode/v1
LLM_MODEL=qwen-max

# 智谱 GLM
LLM_BASE_URL=https://open.bigmodel.cn/api/paas/v4
LLM_MODEL=glm-4

# 百度千帆
LLM_BASE_URL=https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop
LLM_MODEL=ernie-4.0-8k
```

## 支持的视频生成源

| 提供商 | 模型 | 时长 | 状态 | 获取 API Key |
|--------|------|------|------|-------------|
| Runway ML | Gen-3 Alpha | 5-18s | 支持 | https://dev.runwayml.com/ |
| Pika Labs | Pika-1.0 | 3-4s | 支持 | https://pika.art/ |
| Luma AI | Dream Machine | 5s | 支持 | https://lumalabs.ai/ |
| 可灵 AI | Kling | 5-10s | 计划中 | https://klingai.com/ |

## 内置风格

- 日式动漫风 - 传统日本动漫风格
- 3D 动漫风 - 现代立体动画风格
- 水墨国风 - 中国传统水墨画风格
- 赛博朋克 - 未来科技感风格
- 吉卜力风 - 宫崎骏吉卜力风格

## 开发路线图

### 后端
- [x] 基础项目结构
- [x] 数据库设计（Flyway 迁移）
- [x] 用户认证系统 (JWT)
- [x] 文本切片模块
- [x] 风格模板管理
- [x] 文件上传与解析
- [x] WebSocket 进度推送
- [x] REST API 控制器
- [x] 异常处理机制
- [x] API 文档（Swagger）
- [x] 提示词生成模块（LLM 集成）
- [x] 视频生成 API 集成
- [x] 视频拼接模块（FFmpeg）
- [ ] AI 配音（TTS）
- [ ] 字幕生成
- [ ] 背景音乐添加

### 前端
- [x] 项目结构搭建
- [x] WebSocket 依赖配置
- [x] 页面组件开发
- [x] 状态管理 (Pinia)
- [x] 视频播放器集成
- [x] 进度可视化
- [x] 用户认证页面
- [x] 个人中心
- [ ] 项目编辑功能
- [ ] 视频预览增强
- [ ] 批量操作

### 基础设施
- [x] Docker 容器化
- [x] MySQL 持久化
- [x] Redis 缓存
- [x] MinIO 对象存储
- [x] 健康检查
- [ ] CI/CD 流水线
- [ ] 监控告警
- [ ] 日志聚合

## 常见问题

### Q: 如何重置数据库？

```bash
make reset-db
```

### Q: 如何查看所有用户？

```bash
make show-users
```

### Q: 前端无法连接后端？

检查 `.env.local` 中的 `VITE_API_BASE_URL` 是否正确。

### Q: MinIO 连接失败？

确保 MinIO 容器已启动：
```bash
docker-compose ps minio
```

访问 http://localhost:9001 检查 MinIO 控制台。

## 贡献指南

欢迎提交 Issue 和 Pull Request！

## 许可证

© [2026] [gerrymc]。保留所有权利。未经许可，不得用于商业用途。
