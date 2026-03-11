# CineStory

> 小说转动漫视频生成工具

输入小说文本，自动生成动漫风格的短视频。支持可配置的风格模板、多种视频生成源和智能文本切片。

## 功能特性

### 核心功能
- **智能文本切片**：多级切片策略，保持语义完整和上下文连贯
- **提示词生成**：基于 LLM 自动生成视频生成提示词
- **多源视频生成**：支持 Runway、Pika、Luma 等多种 API
- **视频拼接**：自动拼接视频片段，支持字幕和转场效果
- **风格配置**：内置多种动漫风格，支持自定义

### 技术亮点
- Java 17 + Spring Boot 3.x 后端
- Vue 3 / React 前端
- Docker 容器化部署
- WebSocket 实时进度推送
- 异步任务处理（Spring Batch）
- MySQL + Redis + MinIO 存储

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

# 启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f backend
```

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

### 项目管理
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/projects` | 创建项目 |
| GET | `/api/projects/{id}` | 获取项目详情 |
| GET | `/api/projects` | 获取项目列表 |
| DELETE | `/api/projects/{id}` | 删除项目 |

### 任务管理
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/projects/{id}/tasks` | 启动生成任务 |
| GET | `/api/tasks/{id}` | 获取任务状态 |
| GET | `/api/tasks/{id}/progress` | 获取进度详情 |
| POST | `/api/tasks/{id}/cancel` | 取消任务 |

### 风格模板
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/styles` | 获取风格列表 |
| GET | `/api/styles/{id}` | 获取风格详情 |
| POST | `/api/styles` | 创建自定义风格 |

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

- [x] 基础项目结构
- [x] 数据库设计
- [x] 文本切片模块
- [ ] 提示词生成模块
- [ ] 视频生成 API 集成
- [ ] 视频拼接模块
- [ ] 前端界面
- [ ] WebSocket 进度推送
- [ ] AI 配音（TTS）

## 贡献指南

欢迎提交 Issue 和 Pull Request！

## 许可证

© [2026] [gerrymc]。保留所有权利。未经许可，不得用于商业用途。
