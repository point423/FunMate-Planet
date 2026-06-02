# 监控配置贡献说明

姓名：郭盈盈
学号：2312190102
日期：2026-06-02

## 我完成的工作

### 1. 日志配置
- [x] 结构化日志格式（已使用 `logback-spring.xml` 输出 JSON）
- [x] 日志级别配置（可通过环境变量 `LOG_LEVEL` 调整）
- [x] 请求日志记录（`RequestMetricsFilter` 输出结构化 HTTP 请求日志）

### 2. 健康检查
- [x] `/health` 端点实现（见 `HealthController`）
- [x] 健康检查逻辑（返回 `status`、`timestamp`、`version`）
- [x] 健康检查截图已验证（Railway 部署地址 `/health` 返回 JSON）

### 3. 指标收集
- [x] 请求计数
- [x] 响应时间
- [x] 错误率
- [x] `/metrics` 端点可查看基础 HTTP 指标

## PR 链接
- PR #: 待补充

## 遇到的问题和解决
1. 问题：
   解决：在 Windows `cmd` 中使用 `curl -s` 时看不到输出，改为浏览器打开 Railway 的 `/health` 页面进行截图，结果可直接作为作业证明。
2. 问题：
   解决：结构化日志需要确认输出位置，最终从 `backend/logs/app.log` 或部署日志中截取完整 JSON 行作为证明。

## 心得体会
通过这次监控配置，我把后端的可观测性补齐到了基础可交付状态：既能通过 `/health` 快速确认服务是否可用，也能通过 JSON 日志和 `/metrics` 查看请求情况，后续排查问题更直接。

