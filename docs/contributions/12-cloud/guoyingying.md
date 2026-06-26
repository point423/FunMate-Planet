# 云服务部署贡献说明

姓名：郭盈盈
学号：2312190102
日期：2026-06-25

## 我完成的工作

### 1. 平台选择

- 使用平台：Vercel
- 我负责前端云服务部署，主要完成 Vue 3 + Vite 前端在 Vercel 上的构建与发布，不包含 Railway 后端、数据库、Redis 等后端云资源配置。

### 2. 部署配置

- [x] 配置前端 Vercel 部署文件 `vercel.json`，明确仓库根目录安装命令 `cd frontend && npm ci`、构建命令 `cd frontend && npm run build -- --mode production` 和产物目录 `frontend/dist`。
- [x] 配置前端单页应用路由重写规则，将所有访问路径回退到 `/index.html`，保证登录页、首页、活动页等前端路由在云端刷新后仍能正常访问。
- [x] 配置前端环境变量方案，使用 `VITE_API_BASE_URL` 作为线上 API 地址入口，并在 `frontend/.env.example` 中保留示例配置。
- [x] 完成前端请求层适配，在 `frontend/src/api/index.ts` 中通过 `import.meta.env.VITE_API_BASE_URL || '/api'` 区分本地与云端请求地址。
- [x] 配置 GitHub 仓库连接 Vercel，配合 `main` 分支提交触发前端自动构建与自动部署。
- [x] 完成前端线上访问验证，确认 Vercel 部署后可以正常加载静态资源并向后端 API 发起请求。

### 3. 问题解决

- 遇到的问题：Vue 单页应用部署到 Vercel 后，如果直接访问或刷新 `/home`、`/activity` 等前端路由，可能返回 404。
- 解决方案：在 `vercel.json` 中增加 rewrites 规则，把所有路径统一重写到 `/index.html`，交给 Vue Router 处理前端路由。
- 遇到的问题：本地开发阶段前端常通过相对路径 `/api` 或本地代理访问后端，但云端前后端域名分离后，接口地址不能继续写死。
- 解决方案：将前端 API 基础地址统一收敛到 `VITE_API_BASE_URL`，Vercel 通过环境变量注入线上后端地址，代码中保留 `/api` 作为本地默认值。
- 遇到的问题：Vite 前端环境变量会打包到浏览器端，如果把密钥类信息直接写入前端变量，会有泄露风险。
- 解决方案：在 `frontend/.env.example` 中只保留 `VITE_API_BASE_URL` 这类公开配置，并明确说明前端环境变量中不能存放秘密信息。

## PR 链接

- PR #X: https://github.com/xxx/xxx/pull/X

## 在线地址

- 前端：https://fun-mate-planet.vercel.app

## 心得体会

这次前端云服务部署让我更清楚地理解了“本地可运行”和“云端可发布”之间的差异。前端上线不仅是把页面打包出来，还要同时考虑构建入口、路由回退、环境变量注入和自动部署流程。通过这次 Vercel 部署实践，我对前端项目在真实云平台中的发布方式和配置边界有了更具体的认识。
