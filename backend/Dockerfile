# 第一阶段：构建阶段 (在容器内编译)
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# 利用缓存：先拷贝 pom.xml 并下载依赖
COPY pom.xml .
RUN mvn dependency:go-offline

# 拷贝源码并打包
COPY src ./src
RUN mvn clean package -DskipTests

# 第二阶段：运行阶段
FROM liberica-21

# 设置工作目录
WORKDIR /app

# 安全性：创建并切换到非 root 用户 (appuser)
# 同时确保 uploads 目录权限正确
RUN addgroup -S appgroup && adduser -S appuser -G appgroup && \
    mkdir -p /app/uploads && \
    chown -R appuser:appgroup /app

# 从构建阶段拷贝 jar 包
COPY --from=builder /app/target/backend-0.0.1-SNAPSHOT.jar app.jar
RUN chown appuser:appgroup app.jar

USER appuser

# 暴露端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
