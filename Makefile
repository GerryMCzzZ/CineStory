.PHONY: help build up down restart logs clean install-backend install-frontend

# 默认目标
help:
	@echo "CineStory Docker 管理命令"
	@echo ""
	@echo "使用方法: make [target]"
	@echo ""
	@echo "可用命令:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}'

# 构建镜像
build: ## 构建所有 Docker 镜像
	docker-compose build

# 构建后端镜像
build-backend: ## 构建后端 Docker 镜像
	docker-compose build backend

# 构建前端镜像
build-frontend: ## 构建前端 Docker 镜像
	docker-compose build frontend

# 启动服务
up: ## 启动所有服务
	docker-compose up -d

# 启动并查看日志
up-logs: ## 启动服务并查看日志
	docker-compose up

# 停止服务
down: ## 停止所有服务
	docker-compose down

# 重启服务
restart: ## 重启所有服务
	docker-compose restart

# 查看日志
logs: ## 查看所有服务日志
	docker-compose logs -f

logs-backend: ## 查看后端服务日志
	docker-compose logs -f backend

logs-frontend: ## 查看前端服务日志
	docker-compose logs -f frontend

# 清理
clean: ## 清理容器和卷
	docker-compose down -v

# 清理镜像
clean-all: ## 清理容器、卷和镜像
	docker-compose down -v
	docker system prune -a

# 安装后端依赖
install-backend: ## 安装后端依赖
	cd cinestory-backend && mvn clean install

# 安装前端依赖
install-frontend: ## 安装前端依赖
	cd cinestory-frontend && npm install

# 仅启动基础设施
up-infra: ## 仅启动基础设施服务 (MySQL, Redis, MinIO)
	docker-compose up -d mysql redis minio

# 运行后端（开发模式）
run-backend: ## 在本地运行后端（需要先安装依赖）
	cd cinestory-backend && mvn spring-boot:run

# 运行前端（开发模式）
run-frontend: ## 在本地运行前端（需要先安装依赖）
	cd cinestory-frontend && npm run dev

# 进入后端容器
shell-backend: ## 进入后端容器
	docker-compose exec backend sh

# 进入 MySQL 容器
shell-mysql: ## 进入 MySQL 容器
	docker-compose exec mysql mysql -uroot -pcinestory123

# 查看服务状态
ps: ## 查看所有容器状态
	docker-compose ps

# 数据库迁移
migrate: ## 运行数据库迁移
	docker-compose exec backend mvn flyway:migrate

# 查看后端健康状态
health-backend: ## 检查后端服务健康状态
	@curl -f http://localhost:8080/api/actuator/health || echo "Backend not healthy"

# 重新构建并启动
rebuild: ## 重新构建并启动服务
	docker-compose up -d --build

# 生产部署
deploy: ## 生产环境部署
	docker-compose -f docker-compose.yml --env-file .env.prod up -d --build
