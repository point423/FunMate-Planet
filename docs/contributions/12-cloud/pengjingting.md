# 云服务部署贡献说明

姓名：彭静婷
学号：2312190309
日期：2026-05-28

## 我完成的工作

### 1. 平台选择

- 使用平台：Vercel + Railway
- 前端部署到 Vercel，后端通过 Railway Docker 服务部署，数据库和 Redis 使用 Railway 插件。

### 2. 部署配置

- [x] 编写 `vercel.json`，支持前端从仓库根目录自动构建。
- [x] 编写 `railway.toml` 和 `Dockerfile.railway`，支持后端从仓库根目录构建和 `/health` 健康检查。
- [x] 保留原有 `backend/Dockerfile` 本地保底构建方式，云部署单独使用 `Dockerfile.railway`。
- [x] 配置后端环境变量读取，支持 `PORT`、`DATABASE_URL`、`REDIS_URL`、`JWT_SECRET`、`CORS_ALLOWED_ORIGINS`。
- [x] 配置前端 API 地址，支持 Vercel 构建变量 `VITE_API_BASE_URL`。
- [x] 编写 `docs/deployment.md` 部署说明。

### 3. 问题解决

- 遇到的问题：原后端 Dockerfile 依赖本地预先打好的 `target/backend-0.0.1-SNAPSHOT.jar`，云平台从 Git 拉取源码后无法直接构建。
- 解决方案：改为 Maven 多阶段构建镜像，并为 Railway 增加根目录构建专用的 `Dockerfile.railway`，云平台只需要仓库源码即可完成打包和运行。
- 遇到的问题：本地配置固定使用 `localhost`、固定端口和固定跨域策略，上云后前后端域名、端口和数据库地址都会变化。
- 解决方案：统一改为环境变量驱动，并保留本地默认值，兼顾本地开发和云端部署。

## PR 链接

- PR：待提交后填写

## 在线地址

- 前端：待部署后填写
- 后端健康检查：待部署后填写 `/health`

## 心得体会

这次部署改造让我更清楚地理解了本地开发环境和云平台运行环境的差异。后端不能依赖本地构建产物，敏感配置也不能写死在配置文件中；通过 Dockerfile 多阶段构建和环境变量配置，可以让项目更稳定地完成自动部署。
