# 趣搭星球 - 前端模块说明文档

## 一、模块功能

### 核心定位

前端模块是"趣搭星球"响应式Web应用的用户交互层，主要负责用户界面展示和用户体验优化。该模块围绕"找搭子"和"记录生活"两个核心需求，为用户提供轻量级、低社交压力的活动匹配与回忆记录体验，与后端Spring Boot服务形成完整的前后端分离架构。

### 核心子模块功能

| 子模块 | 功能描述 | 主要页面/组件 |
|--------|----------|---------------|
| 用户与认证模块 | 用户注册、登录、个人信息管理、兴趣标签设置、地理位置上报 | LoginView, RegisterView, ProfileView, TagSelector |
| 搭子推荐模块 | 基于地理位置和兴趣标签的智能推荐，展示附近搭子列表 | DiscoverView, RecommendList, UserCard |
| 活动日记模块 | 活动创建、查询、参与确认，共享日记的创建与内容发布 | ActivityView, ActivityDetail, SharedDiary, PhotoUploader |
| 回忆墙模块 | 展示历史搭子关系和活动日记，形成可视化社交回忆 | MemoryWall, FriendList, DiaryTimeline |

### 用户体验目标

- **响应式设计**：适配桌面端、平板端和移动端多种设备尺寸，确保跨平台一致体验
- **轻量级交互**：简化操作流程，降低社交压力，专注于"线下活动"后的共同回忆沉淀
- **视觉吸引力**：年轻化、时尚化的UI设计风格，吸引目标用户群体
- **快速加载**：Vite构建工具提供极速的开发体验和优化的生产构建

## 二、技术选型

### 核心技术栈

| 技术/框架 | 版本 | 选型理由 |
|-----------|------|----------|
| Vue | 3.x | 渐进式JavaScript框架，组合式API提供更好的代码组织，响应式系统高效，学习曲线平缓 |
| Vite | 5.x | 下一代前端构建工具，开发服务器启动快、热更新迅速，生产构建优化出色 |
| TypeScript | 5.x | 类型安全、开发效率高、代码可维护性强，便于与后端API接口对接 |
| Element Plus | 2.x | 基于Vue 3的企业级UI组件库，组件丰富、文档完善、中文支持好 |
| Vue Router | 4.x | Vue官方路由管理器，支持路由懒加载、导航守卫，与Vue 3深度集成 |
| Pinia | 2.x | Vue官方推荐的状态管理库，组合式API风格，TypeScript支持完善 |
| Axios | 1.x | HTTP请求封装、拦截器支持、请求/响应转换，便于统一处理JWT认证 |

### 辅助工具库

| 工具名称 | 用途 |
|----------|------|
| VueUse | Vue组合式API工具集，包含常用工具函数 |
| date-fns | 日期处理和格式化，用于活动时间展示 |
| El-Icons | Element Plus图标库，丰富的图标资源 |
| clsx | 条件类名处理，组件样式管理 |

### 开发工具

| 工具名称 | 用途 |
|----------|------|
| Bun / pnpm | JavaScript运行时和包管理器，比npm/yarn更快 |
| ESLint | 代码质量检查，配合Vue和TypeScript规则 |
| Prettier | 代码格式化，保持团队代码风格一致 |
| Vue DevTools | Vue官方调试工具，方便开发调试 |

## 三、目录结构

```
frontend/
├── public/                    # 静态资源目录
│   └── favicon.ico           # 网站图标
│
├── src/
│   ├── main.ts               # 应用入口文件
│   ├── App.vue               # 根组件
│   │
│   ├── assets/               # 静态资源
│   │   ├── images/           # 图片资源
│   │   │   └── logo.png      # 应用Logo
│   │   └── styles/           # 全局样式
│   │       ├── index.css     # 主样式文件
│   │       └── variables.css # CSS变量定义
│   │
│   ├── components/           # 公共组件目录
│   │   ├── common/           # 通用组件
│   │   │   ├── Header.vue    # 顶部导航
│   │   │   ├── Footer.vue    # 底部信息
│   │   │   └── Sidebar.vue   # 侧边栏
│   │   ├── auth/             # 认证相关组件
│   │   │   ├── LoginForm.vue
│   │   │   └── RegisterForm.vue
│   │   ├── profile/          # 个人主页相关组件
│   │   │   ├── ProfileCard.vue
│   │   │   └── TagSelector.vue
│   │   ├── discover/         # 搭子广场相关组件
│   │   │   ├── UserCard.vue
│   │   │   └── RecommendList.vue
│   │   └── activity/         # 活动日记相关组件
│   │       ├── ActivityCard.vue
│   │       ├── ActivityForm.vue
│   │       └── DiaryEditor.vue
│   │
│   ├── views/                # 页面视图组件
│   │   ├── auth/             # 认证相关页面
│   │   │   ├── LoginView.vue
│   │   │   └── RegisterView.vue
│   │   ├── profile/          # 个人主页
│   │   │   └── ProfileView.vue
│   │   ├── discover/         # 搭子广场
│   │   │   └── DiscoverView.vue
│   │   ├── activity/         # 活动日记
│   │   │   ├── ActivityListView.vue
│   │   │   ├── ActivityCreateView.vue
│   │   │   └── ActivityDetailView.vue
│   │   ├── memory/           # 回忆墙
│   │   │   └── MemoryView.vue
│   │   └── HomeView.vue      # 首页
│   │
│   ├── router/               # 路由配置
│   │   └── index.ts          # Vue Router配置
│   │
│   ├── stores/               # Pinia状态管理
│   │   ├── index.ts          # Store入口
│   │   ├── user.ts           # 用户状态
│   │   └── activity.ts       # 活动状态
│   │
│   ├── api/                  # API接口封装
│   │   ├── index.ts          # Axios实例配置
│   │   ├── auth.ts           # 认证接口
│   │   ├── user.ts           # 用户接口
│   │   ├── dazi.ts           # 搭子推荐接口
│   │   └── activity.ts       # 活动接口
│   │
│   ├── composables/          # 组合式函数（Hooks）
│   │   ├── useAuth.ts        # 认证相关Hook
│   │   ├── useLocation.ts    # 地理位置Hook
│   │   └── useApi.ts         # API请求Hook
│   │
│   ├── types/                # TypeScript类型定义
│   │   ├── user.ts           # 用户类型
│   │   ├── activity.ts       # 活动类型
│   │   ├── diary.ts          # 日记类型
│   │   └── api.ts            # API响应类型
│   │
│   └── utils/                # 工具函数
│       ├── validators.ts     # 表单验证函数
│       └── format.ts         # 格式化函数
│
├── .env                      # 环境变量
├── .env.development          # 开发环境变量
├── .env.production           # 生产环境变量
├── .gitignore                # Git忽略文件
├── index.html                # HTML入口文件
├── package.json              # 项目依赖配置
├── tsconfig.json             # TypeScript配置
├── vite.config.ts            # Vite配置文件
└── README.md                 # 项目说明文档
```

## 四、运行方式

### 前置条件

1. 安装Node.js 18+ 或 Bun 1.0+
2. 确保后端服务已启动（Spring Boot运行在 http://localhost:8080）
3. 确保MySQL和Redis服务正常运行

### 安装依赖

```bash
# 使用 bun（推荐）
bun install

# 或使用 pnpm
pnpm install

# 或使用 npm
npm install
```

### 环境变量配置

创建 `.env.development` 文件配置开发环境变量：

```env
# 后端API基础地址
VITE_API_BASE_URL=http://localhost:8080/api

# 应用标题
VITE_APP_TITLE=趣搭星球
```

创建 `.env.production` 文件配置生产环境变量：

```env
# 生产环境API地址
VITE_API_BASE_URL=https://api.funmate.com/api

VITE_APP_TITLE=趣搭星球
```

### 开发模式运行

```bash
# 使用 bun
bun run dev

# 或使用 npm
npm run dev
```

开发服务器将在 `http://localhost:5173` 启动。

### 生产构建

```bash
# 构建生产版本
bun run build

# 预览生产构建
bun run preview
```

构建产物将输出到 `dist/` 目录。

### 代码检查

```bash
# 运行ESLint检查
bun run lint

# 自动修复可修复的问题
bun run lint --fix

# 类型检查
bun run type-check
```

## 五、接口对接说明

### 接口通用规范

前端通过RESTful API与后端Spring Boot服务通信，遵循以下规范：

- **接口前缀**：`/api`
- **请求方式**：RESTful风格（GET/POST/PUT/DELETE）
- **数据格式**：请求/响应均为JSON
- **认证方式**：JWT Token（放在请求头 `Authorization: Bearer {token}`）

### 响应通用格式

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### Axios实例配置

```typescript
// src/api/index.ts
import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

// 创建Axios实例
const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器：自动添加JWT Token
service.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器：统一处理错误
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const { code, message, data } = response.data
    if (code === 200) {
      return data
    } else {
      ElMessage.error(message || '请求失败')
      return Promise.reject(new Error(message || '请求失败'))
    }
  },
  (error) => {
    if (error.response?.status === 401) {
      // Token过期，清除用户信息并跳转登录页
      const userStore = useUserStore()
      userStore.logout()
      window.location.href = '/login'
    }
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default service
```

### Pinia状态管理示例

```typescript
// src/stores/user.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, register, getUserInfo } from '@/api/auth'
import type { UserInfo, LoginForm } from '@/types/user'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)

  // 登录
  const loginAction = async (form: LoginForm) => {
    const data = await login(form)
    token.value = data.token
    localStorage.setItem('token', data.token)
    return data
  }

  // 获取用户信息
  const fetchUserInfo = async () => {
    const data = await getUserInfo()
    userInfo.value = data
    return data
  }

  // 登出
  const logout = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    loginAction,
    fetchUserInfo,
    logout,
  }
})
```

### Vue Router路由守卫

```typescript
// src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/auth/LoginView.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/discover',
      name: 'Discover',
      component: () => import('@/views/discover/DiscoverView.vue'),
      meta: { requiresAuth: true }
    },
    // ... 其他路由
  ],
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else {
    next()
  }
})

export default router
```

### 核心接口列表

| 模块 | 接口路径 | 方法 | 功能描述 |
|------|----------|------|----------|
| 认证 | `/auth/register` | POST | 用户注册 |
| 认证 | `/auth/login` | POST | 用户登录，返回JWT Token |
| 用户 | `/users/me` | GET | 获取当前用户信息 |
| 用户 | `/users/location` | POST | 上报用户地理位置 |
| 搭子推荐 | `/dazi/recommend` | GET | 获取搭子推荐列表 |
| 活动 | `/activities` | POST | 创建活动 |
| 活动 | `/activities/{id}` | GET | 获取活动详情 |
| 文件上传 | `/upload/image` | POST | 上传图片 |

### API接口封装示例

```typescript
// src/api/auth.ts
import request from './index'
import type { LoginForm, RegisterForm } from '@/types/user'

// 用户登录
export const login = (data: LoginForm) => {
  return request.post('/auth/login', data)
}

// 用户注册
export const register = (data: RegisterForm) => {
  return request.post('/auth/register', data)
}

// 获取当前用户信息
export const getUserInfo = () => {
  return request.get('/users/me')
}

// 上报地理位置
export const reportLocation = (data: { longitude: number; latitude: number }) => {
  return request.post('/users/location', data)
}
```

```typescript
// src/api/dazi.ts
import request from './index'

// 获取搭子推荐列表
export const getRecommendations = (params: {
  radius?: number
  pageNum?: number
  pageSize?: number
}) => {
  return request.get('/dazi/recommend', { params })
}
```

```typescript
// src/api/activity.ts
import request from './index'
import type { ActivityForm } from '@/types/activity'

// 创建活动
export const createActivity = (data: ActivityForm) => {
  return request.post('/activities', data)
}

// 获取活动详情
export const getActivityDetail = (id: number) => {
  return request.get(`/activities/${id}`)
}
```

## 六、开发规范

### 代码规范

- **命名规范**：组件使用PascalCase，变量和函数使用camelCase，常量使用UPPER_SNAKE_CASE
- **文件命名**：Vue组件使用PascalCase（如`UserCard.vue`），工具函数使用camelCase
- **注释规范**：所有公共函数必须添加JSDoc注释
- **类型规范**：避免使用any，尽量使用明确的类型定义，与后端API响应保持一致

### Vue组件规范

- 使用 `<script setup>` 语法糖编写组合式API
- 组件Props定义使用 `defineProps` with TypeScript
- 组件Events定义使用 `defineEmits`
- 组件名称使用多词命名，避免与HTML元素冲突

### Git提交规范

遵循 Conventional Commits 规范：

| 类型 | 描述 | 示例 |
|------|------|------|
| feat | 新功能 | feat: 添加用户登录功能 |
| fix | Bug修复 | fix: 修复首页加载问题 |
| docs | 文档更新 | docs: 更新前端模块文档 |
| style | 代码格式调整 | style: 格式化代码 |
| refactor | 代码重构 | refactor: 重构用户模块 |
| test | 测试相关 | test: 添加用户模块测试 |
| chore | 构建/工具相关 | chore: 更新依赖版本 |

### 分支管理

- `main`: 主分支，稳定版本
- `develop`: 开发分支
- `feature/*`: 功能分支
- `bugfix/*`: Bug修复分支
