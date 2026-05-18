#!/bin/bash
set -e

echo "🚀 开始部署 FunMate-Planet..."

# 如果使用了 GHCR，可以先拉取最新镜像
# docker compose -f compose.prod.yaml pull

# 重新构建并启动生产环境容器
docker compose -f compose.prod.yaml up -d --build

# 等待健康检查
echo "⏳ 等待服务就绪 (预计 15 秒)..."
sleep 15

# 显示服务状态
docker compose -f compose.prod.yaml ps

echo "✅ 部署完成！"
echo "前端访问地址: http://localhost"
echo "后端 API 地址: http://localhost:8080"
