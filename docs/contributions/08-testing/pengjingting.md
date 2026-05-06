# 软件测试贡献说明

姓名：彭静婷  
学号：2312190309  
角色：后端  
日期：2026-04-24

## 完成的测试工作

### 测试文件
- `backend/src/test/java/com/zjgsu/pjt/backend/service/` 下的所有 Test 类 (UserServiceTest, AuthServiceTest, ActivityServiceTest, ChatServiceTest, FriendServiceTest, FriendshipServiceTest, DiaryServiceTest, EvaluationServiceTest, DiscoverServiceTest)
- `backend/src/test/java/com/zjgsu/pjt/backend/controller/` 下的所有 Test 类 (UserControllerTest, AuthControllerTest, ActivityControllerTest, ChatControllerTest, DiaryControllerTest, EvaluationControllerTest, SocialControllerTest)

### 测试清单
- **正常情况测试**（35 个）：涵盖注册、登录、活动创建、消息发送、关注、好友申请、地理位置查询等全量业务流程。
- **边界 / 异常情况测试**（20 个）：涵盖 401 未登录拦截、404 资源不存在处理、重复注册校验、密码匹配失败场景、分页边界测试。
- **Mock 使用**：
    - **Mockito**：模拟了所有的 Repository 和 Redis 操作，确保测试不依赖物理环境。
    - **MockMvc**：模拟了真实 HTTP 请求，验证了 JWT 拦截器与 Controller 的交互。
    - **H2 数据库**：集成了内存数据库，实现了测试环境的 ApplicationContext 零外部依赖。

### 覆盖率
- **核心模块覆盖率**：**62%** (Service 包)
- **截图佐证**：见 `backend/target/site/jacoco/index.html` 生成的报告截图。

### AI 辅助测试（加分项）
- **使用工具**：ChatGPT-4 / GitHub Copilot
- **Prompt 示例**：
    - "为具有分页逻辑和内存 Map 存储的 ChatService 编写 JUnit 5 单元测试，要求覆盖边缘情况。"
    - "如何解决 Spring Boot 测试中静态变量导致的数据污染问题？"
- **修改过程**：AI 生成了初步的 Mock 模板，人工修复了 BCrypt 密码加密匹配逻辑以及 ChatService 中静态变量导致的数据一致性错误。

## PR 链接
- PR : https://github.com/point423/FunMate-Planet/pull/15

## 遇到的问题和解决
1. **问题**：ChatService 使用静态变量导致测试用例之间数据干扰，断言失败。  
   **解决**：将 Service 内部的 Map 修改为非静态成员变量，由 Spring 容器保证测试方法间的实例隔离。
2. **问题**：Controller 测试持续返回 401 错误。  
   **解决**：在测试类中 Mock 掉 JwtUtil 并添加 Authorization Header 模拟已登录用户。

## 心得体会
通过本次软件测试实践，我深刻理解了“为测试而设计”的开发理念。通过将业务逻辑与持久层彻底解耦，不仅让 Service 变得极其易于测试，也大大提升了代码的健壮性。JaCoCo 的覆盖率数据让我能像扫描雷达一样发现代码中未被触达的盲区，从而有针对性地对异常处理流程进行了补强。
