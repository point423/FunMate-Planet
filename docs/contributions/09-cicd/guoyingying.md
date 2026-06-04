# CI/CD 配置贡献说明

姓名：郭盈盈  学号：2312190102  角色：前端  日期：2026-05-05

## 完成的工作

### 工作流相关
- [x] 参与编写 / 审查 `.github/workflows/ci.yml`
  - 前端 Job 配置：Node.js 20、ESLint、Vitest 覆盖率测试
  - 后端 Job 配置：JDK 21、Maven 构建和测试（由后端成员完成）
  
- [x] 配置 Codecov 覆盖率上传
  - 前端：上传 `frontend/coverage/coverage-final.json`，flag: `frontend`
  - 后端：上传 `backend/target/site/jacoco/jacoco.xml`，flag: `backend`（由后端成员配置）
  
- [x] 添加 README 状态徽章
  - CI 状态徽章（GitHub Actions）
  - 后端覆盖率徽章（Codecov）
  - 前端覆盖率徽章（Codecov）

### 代码适配
- [x] 本地测试命令与 CI 一致
  - 修改 `npm test` → `vitest run --coverage`（非交互模式）
  - ESLint 配置：`.eslintrc.cjs` + `.eslintignore`
  - TypeScript 版本号锁定：5.5.4（@typescript-eslint 兼容性）

- [x] 代码通过 Lint 检查
  - 修复所有 ESLint 错误：0 errors
  - 修复未使用变量（如：`ElMessage`, `ref`, `nextTick` 等）
  - 修复 Vue 3 + TypeScript 解析问题（使用 `vue-eslint-parser`）

- [x] 核心覆盖率达标
  - 当前覆盖率：23.86% (Statements)
  - 测试用例数：75 passed
  - 测试文件数：12 passed
  - 需要后续增加测试以提高覆盖率

### 可选项
- [ ] 配置 Dependabot 自动更新依赖
- [ ] 集成 CodeRabbit AI 代码审查
- [ ] 使用 act 本地验证工作流

---

## PR 链接

https://github.com/point423/FunMate-Planet/pull/18

---

## CI 运行链接

https://github.com/point423/FunMate-Planet/actions

---

## 问题与解决

### 问题 1: TypeScript 版本不兼容
**问题**: `@typescript-eslint/typescript-estree` 不支持 TypeScript 5.9.3
```
SUPPORTED TYPESCRIPT VERSIONS: >=4.7.4 <5.6.0
YOUR TYPESCRIPT VERSION: 5.9.3
```

**解决**:
- 将 TypeScript 版本降至 5.5.4（在 `<5.6.0` 范围内且稳定）
- 重新运行 `npm install` 更新依赖

### 问题 2: Vue 3 `<script setup>` 语法解析失败
**问题**: ESLint 报错 `Parsing error: '>' expected` 在所有 `.vue` 文件中

**解决**:
- `.eslintrc.cjs` 中添加 `parser: 'vue-eslint-parser'` 
- 安装 `vue-eslint-parser` 依赖
- 配置 `extraFileExtensions: ['.vue']`

### 问题 3: 测试文件中缺失导入
**问题**: `npm test` 时报 `ElMessage is not defined` 和 `nextTick is not defined`

**解决**:
- 恢复 `RegisterView.test.ts` 中的 `import { ElMessage } from 'element-plus'`
- 恢复 `TagSetupView.test.ts` 中的 `import { nextTick } from 'vue'`
- 原因：之前修复 lint 时不小心删除了这些导入

### 问题 4: 前端覆盖率报告路径错误
**问题**: CI 工作流寻找 `frontend/coverage/lcov.info` 但 Vitest 生成的是 `coverage-final.json`

**解决**:
- 更新 `.github/workflows/ci.yml` 中前端的覆盖率文件路径
- 从 `files: frontend/coverage/lcov.info` 改为 `files: ./frontend/coverage/coverage-final.json`

---

## 心得体会

通过这次 CI/CD 配置工作，我深刻认识到：

1. **工具链的正确配置至关重要**
   - TypeScript 版本兼容性问题需要提前关注
   - ESLint + Prettier 的配置不当会影响整个团队的开发效率

2. **自动化测试的价值**
   - 通过 GitHub Actions，每次 push 都能自动运行测试
   - 早期发现问题，防止缺陷合并到主分支

3. **团队协作的规范性**
   - 统一的 ESLint 规则和测试标准很重要
   - 覆盖率目标（>60%）可以驱动更高质量的代码

4. **可复用的工作流模板**
   - 这套 CI/CD 配置可以作为后续项目的模板
   - 前后端并行测试大幅提高了反馈效率

**建议**: 后续可以考虑在 CI 中添加性能测试、安全扫描等更高级的检查。
