# 软件测试贡献说明 - 前端测试

**姓名：** 郭盈盈
**学号：** 2312190102
**角色：** 前端
**日期：** 2026-04-28  

---

## 一、完成的测试工作概览

### 测试成果

| 指标 | 数值 |
|------|------|
| 测试文件数 | 12 个 |
| 测试用例总数 | 75 个 |
| 组件测试 | 8 个组件，47 个测试用例 |
| Mock API 测试 | 4 个模块，28 个测试用例 |
| **测试通过率** | **100%** |

---

## 二、测试文件详单

### 📱 组件测试（8 个）

| # | 测试文件 | 组件 | 测试数 | 路径 |
|---|---------|------|--------|------|
| 1 | LoginView.test.ts | 登入界面 | 5 | `src/__tests__/views/LoginView.test.ts` |
| 2 | RegisterView.test.ts | 注册界面 | 5 | `src/__tests__/views/RegisterView.test.ts` |
| 3 | TagSetupView.test.ts | 标签设置 | 7 | `src/__tests__/views/TagSetupView.test.ts` |
| 4 | ActivityView.test.ts | 活动页面 | 7 | `src/__tests__/views/ActivityView.test.ts` |
| 5 | AppNavbar.test.ts | 导航栏 | 3 | `src/__tests__/components/AppNavbar.test.ts` |
| 6 | TagSelector.test.ts | 标签选择器 | 7 | `src/__tests__/components/TagSelector.test.ts` |
| 7 | DiaryEditor.test.ts | 日记编辑器 | 5 | `src/__tests__/components/DiaryEditor.test.ts` |
| 8 | AiAssistant.test.ts | AI 助手 | 8 | `src/__tests__/components/AiAssistant.test.ts` |
| **合计** | | | **47** | |

**覆盖情况：**
- TaskSelector.vue: **100%** 
- RegisterView.vue: **100%** 
- ActivityView.vue: **100%** 
- LoginView.vue: **89.74%** 
- TagSetupView.vue: **93.15%** 
- AppNavbar.vue: **99.22%** 
- DiaryEditor.vue: **86.91%** 
- AiAssistant.vue: **86.11%** 

### 🔌 Mock API 测试（4 个）

| # | 测试文件 | API 模块 | 测试数 | 覆盖的测试点 |
|---|---------|---------|--------|-------------|
| 1 | auth.test.ts | 认证 | 7 | 登入、注册、用户信息获取 |
| 2 | user.test.ts | 用户 | 8 | 用户信息、标签、好友申请 |
| 3 | activity.test.ts | 活动 | 9 | 创建活动、获取日记、上传日记 |
| 4 | ai.test.ts | AI 助手 | 4 | AI 建议、网络错误、多查询 |
| **合计** | | | **28** | |

**API 覆盖率：**
- ai.ts: **100%** 
- auth.ts: **95%** 
- activity.ts: **80.48%** 
- user.ts: **85.71%** 

---

## 三、测试清单 & 覆盖范围

### 组件测试涵盖

- [x] 组件正常渲染
- [x] 用户交互（点击、输入、表单提交）
- [x] 表单验证（必填字段、格式验证）
- [x] 条件渲染和动态数据
- [x] 错误提示展示
- [x] 加载状态管理
- [x] 路由导航
- [x] 组件事件发出（emit）

### API 测试涵盖

- [x] 成功请求场景（数据正确返回）
- [x] 失败场景（网络错误、服务器错误、验证失败）
- [x] 参数验证
- [x] 异常处理
- [x] Mock 数据使用
- [x] Axios 拦截器 Mock
- [x] 异步操作处理

### Mock 使用

- [x] Element Plus 组件库 Mock
- [x] Vue Router Mock（useRouter、useRoute）
- [x] Pinia Store Mock（useUserStore）
- [x] Axios 请求 Mock（request.post、request.get、request.put）
- [x] Browser API Mock（localStorage、URL.createObjectURL）

---

## 四、覆盖率报告

### 总体覆盖率

```
整体语句覆盖率：24.01%
整体分支覆盖率：56.33%
整体函数覆盖率：25.35%
```

### 🎯 核心功能覆盖率（>80%）

| 模块 | 覆盖率 |
|------|--------|
| api/ai.ts | **100%** |
| components/profile/TagSelector.vue | **100%** |
| views/auth/RegisterView.vue | **100%** |
| views/activity/ActivityView.vue | **100%** |
| api/auth.ts | **95%** |
| components/common/AppNavbar.vue | **99.22%** |
| views/auth/TagSetupView.vue | **93.15%** |
| components/activity/DiaryEditor.vue | **86.91%** |
| api/user.ts | **85.71%** |
| api/activity.ts | **80.48%** |
| components/common/AiAssistant.vue | **86.11%** |
| views/auth/LoginView.vue | **89.74%** |

---

## 五、AI 辅助过程

### 使用工具
- **工具：** GitHub Copilot
- **IDE：** Visual Studio Code
- **框架：** Vitest + Vue Test Utils

### 主要 Prompt 与应用

#### 1. 测试框架搭建
```
Prompt: "为 Vue 3 + TypeScript 项目配置 Vitest 测试框架，包括以下要求：
- 支持 Vue 单文件组件测试
- Mock Element Plus 组件库
- Mock vue-router 和 pinia
- 支持覆盖率报告生成"
```
**应用：** 生成了完整的 vitest.config.ts 和 setup.ts 配置文件

#### 2. 组件测试编写
```
Prompt: "为 Vue 3 组件编写单元测试，测试以下场景：
- 组件挂载和渲染
- 用户交互（点击事件、表单输入）
- 条件渲染
- Props 传递和 Emit 事件"
```
**应用：** 生成了 8 个组件测试文件，覆盖所有关键交互

#### 3. API Mock 设置
```
Prompt: "创建 API 单元测试，Mock axios 请求，测试以下场景：
- 成功请求
- 网络错误
- 响应数据验证
- 参数传递验证"
```
**应用：** 生成了 4 个 API 测试文件，28 个 Mock 测试用例

#### 4. 错误修复
```
Prompt: "修复以下 Vitest 错误：
- TypeError: vi.mocked(...).mockReturnValue is not a function
- URL.createObjectURL is not a function
详细说明根本原因和解决方案"
```
**应用：** 快速定位并修复了所有 23 个失败的测试

---

## 六、遇到的问题与解决

### 问题 1：vue-router Mock 失败

**现象：** `vi.mocked(VueRouter.useRouter).mockReturnValue is not a function`

**根本原因：** vue-router 没有被正确 mock 为可被 vi.mocked() 操作的函数

**解决方案：**
```typescript
// 在 setup.ts 中创建可 mock 的函数对象
const mockUseRouter = vi.fn()
const mockUseRoute = vi.fn()

vi.mock('vue-router', () => ({
  useRouter: mockUseRouter,
  useRoute: mockUseRoute,
  RouterLink: { name: 'RouterLink' },
}))

// 在测试中正确使用
vi.mocked(VueRouter.useRouter).mockReturnValue(mockRouter)
```

**学到的知识：** Vitest 的 mock 函数需要在模块级别正确定义才能被 vi.mocked() 正确访问

### 问题 2：URL API 不可用

**现象：** `TypeError: URL.createObjectURL is not a function`

**根本原因：** jsdom 环境中 URL 对象默认未完全实现

**解决方案：**
```typescript
// 在 setup.ts 中 Mock URL API
if (!global.URL) {
  global.URL = {} as any
}
global.URL.createObjectURL = vi.fn(() => 'blob:mock-url')
global.URL.revokeObjectURL = vi.fn()
```

**学到的知识：** jsdom 环境需要手动 Mock 某些浏览器 API

### 问题 3：路由 Mock 在不同测试间状态污染

**现象：** ActivityView 的"伙伴路由测试"失败，因为前一个测试的 mock 状态未清除

**根本原因：** Mock 需要在每个测试前重新设置正确的返回值

**解决方案：**
```typescript
it('当前路由为伙伴时伙伴导航项应为活跃', () => {
  // 重新 mock 路由为伙伴路由
  const mockPartnerRoute = { path: '/activity/partner' }
  vi.mocked(VueRouter.useRoute).mockReturnValue(mockPartnerRoute)
  
  // ... 测试代码
})
```

**学到的知识：** Vitest 测试隔离需要在每个 describe 或 it 块中明确设定 mock 状态

### 问题 4：编译器宏警告

**现象：** `[@vue/compiler-sfc] defineProps is a compiler macro and no longer needs to be imported`

**原因：** Vue 3.3+ 中 defineProps/defineEmits 无需导入

**解决方案：** 删除测试文件中的 import 声明（这是警告，不影响测试通过）

**学到的知识：** 需要关注 Vue 版本更新带来的 API 变化

---

## 七、PR 链接

[https://github.com/point423/FunMate-Planet.git](https://github.com/point423/FunMate-Planet/pull/16)

---

## 八、心得体会

​		通过这次软件测试实践，我获得了深刻的认识和宝贵的经验。在能力提升方面，这个过程极大地增强了我的代码质量意识，让我理解了单元测试对项目的重要性；提高了我的错误排查能力，通过 Mock 错误能够快速定位问题根源；培养了我的测试设计能力，让我能够识别关键的测试场景；也加深了我对 Vue 3 响应式系统和组件机制的理解。我认为可以进一步添加 Playwright E2E 测试以提升测试完整性，建立更多的集成测试连接前端和后端，以及配置 GitHub Actions 实现持续集成。对于团队协作，我建议统一 Mock 策略和测试文件位置，定期审查测试覆盖率和失败测试，编写测试编写指南供团队参考，并关注测试执行时间来优化 Mock 配置。这次实践让我意识到测试不仅是质量保障的工具，更是深入理解框架和提升编程能力的重要途径。

---

## 十、附录：文件清单

### 测试配置文件
- `vitest.config.ts` - Vitest 配置
- `src/__tests__/setup.ts` - 全局 Mock 设置
- `package.json` - 依赖和脚本更新

### 测试文件
```
src/__tests__/
├── api/
│   ├── auth.test.ts (7)
│   ├── user.test.ts (8)
│   ├── activity.test.ts (9)
│   └── ai.test.ts (4)
├── components/
│   ├── AppNavbar.test.ts (3)
│   ├── TagSelector.test.ts (7)
│   ├── DiaryEditor.test.ts (5)
│   └── AiAssistant.test.ts (8)
├── views/
│   ├── LoginView.test.ts (5)
│   ├── RegisterView.test.ts (5)
│   ├── TagSetupView.test.ts (7)
│   └── ActivityView.test.ts (7)
└── setup.ts
```

### 总计
- **文件数：** 16
- **测试用例：** 75
- **通过率：** 100%
