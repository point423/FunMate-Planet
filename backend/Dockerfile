# 使用轻量级的运行时镜像，跳过容器内编译以解决网络问题
FROM bellsoft/liberica-openjdk-alpine:21

WORKDIR /app

# 安全性：创建并切换到非 root 用户 (appuser)
# 同时确保相关目录权限正确
RUN addgroup -S appgroup && adduser -S appuser -G appgroup && \
    mkdir -p /app/uploads && \
    chown -R appuser:appgroup /app

# 核心步骤：直接从宿主机的 target 目录拷贝编译好的 jar 包
# 运行构建前，请务必先在宿主机 backend 目录执行：
# ./mvnw clean package -DskipTests
COPY target/backend-0.0.1-SNAPSHOT.jar app.jar
RUN chown appuser:appgroup app.jar

USER appuser

# 暴露端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
