# FunMate Planet

[![Build Status](https://github.com/point423/FunMate-Planet/actions/workflows/ci.yml/badge.svg)](https://github.com/point423/FunMate-Planet/actions/workflows/ci.yml)
[![Coverage](https://codecov.io/gh/point423/FunMate-Planet/branch/develop/graph/badge.svg)](https://codecov.io/gh/point423/FunMate-Planet)

## 项目名称

FunMate Planet 是一个基于兴趣匹配与地理位置发现的线下社交活动平台，帮助用户寻找附近搭子、发起活动、记录活动日记，并通过评价机制建立可信的社交关系。

## 技术栈

### 前端

- Vue 3
- TypeScript
- Vite
- Vue Router
- Pinia
- Axios
- Element Plus
- Vitest

### 后端

- Java 21
- Spring Boot 3
- Spring Data JPA
- MySQL
- Redis
- JWT
- Maven
- JUnit 5

### 工程化与部署

- GitHub Actions
- Docker
- Docker Compose
- Codecov

## 主要功能

- 用户注册、登录、退出登录，基于 JWT 的身份认证
- 用户资料维护，包括头像、标签、昵称等信息编辑
- 基于 Redis Geo 的附近用户发现与兴趣匹配
- 单向关注与双向好友关系管理
- 活动创建、加入、状态流转与参与管理
- 活动日记发布与多图内容展示
- 活动结束后的用户互评与评分排行
- 用户私聊与会话管理
- AI 助手能力接入
- 文件上传、健康检查、监控接口支持

## 目录结构

```text
FunMate-Planet/
├─ backend/                 # Spring Boot 后端服务
│  ├─ src/main/java/        # 控制器、服务、实体、仓储、配置
│  ├─ src/main/resources/   # 应用配置与日志配置
│  └─ src/test/java/        # 后端单元测试与接口测试
├─ frontend/                # Vue 3 前端应用
│  ├─ src/api/              # 前端 API 请求封装
│  ├─ src/views/            # 页面视图
│  ├─ src/components/       # 通用与业务组件
│  ├─ src/stores/           # Pinia 状态管理
│  └─ src/__tests__/        # 前端测试
├─ docs/                    # 架构、接口、部署、监控等文档
├─ docker/                  # Docker 相关配置
├─ uploads/                 # 上传文件目录
├─ docker-compose.yml       # 本地容器编排
├─ compose.prod.yaml        # 生产环境编排
└─ README.md
```

## 安装步骤

### 方式一：本地开发

#### 1. 克隆项目

```bash
git clone https://github.com/point423/FunMate-Planet.git
cd FunMate-Planet
```

#### 2. 配置环境变量

复制根目录的环境变量模板并按实际环境修改：

```bash
cp .env.example .env
```

关键配置项包括：

- `DATABASE_URL` / `DB_HOST` / `DB_PORT` / `DB_NAME` / `DB_USERNAME` / `DB_PASSWORD`
- `REDIS_URL` / `REDIS_HOST` / `REDIS_PORT`
- `JWT_SECRET`
- `VITE_API_BASE_URL`
- `AI_BASE_URL` / `AI_MODEL` / `AI_API_KEY`

#### 3. 启动后端

```bash
cd backend
mvn clean spring-boot:run
```

默认服务地址：

- 后端：`http://localhost:8080`
- API 前缀：`http://localhost:8080/api`

#### 4. 启动前端

```bash
cd frontend
npm ci
npm run dev
```

默认前端地址：

- 前端：`http://localhost:5173`

### 方式二：Docker 启动

在项目根目录执行：

```bash
docker compose up -d --build
```

如需先手动打包后端，也可以执行：

```bash
cd backend
mvn clean package -DskipTests
cd ..
docker compose up -d --build
```

## 常用命令

### 前端

```bash
cd frontend
npm run dev
npm run build
npm run lint
npm run test
```

### 后端

```bash
cd backend
mvn clean test
mvn spring-boot:run
```

## API 端点

基础信息：

- Base URL：`/api`
- 鉴权方式：`Authorization: Bearer <token>`
- 文档位置：`docs/api.md`
- OpenAPI 描述：`docs/api.yaml`

### 认证

- `POST /api/auth/register` 注册
- `POST /api/auth/login` 登录
- `POST /api/auth/logout` 退出登录

### 用户

- `GET /api/users/me` 获取当前用户信息
- `PUT /api/users/me` 更新当前用户资料
- `POST /api/users/location` 上报地理位置
- `GET /api/users/{id}` 获取指定用户主页

### 关注与好友

- `POST /api/users/{id}/follow` 关注或取消关注
- `GET /api/users/{id}/followers` 获取粉丝列表
- `GET /api/users/{id}/following` 获取关注列表
- `GET /api/friends` 获取好友列表
- `GET /api/friends/requests` 获取好友申请列表
- `POST /api/friends/requests` 发送好友申请
- `POST /api/friends/requests/{id}/handle` 处理好友申请

### 活动与日记

- `POST /api/activities` 创建活动
- `GET /api/activities` 获取活动列表
- `GET /api/activities/{id}` 获取活动详情
- `POST /api/activities/{id}/join` 加入活动
- `POST /api/activities/{id}/end` 结束活动
- `POST /api/diaries` 发布活动日记
- `GET /api/diaries` 获取日记列表
- `GET /api/diaries/{id}` 获取日记详情

### 社交互动

- `POST /api/evaluations` 提交活动评价
- `GET /api/chat/conversations` 获取会话列表
- `GET /api/chat/messages` 获取聊天记录
- `POST /api/chat/messages` 发送消息
- `GET /api/discover/nearby` 获取附近用户
- `GET /api/discover/random` 随机推荐用户
- `GET /api/discover/ranking` 获取评分排行

### 其他

- `POST /api/upload/image` 上传图片

## 测试与质量保障

- GitHub Actions 自动执行前后端 CI 流程
- 后端使用 JUnit 5 与 JaCoCo
- 前端使用 Vitest 进行测试与覆盖率统计
- 覆盖率结果通过 Codecov 展示

可用命令：

```bash
cd backend
mvn clean test

cd frontend
npm run test
```

## 成员贡献

### 郭盈盈

- 负责前端架构设计与页面实现，完成登录、注册、首页、聊天、活动、个人主页、他人主页、标签初始化等核心页面开发
- 完成前端组件与模块封装，包括 `AppNavbar`、`TagSelector`、`UserCard`、`DiaryEditor` 等通用组件
- 负责前端 API 请求层封装与后端接口对接，统一处理 `token` 注入、响应解包、登录失效跳转、上传请求兼容等逻辑
- 参与前端目录结构规划、路由组织、状态管理设计与交互流程梳理
- 参与编写和整理前端相关文档，包括 `docs/frontend.md`、`docs/design-spec.md` 等说明内容

### 彭静婷

- 负责后端架构设计与核心业务实现，完成认证、用户、活动、好友、日记、评价、聊天、发现、上传等模块开发
- 完成 Spring Boot 后端分层设计，涵盖 Controller、Service、Repository、Entity、配置与全局异常处理
- 负责数据库模型设计与 JPA 持久层实现，完成用户、活动、好友关系、活动日记、用户评价等核心实体建模
- 负责 JWT 认证、Redis Geo 附近用户发现、统一响应结构与异常处理机制实现
- 完成 Dockerfile、`docker-compose.yml`、本地联调与接口测试脚本配置，推进容器化部署与后端服务集成
- 参与编写和整理后端、架构、数据库、部署等文档内容

## 相关文档

- `docs/architecture.md` 系统架构说明
- `docs/api.md` 接口文档
- `docs/api.yaml` OpenAPI 描述文件
- `docs/deployment.md` 部署说明
- `docs/monitoring.md` 监控说明
