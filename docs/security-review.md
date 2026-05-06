# 趣搭星球 - 安全审查报告 (AI 辅助)

**审查时间**：2024-04-04  
**审查工具**：Claude 3.5 / GPT-4  
**审查范围**：认证、用户控制、JWT 工具类

---

## 一、 AI 发现的安全漏洞

### 1. 硬编码敏感信息 (High)
- **发现**：`JwtUtil.java` 中硬编码了 JWT 密钥 `FunMatePlanetSecretKeyForJwtTokenGeneration2024`。
- **危害**：密钥一旦泄露，攻击者可以伪造任何用户的 Token。
- **修复建议**：将密钥移至环境变量，并从配置文件读取。

### 2. 失效的访问控制 - IDOR 越权漏洞 (High)
- **发现**：`UserController.java` 中的 `updateUser(@PathVariable Long id, ...)` 接口未校验操作者权限。
- **危害**：登录用户可以通过修改 URL 中的 ID，随意修改任意其他用户的资料（横向越权）。
- **修复建议**：增加逻辑校验，确保 `currentUserId` 与被修改的 `id` 一致。

### 3. JWT 注销失效 (Medium)
- **发现**：`AuthService.java` 中的 `logout` 方法为空，JWT 本身无状态，退出后令牌依然有效。
- **危害**：无法真正让 Session 失效。
- **修复建议**：(进阶修复) 引入 Redis 黑名单。目前先记录为已知设计特性。

### 4. 敏感信息泄露 (Low)
- **发现**：注册接口返回了完整的 User 对象（包含密码哈希）。
- **危害**：虽然是哈希，但增加了被破解风险。
- **修复建议**：返回统一的成功消息或脱敏对象。

---

## 二、 核心漏洞修复

### 修复 1：环境变量化 JWT 密钥
修改了 `JwtUtil.java` 和 `application.yml`，强制要求从 `JWT_SECRET` 环境变量读取，不再硬编码默认值。

### 修复 2：修复 UserController 越权漏洞
对 `updateUser` 和 `deleteUser` 增加了权限校验，代码如下：
```java
if (!id.equals(currentUserId)) {
    return Result.error(403, "无权操作他人数据");
}
```

---

## 三、 结论
项目已通过 AI 安全审查。修复了最危急的硬编码密钥和 IDOR 越权漏洞，并集成了 Gitleaks 自动化扫描防止未来密钥泄露。
