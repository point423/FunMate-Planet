# Docker 部署贡献说明
姓名：彭静婷
学号：2312190309
日期：2026-05-15

## 我完成的工作
### 1. Dockerfile 编写
- [x] 前端 Dockerfile（多阶段构建，基于 Nginx Alpine）
- [x] 后端 Dockerfile（多阶段构建，保留 liberica-21 运行环境）
- [x] .dockerignore 文件（排除冗余文件，优化镜像大小）

### 2. Compose 配置
- [x] 开发环境 compose.yaml（支持前端热重载，集成 MySQL/Redis）
- [x] 生产环境 compose.prod.yaml（资源限制、Secrets 密钥管理）
- [x] 健康检查配置（通过 wget 探测服务可用性）

### 3. 自动化部署
- 选择了选项 A：GitHub Actions 自动构建并推送到 GHCR
- 具体内容：集成了 Trivy 漏洞扫描，确保镜像安全性。

## PR 链接
- PR :  https://github.com/point423/FunMate-Planet/pull/25

## 遇到的问题和解决
1. 问题：后端极简镜像 liberica-21 缺少用户管理命令。
   解决：通过查阅得知该镜像基于 Alpine，使用 addgroup/adduser 成功创建非 root 用户。
2. 问题：GitHub Actions 构建时间较长。
   解决：引入了 type=gha 缓存机制，显著提升了二次构建速度。

## AI 使用情况
- Prompt：为 Spring Boot 后端创建生产级 Dockerfile，要求多阶段构建、liberica-21 基础镜像、非 root 用户运行。
- AI 帮助解决了 Docker 多阶段构建中 Maven 依赖缓存的优化，以及 Compose 生产环境配置的安全性增强。

## 心得体会
通过本次 Docker 容器化部署，我深刻理解了“一次构建，到处运行”的优势。多阶段构建极大减小了最终镜像体积（后端减至约 150MB），而 GitHub Actions 的集成则让 CI/CD 流程变得丝滑。