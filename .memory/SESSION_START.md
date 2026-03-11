# CineStory 项目记忆系统 - 会话恢复指南

> 当你开始一个新的会话，或者有新的协作者加入时，请按以下步骤快速了解项目状态。

---

## 🚀 快速开始（3 分钟上手）

### 第一步：阅读项目状态摘要

**读取文件**: `.memory/index.md`

这会告诉你：
- 项目是做什么的
- 当前进度如何
- 最近在做什么

### 第二步：查看待办事项

**读取文件**: `.memory/progress/pending.md`

这会告诉你：
- 有哪些高优先级任务需要完成
- 当前阻塞的问题是什么

### 第三步：了解架构决策

**读取文件**: `.memory/decisions/architecture.md`

这会告诉你：
- 为什么选择这个技术栈
- 重要的架构决策及其理由

---

## 📋 给 AI 的会话开始提示词

当你开始一个新的 AI 会话时，请使用以下提示词：

```
请先阅读 .memory/index.md 文件，了解 CineStory 项目的整体状态。

然后阅读以下文件，获取完整上下文：
1. .memory/progress/completed.md - 已完成的功能
2. .memory/progress/in_progress.md - 进行中的任务
3. .memory/progress/pending.md - 待办事项
4. .memory/progress/blocked.md - 阻塞问题
5. .memory/decisions/architecture.md - 架构决策

阅读完成后，请用以下格式回复我：
=== 项目状态摘要 ===
- 项目目标: [一句话描述]
- 当前进度: [百分比]
- 最近完成: [最近的 1-2 个完成项]
- 当前任务: [当前应该做什么]
- 阻塞问题: [如有]
- 下一步建议: [1-2 条具体建议]
=== 结束 ===
```

---

## 👥 给协作者的入门指南

### 新协作者入职清单

- [ ] 阅读 `.memory/index.md` - 项目概览
- [ ] 阅读 `.memory/decisions/architecture.md` - 技术架构
- [ ] 阅读 `README.md` - 本地开发环境搭建
- [ ] 查看 `.memory/progress/in_progress.md` - 当前任务
- [ ] 查看 `.memory/progress/blocked.md` - 待解决问题

### 技术栈速览

| 层级 | 技术 | 说明 |
|------|------|------|
| 后端 | Java 17 + Spring Boot 3.x | 主框架 |
| 数据库 | MySQL 8.0 | 持久化 |
| 缓存 | Redis | 任务队列 |
| 存储 | MinIO | 文件存储 |
| 容器 | Docker Compose | 开发环境 |

### 关键代码路径

```
cinestory-backend/src/main/java/com/cinestory/
├── service/          # 业务逻辑（重点）
│   ├── text/         # 文本切片（已完成）
│   ├── prompt/       # 提示词生成（待开发）
│   ├── video/        # 视频生成（待开发）
│   └── memory/       # 记忆系统（已完成）
├── model/            # 数据模型
└── repository/       # 数据访问
```

---

## 🔄 会话结束时

### AI 应该做的事情

1. **更新进度**: 将完成的功能记录到 `.memory/progress/completed.md`
2. **记录决策**: 重要的技术决策记录到 `.memory/decisions/architecture.md`
3. **记录问题**: 遇到的阻塞问题记录到 `.memory/progress/blocked.md`
4. **创建会话记录**: 在 `.memory/sessions/` 创建今日会话记录

### 使用 MemoryService API

```java
// 记录完成的功能
memoryService.recordCompleted("提示词生成", "PromptGenerationService",
    "实现基于 LLM 的提示词生成服务",
    List.of("PromptGenerationService.java", "LLMClient.java"));

// 记录会话
SessionRecord record = SessionRecord.createCurrent("实现提示词生成模块");
record.getCompletedItems().add("完成 PromptGenerationService");
record.getIssues().add("LLM API 调用超时问题需要优化");
record.getNextSteps().add("添加重试机制");
memoryService.recordSession(record);
```

---

## 📊 项目状态速查

### 当前状态（2025-03-11）

| 维度 | 状态 | 完成度 |
|------|------|--------|
| 整体进度 | 基础搭建中 | 20% |
| 后端开发 | 基础 + 记忆系统 | 35% |
| 前端开发 | 未开始 | 0% |

### 下一步应该做什么

**优先级 P0（必须完成）**:
1. 实现提示词生成服务（PromptGenerationService）
2. 集成 LLM API
3. 实现视频生成服务集成

**优先级 P1（改进体验）**:
4. 添加 REST API 控制器
5. 实现视频拼接模块

---

## 💡 记忆系统使用技巧

### 查看项目状态
```bash
# Linux/Mac
cat .memory/index.md

# Windows
type .memory\index.md
```

### 搜索历史记录
```bash
# 查找所有已完成的功能
grep -r "\- \[x\]" .memory/progress/

# 查找所有架构决策
grep -r "ADR-" .memory/decisions/
```

### 生成进度报告
```bash
# 查看最近更新的文件
ls -lt .memory/*/ | head -20
```

---

## 🔧 维护记忆系统

### 记忆文件更新频率

| 文件 | 更新频率 | 负责人 |
|------|----------|--------|
| index.md | 每次会话结束 | AI |
| completed.md | 完成功能时 | AI |
| in_progress.md | 每次会话 | AI |
| pending.md | 每周 | AI/人工 |
| blocked.md | 发现问题时 | AI |
| decisions/architecture.md | 做决策时 | AI |
| sessions/*.md | 每次会话结束 | AI |

### 保持记忆准确性

1. **及时更新**: 完成功能后立即记录
2. **保持同步**: 代码变更后同步更新记忆
3. **定期审查**: 每周审查一次记忆准确性
4. **版本控制**: 记忆文件纳入 Git 管理

---

**文档创建时间**: 2025-03-11
**最后更新**: 2025-03-11
