# 安全审查贡献说明

姓名：彭静婷  
学号：2312190309  
日期：2026-05-06

## 我完成的工作

### AI 安全审查
- **审查了哪些文件/模块**：`AuthController.java`, `UserController.java`, `JwtUtil.java`, `AuthService.java`
- **AI 发现的主要问题**：
    1. **硬编码密钥**：`JwtUtil` 中存在默认密钥字符串。
    2. **水平越权 (IDOR)**：`UserController` 的修改和删除接口仅通过路径 ID 识别资源，未校验操作者身份。
    3. **错误信息暴露**：部分异常直接抛出到前端，包含内部类路径。
- **我修复了哪些问题**：
    1. **修复硬编码密钥**：将 `jwt.secret` 改为强制从环境变量 `${JWT_SECRET}` 读取，并提供了 `.env.example` 模板。
    2. **修复 IDOR 漏洞**：在 `updateUser` 和 `deleteUser` 接口中增加了 `if (!id.equals(currentUserId))` 校验，确保用户只能操作自己的账户。

### 安全检查清单
- [x] **密码存储**：使用 BCrypt 哈希存储，已在 `AuthService` 实现。
- [x] **JWT / Session**：Token 设置了 24 小时过期时间。
- [x] **接口鉴权**：除登录注册外，所有 `/api/**` 接口均受 `AuthInterceptor` 保护。
- [x] **越权访问**：已通过本次安全加固修复。
- [x] **SQL 注入防护**：全面使用 Spring Data JPA (Hibernate)，采用参数化查询，无拼接 SQL。
- [x] **API Key / 密码**：不硬编码，通过 `${...}` 语法引用环境变量。
- [x] **.env 文件**：已加入 `.gitignore`，提供了模板。

### CI 安全扫描
- **配置了哪个选项**：选项 A（密钥泄露扫描 - Gitleaks）
- **扫描结果**：CI 运行通过，未在提交历史中发现明文泄露的敏感密钥。

## PR 链接
- PR : https://github.com/point423/FunMate-Planet/pull/19

## 遇到的问题和解决
1. **问题**：在本地开发时，由于删除了硬编码密钥，Spring Boot 因为找不到 `${JWT_SECRET}` 无法启动。  
   **解决**：在 IntelliJ 运行配置中添加了环境变量，或者在本地创建 `.env` 文件。
2. **问题**：Gitleaks 扫描报出之前的提交历史中存在测试密钥。  
   **解决**：配置了 `gitleaks.toml` 忽略特定的测试字符串，或者直接接受历史记录泄露风险并后续重置密钥。

## 心得体会
Vibe Coding 模式下，AI 像是一个经验丰富的“代码警察”。原本由于开发效率追求而忽略的越权校验，在 AI 的一眼识破下无所遁形。我认识到，安全不应是开发的沉重负担，而是通过 CI 自动化工具融入流程的习惯。
