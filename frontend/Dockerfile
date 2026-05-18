# Build stage
FROM node:20-alpine AS builder

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build --no-types

# Production stage
FROM nginx:1.25-alpine

# 拷贝构建产物到 Nginx 默认目录
COPY --from=builder /app/dist /usr/share/nginx/html

# 拷贝 Nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 暴露标准 HTTP 端口
EXPOSE 80

# 健康检查：指向 80 端口
HEALTHCHECK --interval=10s --timeout=3s --retries=3 \
  CMD wget -qO- http://localhost/ || exit 1

# 以默认 root 用户运行，避免权限烦恼
CMD ["nginx", "-g", "daemon off;"]