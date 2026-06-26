# 监控配置贡献说明

姓名：郭盈盈
学号：2312190102
日期：2026-06-03

## 我完成的工作

### 1. 日志配置

- [x] 统一前端请求入口，所有 API 请求都经过 `frontend/src/api/index.ts` 的 axios 实例，便于把请求异常集中暴露出来。
- [x] 在响应拦截器中统一处理业务错误和网络错误，`401` 时自动清理 token 并跳转登录页，避免前端静默失败。
- [x] 在 `frontend/src/composables/useApi.ts` 中保留 `error` 状态，方便页面直接展示加载失败原因。

### 2. 健康检查

- [x] 在 `frontend/Dockerfile` 中增加 `HEALTHCHECK`，通过访问 `http://localhost/` 判断前端静态站点是否可用。
- [x] 在 `docker-compose.yml` 中为前端服务配置健康检查，并让前端依赖后端健康状态后再启动。
- [x] 在 `frontend/nginx.conf` 中保留 `try_files $uri $uri/ /index.html;`，保证前端路由刷新后仍能正常访问。

### 3. 指标收集

- [x] 前端统一通过 `frontend/src/api/index.ts` 维护 API 基地址，本地 Docker 场景下默认走 `/api` 代理，便于后端稳定统计请求计数、响应时间和错误率。
- [x] 前端请求层保留超时、鉴权和错误提示逻辑，便于在页面侧快速定位异常请求。
- [x] 配合后端监控接口完成可观测性闭环，前端负责暴露错误和保持请求入口一致，后端负责指标采集。

## PR 链接

- PR #X: https://github.com/xxx/xxx/pull/X

## 遇到的问题和解决

1. 问题：前端静态页和后端 API 分离后，健康检查容易只覆盖页面本身。
   解决：在 `frontend/Dockerfile` 增加容器级 `HEALTHCHECK`，并通过 `docker-compose.yml` 保证前后端启动顺序。

2. 问题：接口报错如果只依赖控制台输出，排查效率不高。
   解决：在 `frontend/src/api/index.ts` 中统一处理 401、网络错误和业务错误，并用 `ElMessage` 直接提示。

3. 问题：Vue Router 的 history 模式在刷新子路由时容易 404。
   解决：在 `frontend/nginx.conf` 中使用 `try_files` 回退到 `index.html`，保持路由可用。

## 心得体会

前端在监控配置中的重点不是重复实现后端指标，而是把请求入口、错误提示和健康检查做统一，方便整体系统排查问题。
