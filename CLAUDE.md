# 趣搭星球 - 项目规则

## 技术栈
- 前端：Vue 3 + TypeScript + Vite + Element Plus + Pinia + Vue Router
- 后端：Spring Boot 3.5.11 + MySQL 8.0 + Spring Data JPA + Redis 5.0 + JWT
- 部署：docker

## 目录结构
- frontend/src/components/ - 可复用组件
- frontend/src/views/ - 页面组件
- frontend/src/api/ - API 调用封装
- frontend/src/stores/ - Pinia 状态管理
- frontend/src/types/ - TypeScript 类型定义
- backend/src/main/java/ - 后端业务代码
- backend/src/main/resources/ - 配置文件
- docs/ - 项目文档

## 代码规范
- 前端：使用 <script setup> 组合式 API，避免 any 类型
- 后端：遵循 RESTful 接口规范，统一响应格式
- Git 提交：遵循 Conventional Commits 规范（feat/fix/docs/style）

## 禁止事项
- 前端不要直接操作 DOM
- 后端不要在 Controller 写业务逻辑
- 不要修改核心配置文件（除非明确要求）