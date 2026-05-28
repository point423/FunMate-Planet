# Linux 本地 Docker 与云部署测试指南

本文只记录测试步骤，避免在聊天记录里翻找。当前项目保留原有本地 Docker 保底部署链路，云部署使用单独的 `Dockerfile.railway` 和 `railway.toml`。

## 一、本地 Docker 保底部署测试

这套流程使用原来的本地部署文件：

- `backend/Dockerfile`
- `frontend/Dockerfile`
- `frontend/nginx.conf`
- `docker-compose.yml`

### 1. 前置条件

Linux 机器需要安装：

```bash
java -version
docker --version
docker compose version
```

后端 Dockerfile 依赖本机先打好的 jar，所以先打包后端：

```bash
cd backend
mvn clean package -DskipTests
```

### 2. 启动本地 Docker Compose

```bash
cd ..
docker compose up -d --build
docker compose ps
```

如果想看日志：

```bash
docker compose logs -f backend
docker compose logs -f frontend
```

### 3. 检查服务

后端健康检查：

```bash
curl http://localhost:8080/health
```

预期返回：

```json
{"status":"UP"}
```

数据库和 Redis 连接检查：

```bash
curl http://localhost:8080/api/test/connection/all
```

前端检查：

```bash
curl -I http://localhost:3000
```

浏览器访问：

```text
http://localhost:3000
```

### 4. 停止本地环境

```bash
docker compose down
```

如果要连数据卷一起清掉：

```bash
docker compose down -v
```

## 二、本地模拟云部署后端

这套流程专门测试云部署文件：

- `Dockerfile.railway`
- `railway.toml`

不会使用原来的 `backend/Dockerfile`。

### 1. 构建 Railway 后端镜像

在项目根目录执行：

```bash
DOCKER_BUILDKIT=1 docker build --network=host --progress=plain -f Dockerfile.railway -t funmate-backend:railway .
```

`Dockerfile.railway` 使用 `docker/maven-settings.xml` 中的 Maven 镜像源，并启用了 BuildKit 的 `/root/.m2/repository` 缓存。第一次构建仍需要下载依赖，后续构建会复用缓存。

如果卡在 `mvn dependency:go-offline` 很久，先验证容器内 Maven 网络：

```bash
docker run --rm \
  -v "$PWD/docker/maven-settings.xml:/tmp/settings.xml:ro" \
  maven:3.9.9-eclipse-temurin-21 \
  bash -lc "mvn -B -ntp -s /tmp/settings.xml help:effective-settings"
```

清理损坏的 BuildKit 缓存后重试：

```bash
docker builder prune -f
DOCKER_BUILDKIT=1 docker build --progress=plain -f Dockerfile.railway -t funmate-backend:railway .
```

### 2. 启动本地 MySQL 和 Redis 依赖

```bash
docker compose up -d mysql redis
```

获取 Compose 网络名：

```bash
NETWORK_NAME=$(docker inspect fm-mysql --format '{{range $name, $_ := .NetworkSettings.Networks}}{{println $name}}{{end}}' | head -n 1)
echo "$NETWORK_NAME"
```

### 3. 运行云部署镜像

```bash
docker run --rm --network "$NETWORK_NAME" -p 8081:8080 \
  -e PORT=8080 \
  -e DB_HOST=fm-mysql \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=1234567 \
  -e REDIS_URL=redis://fm-redis:6379 \
  -e JWT_SECRET=replace_with_a_32_char_or_longer_secret \
  -e CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000 \
  -e UPLOAD_PUBLIC_URL=http://localhost:8081/static/ \
  funmate-backend:railway
```

### 4. 检查云镜像

新开一个终端执行：

```bash
curl http://localhost:8081/health
curl http://localhost:8081/api/test/connection/all
```

## 三、Vercel 前端构建测试

在 Linux 本地模拟 Vercel 构建：

```bash
cd frontend
npm ci
VITE_API_BASE_URL=https://your-railway-backend.up.railway.app/api npm run build -- --mode production
```

如果只是本地 Docker Compose 前端，不需要设置 `VITE_API_BASE_URL`，因为本地 Nginx 仍然代理到 `backend:8080`。

## 四、真正云部署配置

### 1. Railway 后端

Railway 后端使用：

- `railway.toml`
- `Dockerfile.railway`

需要配置的环境变量：

```env
JWT_SECRET=replace_with_a_32_char_or_longer_secret
CORS_ALLOWED_ORIGINS=https://your-vercel-app.vercel.app
UPLOAD_PUBLIC_URL=https://your-railway-backend.up.railway.app/static/
NODE_ENV=production
```

数据库和 Redis 推荐使用 Railway 插件自动注入。

如果 Railway 没有自动注入 JDBC 格式的 `DATABASE_URL`，后端也支持这些变量：

```env
MYSQLHOST=xxx
MYSQLPORT=3306
MYSQLDATABASE=funmate_planet
MYSQLUSER=root
MYSQLPASSWORD=xxx
REDIS_URL=redis://xxx:6379
```

### 2. Vercel 前端

Vercel 读取：

- `vercel.json`

需要配置：

```env
VITE_API_BASE_URL=https://your-railway-backend.up.railway.app/api
```

## 五、云端上线后验证

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

1. 打开 Vercel 前端域名。
2. 打开开发者工具 Network。
3. 登录或注册。
4. 确认接口请求地址是：

```text
https://your-railway-backend.up.railway.app/api/...
```

如果浏览器报 CORS 错误，检查 Railway 后端环境变量：

```env
CORS_ALLOWED_ORIGINS=https://your-vercel-app.vercel.app
```

多个域名用英文逗号分隔：

```env
CORS_ALLOWED_ORIGINS=https://your-vercel-app.vercel.app,http://localhost:5173
```
