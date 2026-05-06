# 安全审查贡献说明

姓名：彭静婷  
学号：2312190309  
日期：2026-05-06

## 我完成的工作

### AI 安全审查
- **审查了哪些文件/模块**：`AuthController.java`, `UserController.java`, `ActivityController.java`, `DiaryController.java`, `FriendController.java`, `EvaluationController.java`, `ChatController.java`, `JwtUtil.java`, `AuthService.java`, `FriendService.java`, `DiaryService.java`, `EvaluationService.java`, `ChatService.java`
- **AI 发现的主要问题**：
    1. **高危：硬编码密钥**：`JwtUtil` 中直接声明了用于签名 JWT 的私钥字符串，且该字符串出现在了提交历史中，具有极高的泄露风险。
    2. **高危：水平越权 (IDOR)**：多个 Controller（用户、活动、日记、好友申请、评价、聊天）中的修改和删除接口仅依赖路径 ID 或请求体 ID，登录用户可以绕过身份校验操作他人的私有数据。
    3. **中危：错误信息泄露**：部分异常处理直接返回了详细的报错信息，可能暴露服务器内部结构。
- **我修复了哪些问题**：
    1. **修复硬编码密钥**：将 JWT 密钥从代码中移除，改为从环境变量 `${JWT_SECRET}` 动态读取，并配套提供了 `.env.example` 模板。
    2. **全面修复越权漏洞**：在所有涉及私有数据操作（更新、删除、处理申请等）的 Service 和 Controller 中增加了身份比对逻辑，确保操作者 ID 与数据所属者 ID 一致。
    3. **补全安全测试覆盖**：针对新增的 403 越权分支编写了异常场景测试用例，确保安全加固代码被 100% 覆盖，解决了合并 PR 时的 Codecov Patch Coverage 报错。

### 安全检查清单
- [x] **密码存储**：使用 BCrypt 哈希存储，已在 `AuthService` 中实装。
- [x] **JWT / Session**：Token 具备 24 小时过期时间，且受动态环境变量密钥保护。
- [x] **接口鉴权**：除登录注册外，所有业务接口均通过 `AuthInterceptor` 强制校验 Token。
- [x] **越权访问**：已全面完成用户操作本人数据的逻辑校验（IDOR 修复）。
- [x] **SQL 注入防护**：全面使用 Spring Data JPA (Hibernate)，采用参数化查询，杜绝拼接 SQL。
- [x] **API Key / 密码**：通过环境变量管理，禁止明文提交代码库。
- [x] **.env 文件**：已加入 `.gitignore`，项目根目录提供了模板。

### CI 安全扫描
- **配置了哪个选项**：选项 A（密钥泄露扫描 - Gitleaks）
- **扫描结果**：成功集成。扫描通过，自动化拦截了潜在的硬编码密钥入库风险。

## PR 链接
- PR : https://github.com/point423/FunMate-Planet/pull/19
- https://github.com/point423/FunMate-Planet/pull/20
- https://github.com/point423/FunMate-Planet/pull/21

## 遇到的问题和解决
1. **问题：Codecov 报告 "Patch coverage is 0%" 导致合并受阻**  
   **解决**：在修复 IDOR 越权漏洞后，CI 提示新增的安全校验代码（`if` 逻辑分支）从未被执行。这是因为原有测试集仅模拟了“正常操作”场景。通过在 `UserControllerTest` 等测试类中补全模拟“非法越权访问”的 403 异常测试，成功触发了安全校验逻辑，使安全补丁的覆盖率达到了 100%。
2. **问题：在本地开发时，由于删除了硬编码密钥，Spring Boot 无法启动**  
   **解决**：在 IntelliJ 运行配置中添加了环境变量，或者在本地创建不追踪的 `.env` 文件。同时也通过 `${JWT_SECRET:default}` 占位符确保了开发环境的兼容性。
3. **问题：Gitleaks 拦截了提交历史中的测试密钥**  
   **解决**：由于早期版本提交过明文密钥，扫描器报错。采取了重置密钥字符串并使用 Gitleaks 忽略已知旧记录的处理方式。

## 心得体会
Vibe Coding 带来的高效率有时会让人忽视逻辑严密性，尤其是在处理资源 ID 时容易忽略鉴权。AI 在安全审查中扮演了极其重要的“挑错”角色。通过这次全面加固，我深刻理解到：安全不仅仅是加密，更是对每一个入口的细致鉴权。集成 CI 扫描和覆盖率检查则是从制度上防止“人性的疏忽”，确保安全标准能持续落地并被有效验证。
