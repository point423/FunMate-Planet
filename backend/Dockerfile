# 使用极简 Alpine 环境，体积仅 30MB+
FROM liberica-21

# 设置工作目录
WORKDIR /app

# 将打包好的 jar 包拷贝到镜像中
# 注意：文件名需与 pom.xml 中的 artifactId 和 version 对应
COPY target/backend-0.0.1-SNAPSHOT.jar app.jar

# 暴露后端端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
