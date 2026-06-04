# FunMate Planet 监控配置说明

## 目标

本次监控配置为后端服务补充基础可观测性，覆盖结构化日志、健康检查和 HTTP 请求指标，便于本地 Docker、Railway 等部署环境快速判断服务状态。

## 结构化日志

后端新增 `backend/src/main/resources/logback-spring.xml`，统一输出 JSON 日志到控制台和 `logs/app.log`。

单条日志字段包括：

| 字段 | 说明 |
| --- | --- |
| `time` | 日志时间 |
| `level` | 日志级别 |
| `service` | 服务名，默认 `funmate-planet-backend` |
| `thread` | 线程名 |
| `logger` | 日志来源 |
| `message` | 日志消息 |
| `exception` | 异常摘要 |

请求日志由 `RequestMetricsFilter` 记录，示例：

```json
{"time":"2026-05-29T16:00:00.000+08:00","level":"INFO","service":"funmate-planet-backend","thread":"http-nio-8080-exec-1","logger":"c.z.p.b.monitoring.RequestMetricsFilter","message":"event=http_request method=GET path=/health status=200 duration_ms=12 error=","exception":""}
```

日志级别可通过环境变量调整：

```env
LOG_LEVEL=INFO
```

## 健康检查

后端 `/health` 端点返回服务状态、时间戳和版本号。

测试命令：

```bash
curl http://localhost:8080/health
```

返回示例：

```json
{
  "status": "healthy",
  "timestamp": "2026-05-29T08:00:00Z",
  "version": "0.0.1-SNAPSHOT"
}
```

版本号可通过环境变量覆盖：

```env
APP_VERSION=1.0.0
```

## 指标收集

新增 `/metrics` 端点查看基础 HTTP 指标。

测试命令：

```bash
curl http://localhost:8080/metrics
```

返回示例：

```json
{
  "timestamp": "2026-05-29T08:00:00Z",
  "http": {
    "total_requests": 10,
    "error_requests": 1,
    "error_rate": 0.1,
    "active_requests": 0,
    "average_response_time_ms": 35.4,
    "max_response_time_ms": 120
  }
}
```

指标说明：

| 指标 | 说明 |
| --- | --- |
| `total_requests` | 服务启动后收到的请求总数 |
| `error_requests` | HTTP 5xx 请求数 |
| `error_rate` | 5xx 错误率 |
| `active_requests` | 当前正在处理的请求数 |
| `average_response_time_ms` | 平均响应时间 |
| `max_response_time_ms` | 最大响应时间 |

## 本地验证

后端本地启动后执行：

```bash
curl http://localhost:8080/health
curl http://localhost:8080/metrics
```

Docker Compose 启动后执行：

```bash
docker compose logs backend | head
curl http://localhost:8080/health
curl http://localhost:8080/metrics
```

## Linux Docker 验证记录

在 Linux Docker Compose 环境中完成验证，后端容器、MySQL、Redis 均正常启动。

验证命令：

```bash
curl http://localhost:8080/health
curl http://localhost:8080/metrics
docker compose logs backend --tail=30
```

实际验证结果包括：

- `/health` 正常返回 `status=healthy`、`timestamp`、`version=0.0.1-SNAPSHOT`。
- `/metrics` 正常返回 `total_requests`、`error_requests`、`error_rate`、`active_requests`、`average_response_time_ms`、`max_response_time_ms`。
- 后端日志正常输出 JSON 结构化日志，包含 `time`、`level`、`service`、`thread`、`logger`、`message`、`exception` 字段。
- `mvn clean test -DskipTests=false` 已在 Linux 环境验证通过。
