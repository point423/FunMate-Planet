# 监控配置贡献说明

姓名：彭静婷
学号：2312190309
日期：2026-05-29
分支：feature-point423-13-monitoring

## 我完成的工作

### 1. 日志配置

- [x] 配置结构化 JSON 日志格式。
- [x] 配置控制台日志和 `logs/app.log` 文件日志。
- [x] 支持通过 `LOG_LEVEL` 环境变量调整日志级别。

### 2. 健康检查

- [x] 增强 `/health` 端点。
- [x] 返回 `status`、`timestamp`、`version`。
- [x] 支持通过 `APP_VERSION` 环境变量覆盖版本号。

### 3. 指标收集

- [x] 收集请求计数。
- [x] 收集响应时间。
- [x] 收集 5xx 错误率。
- [x] 提供 `/metrics` 指标查询端点。

## PR 链接

- PR : https://github.com/point423/FunMate-Planet/pull/28

## 遇到的问题和解决

1. 问题：项目没有引入额外监控组件，直接接入完整 Prometheus/Actuator 会扩大改动范围。
   解决：采用轻量级 `OncePerRequestFilter` 记录请求指标，满足基础监控要求，同时保持改动可控。

2. 问题：结构化日志需要兼容本地 Docker 和云部署环境。
   解决：使用 Logback 原生配置输出 JSON 日志到控制台和文件，容器平台可直接采集标准输出。

3. 问题：CI 测试环境没有真实 Redis，并且 H2 对 `user` 表名存在关键字兼容问题。
   解决：测试环境跳过种子数据初始化，在 H2 测试 URL 中配置 `NON_KEYWORDS=USER`，并补齐控制器接口和单测 mock。

## 验证结果

- [x] Linux Docker Compose 后端启动成功。
- [x] `/health` 返回 `status`、`timestamp`、`version`。
- [x] `/metrics` 返回请求数、响应时间、错误率等指标。
- [x] `docker compose logs backend --tail=30` 可以看到 JSON 结构化请求日志。
- [x] `mvn clean test -DskipTests=false` 在 Linux 环境执行通过。

## 心得体会

这次监控配置让我更清楚地理解了健康检查、日志和指标之间的分工。健康检查用于判断服务是否可用，结构化日志用于定位问题，指标则用于观察服务运行趋势。三者配合后，部署后的排障效率会明显提升。
