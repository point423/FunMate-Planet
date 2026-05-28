# FunMate Planet 云服务部署说明

## 平台选择

本项目采用前后端分离部署：

- 前端：Vercel，负责托管 `frontend/dist` 静态资源。
- 后端：Railway，使用 `Dockerfile.railway` 从源码构建 Spring Boot 服务。
- 数据库：Railway MySQL 插件。
- Redis：Railway Redis 插件。

这样可以保留现有 Vue + Spring Boot + MySQL + Redis 架构，同时满足云平台环境变量、自动构建和健康检查要求。

## 部署配置文件

- `vercel.json`：前端 Vercel 构建配置。
- `railway.toml`：后端 Railway Docker 构建和健康检查配置。
- `Dockerfile.railway`：Railway 从仓库根目录构建后端服务时使用。
- `backend/Dockerfile`：保留原有本地 Docker Compose 构建方式，从 `backend/target` 拷贝已打包 jar。
- `frontend/nginx.conf`：保持原本本地 Docker Compose 的 `backend:8080` 服务名代理，不作为云部署入口。

## 后端环境变量

Railway 后端服务需要配置：

| 变量名 | 示例 | 说明 |
| --- | --- | --- |
| `PORT` | `8080` | 服务监听端口，Railway 可自动注入 |
| `DATABASE_URL` | `jdbc:mysql://host:3306/funmate_planet?...` | JDBC 数据库连接，设置后优先使用 |
| `MYSQLHOST` / `MYSQLPORT` / `MYSQLDATABASE` | Railway 自动提供 | 未设置 `DATABASE_URL` 时使用 |
| `MYSQLUSER` / `MYSQLPASSWORD` | Railway 自动提供 | 数据库账号和密码 |
| `REDIS_URL` | `redis://default:password@host:port` | Redis 连接地址 |
| `JWT_SECRET` | 至少 32 位随机字符串 | JWT 签名密钥 |
| `CORS_ALLOWED_ORIGINS` | `https://your-app.vercel.app` | 前端域名白名单，多个域名用英文逗号分隔 |
| `UPLOAD_DIR` | `uploads/` | 上传文件目录 |
| `UPLOAD_PUBLIC_URL` | `https://your-api.up.railway.app/static/` | 上传文件公开访问前缀 |
| `AI_API_KEY` / `API_KEY` | `your_api_key` | 第三方 AI 服务密钥 |
| `AI_BASE_URL` | `http://ollama:11434/v1` | AI 服务地址 |
| `AI_MODEL` | `qwen2.5:1.5b` | AI 模型名称 |
| `NODE_ENV` | `production` | 运行环境 |

如果使用 Railway MySQL 插件，推荐不手写 `DATABASE_URL`，直接使用 Railway 自动注入的 `MYSQLHOST`、`MYSQLPORT`、`MYSQLDATABASE`、`MYSQLUSER`、`MYSQLPASSWORD`。

## 前端环境变量

Vercel 前端项目需要配置：

| 变量名 | 示例 | 说明 |
| --- | --- | --- |
| `VITE_API_BASE_URL` | `https://your-api.up.railway.app/api` | 前端请求后端 API 的基础地址 |

## Railway 后端部署步骤

1. 在 Railway 新建 Project，并连接 GitHub 仓库。
2. 新建后端服务，部署来源选择当前仓库。
3. 确认服务使用根目录下的 `railway.toml`，并通过 `Dockerfile.railway` 构建。
4. 添加 MySQL 插件和 Redis 插件。
5. 在后端服务中配置 `JWT_SECRET`、`CORS_ALLOWED_ORIGINS`、`UPLOAD_PUBLIC_URL`、AI 相关变量。
6. 推送 `main` 分支后触发自动部署。
7. 部署完成后访问 `/health`，返回 `{"status":"UP"}` 表示服务可用。

## Vercel 前端部署步骤

1. 在 Vercel 导入 GitHub 仓库。
2. 保持根目录为仓库根目录，Vercel 会读取 `vercel.json`。
3. 配置 `VITE_API_BASE_URL=https://your-api.up.railway.app/api`。
4. 推送 `main` 分支后触发自动部署。
5. 部署完成后访问 Vercel 域名，检查登录、注册、活动、聊天等页面是否能调用后端接口。

## 自动部署

- GitHub 推送到 `main` 后，Vercel 自动构建前端。
- GitHub 推送到 `main` 后，Railway 自动构建后端 Docker 镜像并重启服务。
- 项目已有 GitHub Actions，可继续用于测试、镜像构建和安全扫描。

## 验证清单

- 后端 `/health` 返回 `UP`。
- 后端 `/api/test/connection/all` 能检查 MySQL 和 Redis 连接。
- 前端页面可在线访问。
- 前端接口请求指向 Railway 后端域名。
- `JWT_SECRET`、数据库密码、AI Key 只存在云平台环境变量中，不提交到仓库。
- `CORS_ALLOWED_ORIGINS` 包含 Vercel 前端域名。

## Linux 本地验证

### 1. 保底 Docker Compose 验证

该流程使用项目原本的 `backend/Dockerfile`、`frontend/Dockerfile`、`frontend/nginx.conf` 和 `docker-compose.yml`。

```bash
cd backend
./mvnw clean package -DskipTests

cd ..
docker compose up -d --build
docker compose ps
```

检查后端：

```bash
curl http://localhost:8080/health
curl http://localhost:8080/api/test/connection/all
```

检查前端：

```bash
curl -I http://localhost:3000
```

浏览器访问：

```text
http://localhost:3000
```

### 2. 本地非 Docker 验证

需要本机已启动 MySQL 和 Redis，或先用 Compose 启动依赖：

```bash
docker compose up -d mysql redis
```

启动后端：

```bash
cd backend
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=funmate_planet
export DB_USERNAME=root
export DB_PASSWORD=1234567
export REDIS_URL=redis://localhost:6379
export JWT_SECRET=replace_with_a_32_char_or_longer_secret
export CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000
./mvnw spring-boot:run
```

启动前端：

```bash
cd frontend
npm ci
npm run dev
```

浏览器访问：

```text
http://localhost:5173
```

## 云部署前验证

### 1. Railway 后端镜像构建验证

这个流程只验证云部署专用的 `Dockerfile.railway`，不会使用本地保底 Dockerfile。

```bash
docker build -f Dockerfile.railway -t funmate-backend:railway .
```

如需在本地模拟运行云镜像，可先启动 MySQL 和 Redis：

```bash
docker compose up -d mysql redis
```

然后运行云镜像：

```bash
docker run --rm --network funmate-planet_default -p 8081:8080 \
  -e PORT=8080 \
  -e DB_HOST=mysql \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=1234567 \
  -e REDIS_URL=redis://redis:6379 \
  -e JWT_SECRET=replace_with_a_32_char_or_longer_secret \
  -e CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000 \
  funmate-backend:railway
```

如果 Compose 生成的网络名不同，先执行：

```bash
docker network ls
```

再把 `funmate-planet_default` 替换为实际网络名。

检查云镜像：

```bash
curl http://localhost:8081/health
curl http://localhost:8081/api/test/connection/all
```

### 2. Vercel 前端构建验证

```bash
cd frontend
npm ci
VITE_API_BASE_URL=https://your-railway-backend.up.railway.app/api npm run build -- --mode production
```

## 云端上线后验证

后端：

```bash
curl https://your-railway-backend.up.railway.app/health
curl https://your-railway-backend.up.railway.app/api/test/connection/all
```

前端：

```bash
curl -I https://your-vercel-app.vercel.app
```

浏览器检查：

- 打开 Vercel 前端域名。
- 注册或登录。
- 打开浏览器开发者工具 Network，确认接口请求地址是 `https://your-railway-backend.up.railway.app/api/...`。
- 如果出现跨域错误，检查 Railway 后端的 `CORS_ALLOWED_ORIGINS` 是否包含 Vercel 域名。
