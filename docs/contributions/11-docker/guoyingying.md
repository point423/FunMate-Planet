# 前端 Docker 部署贡献说明

姓名：郭盈盈

学号：2312190102

日期：2026-05-20

## 我完成的工作

### 1. Dockerfile 编写

- [x] 前端 Dockerfile（`frontend/Dockerfile`）：使用 `node:20-alpine` 作为构建阶段，执行 `npm ci` 和 `npm run build --no-types` 生成前端静态资源。
- [x] 前端生产镜像（`nginx:1.25-alpine`）：将 `dist` 构建产物复制到 `/usr/share/nginx/html`，通过 Nginx 对外提供 Vue 前端页面。
- [x] 前端 `.dockerignore` 文件：排除 `node_modules/`、`dist/`、`.git/`、`.env.local`、`.vscode/` 等无关内容，减少前端镜像构建上下文。
- [x] 前端健康检查：在前端 Dockerfile 中通过 `wget -qO- http://localhost/` 检查 Nginx 首页是否可访问。

### 2. Compose 配置

- [x] 开发环境前端服务（`docker-compose.yml`）：配置 `frontend` 服务从 `./frontend` 构建镜像，并将宿主机 `3000` 端口映射到容器 `80` 端口。
- [x] 前端启动依赖：通过 `depends_on` 等待 `backend` 服务健康后再启动前端容器，避免页面访问时 API 代理目标未就绪。
- [x] 前端服务健康检查：在 `docker-compose.yml` 中使用 `wget -q --spider http://127.0.0.1/` 检查前端容器运行状态。
- [x] 生产环境前端服务（`compose.prod.yaml`）：配置前端使用 `ghcr.io/${GITHUB_REPOSITORY}/frontend:latest` 镜像，开启 `restart: unless-stopped`，并限制内存为 `128M`。

### 3. 前端 Nginx 配置

- [x] Vue Router 支持：在 `frontend/nginx.conf` 中配置 `try_files $uri $uri/ /index.html`，保证刷新二级路由时仍能回到前端应用。
- [x] API 反向代理：将 `/api/` 请求代理到 `http://backend:8080/api/`，使前端容器可以通过 Docker Compose 服务名访问后端。
- [x] 静态资源缓存：为 `js`、`css`、图片、`svg` 等静态资源配置长期缓存和 `Cache-Control: public, immutable`，提升前端页面加载效率。

### 4. 自动化部署配合

- 选择了选项 A：配合 GitHub Actions / GHCR 镜像发布方式。
- 具体内容：前端生产 Compose 配置中使用 `ghcr.io/${GITHUB_REPOSITORY}/frontend:latest`，负责前端镜像在生产部署中的服务接入和端口暴露。
- 与 pengjingting 文档区分：本文只说明前端 Dockerfile、前端 Nginx、前端 Compose 服务和前端访问验证；后端 Dockerfile、数据库、Redis、整体 CI 安全扫描不计入我的个人贡献。

## PR 链接

- 暂无单独 PR 链接；前端 Docker 内容对应仓库中的 `frontend/Dockerfile`、`frontend/.dockerignore`、`frontend/nginx.conf`、`docker-compose.yml` 和 `compose.prod.yaml`。

## 遇到的问题和解决

1. 问题：Vue 单页应用打包后，如果直接用 Nginx 提供静态文件，刷新 `/home`、`/activity` 等前端路由时可能出现 404。

   解决：在 Nginx 的 `/` 路由中配置 `try_files $uri $uri/ /index.html`，将未知路径回退到前端入口文件，由 Vue Router 接管路由解析。

2. 问题：前端容器内不能使用 `localhost:8080` 访问后端，因为容器中的 `localhost` 指向的是前端容器自身。

   解决：在 `nginx.conf` 中将 `/api/` 代理到 `http://backend:8080/api/`，使用 Docker Compose 服务名完成容器间通信。

3. 问题：前端镜像如果直接包含 `node_modules`、本地构建产物和环境文件，会导致构建上下文变大，也可能带入本地敏感配置。

   解决：编写 `frontend/.dockerignore` 排除 `node_modules/`、`dist/`、`.env.local` 等文件，只保留构建生产镜像所需内容。

## AI 使用情况

- Prompt：为 Vue + Vite 前端创建 Docker 多阶段构建方案，要求使用 Node Alpine 构建、Nginx Alpine 运行，并支持 Vue Router history 模式。
- AI 帮助解决了前端容器中 Nginx 静态资源部署、`/api/` 反向代理到后端服务名、Docker Compose 前端健康检查和 `.dockerignore` 优化等问题。

## 心得体会

这次前端 Docker 部署让我理解到，前端容器化不只是把页面打包进镜像，还需要处理路由回退、API 代理、静态资源缓存和容器间通信。通过多阶段构建，前端最终运行环境只保留 Nginx 和静态文件，比直接运行 Node 开发服务器更适合生产部署；通过 Compose 中的服务名代理，也让前后端在容器网络中的联调方式更加清晰。
