# 安全审查贡献说明

姓名：郭盈盈
学号：2312190102
日期：2026-05-12

## 我完成的工作

### AI 安全审查

- 审查了哪些文件/模块：
  - `frontend/src/stores/user.ts`
  - `frontend/` 目录下的常见入口（检查 `v-html`/`innerHTML`、硬编码密钥、storage 使用等）
- AI 发现的主要问题：
  - 在 `frontend/src/stores/user.ts` 中将 `token` 持久化到 `localStorage`（存在被 XSS 获取的风险，风险等级：Medium）。
  - 未在前端发现硬编码 API Key，但记录了如何配置环境变量以避免未来误提交（风险等级：Low）。
- 我修复了哪些问题：
  - 从 `frontend/src/stores/user.ts` 中移除了 `localStorage.getItem/setItem/removeItem` 的读写逻辑，改为在内存（Pinia state）保存 `token`；并在 `docs/security-review.md` 中建议后端使用 HttpOnly cookie 存储令牌。
  - 新增 `frontend/.env.example`，并在文档中记录不要将秘密放在前端环境变量中。

### 安全检查清单

- 密码存储：不适用（前端）
- JWT / Session：前端已移除直接持久化 token，建议后端改为 HttpOnly cookie（已在文档提出）
- 接口鉴权：前端使用现有鉴权流程调用 API，未修改后端鉴权逻辑
- 越权访问：未在前端发现绕过机制的实现（需后端校验）
- 注入防护（XSS）：已扫描 `frontend/src/` 中的 `v-html` / `innerHTML`，未发现直接使用（若后续发现，将使用 DOMPurify 进行净化）
- 敏感信息：添加了 `.env.example`，并明确说明不要在前端硬编码密钥
- 依赖安全：在 CI 中添加了 `npm audit` 步骤，且仓库已包含 `gitleaks` 工作流用于密钥泄露扫描

### CI 安全扫描

- 我检查并确认仓库已有文件：`.github/workflows/security.yml`（包含 Gitleaks），并在 `ci.yml` 的 frontend job 中新增 `npm audit` 步骤用于检测高危依赖。CI 文件路径：`.github/workflows/ci.yml`。
- 执行方式（请在本地或通过触发 GitHub Actions 运行）：
  ```bash
  cd frontend
  npm ci
  npm audit --audit-level=high
  ```
  > 注意：如果使用国内 npm 镜像（如 npmmirror），`npm audit` 可能会失败，建议临时切回 `https://registry.npmjs.org/` 或在 GitHub Actions 中查看结果。

### 选做完成情况

- 已完成：移除 `localStorage` token（前端）
- 已完成：新增 `frontend/.env.example`
- 已完成：在 CI 中增加依赖扫描步骤（`npm audit`）并确认存在 Gitleaks 工作流

## PR 链接

- PR：https://github.com/point423/FunMate-Planet/pull/23

## 遇到的问题和解决

1. 问题：在本地运行 `npm audit` 时使用的镜像不支持 audit 接口（返回 NOT_IMPLEMENTED）。
   解决：建议在本地临时将 registry 切换为 `https://registry.npmjs.org/` 后运行，或直接查看 GitHub Actions 的 audit 结果。

## 心得体会

通过本次检查我学到了：前端不要把长寿命敏感令牌放在 `localStorage`，应优先使用后端设置的 HttpOnly cookie；CI 中的自动化安全扫描（密钥 + 依赖）能及时发现并阻止敏感信息泄露和高危依赖提交。
