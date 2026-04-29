# 趣搭星球（FunMate Planet）

[![CI](https://github.com/point423/FunMate-Planet/actions/workflows/ci.yml/badge.svg)](https://github.com/point423/FunMate-Planet/actions)
[![Backend Service Coverage](https://img.shields.io/badge/Service%20Coverage-62%25-green.svg)](https://github.com/point423/FunMate-Planet)
[![Frontend Coverage](https://img.shields.io/badge/Frontend%20Coverage-Pending-lightgrey.svg)](https://github.com/point423/FunMate-Planet)

## 团队成员
| 姓名 | 学号 | 分工 |
|------|------|------|
| 郭盈盈 | 2312190102 | 前端开发、UI 交互设计 |
| 彭静婷 | 2312190309 | **后端架构负责人**：负责系统整体架构设计、数据库建模、API 全量开发、Docker 容器化部署及 CI/CD 自动化流水线搭建。 |

## 项目简介
趣搭星球是一款专注于线下兴趣社交的响应式 Web 应用。通过“兴趣匹配 + 地理发现”双驱动模式，帮助年轻人快速找到同频搭子。

### 核心后端功能 (已 100% 完成 API 覆盖)
- **认证系统**：支持 JWT 登录/注册/登出全闭环。
- **地理发现**：基于 **Redis Geo** 实现毫秒级的“附近的人”搜索。
- **社交网络**：支持单向关注、双向好友申请握手逻辑。
- **活动闭环**：发起活动、加入活动、活动状态管理。
- **评价体系**：活动后用户互评，系统自动计算并更新**好评排行榜**。
- **活动日记**：支持多图展示的线下活动回忆墙。

## 质量保障（CI/CD & 自动化测试）
本项目建立了完善的持续集成与测试体系：
- **CI 流水线**：基于 GitHub Actions 实现，代码提交后自动触发编译、单元测试及覆盖率统计。
- **后端测试**：基于 JUnit 5 + Mockito 实现，涵盖 55 个核心测试用例。
- **覆盖率**：核心 Service 模块指令覆盖率已达 62%。

## 运行与测试指南

### 1. 快速启动 (Docker 环境)
```sh
# 编译打包并启动
mvn clean package -DskipTests
docker compose up -d --build
```

### 2. 执行自动化测试
```sh
# 运行单元测试与接口测试并生成报告
mvn clean test
```
报告路径：`backend/target/site/jacoco/index.html`
