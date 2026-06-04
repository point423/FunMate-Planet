# 后端开发贡献说明

**姓名：彭静婷
**学号：2312190309
**日期：2026-04-06

---

## 我完成的工作

### API 实现

#### ✅ 用户认证 API（注册 / 登录）
- **实现文件：** `AuthController.java`, `AuthService.java`, `JwtUtil.java`
- **功能说明：**
   - 用户注册：支持用户名、密码、昵称、年龄、性别等信息注册，密码采用 BCrypt 加密存储
   - 用户登录：验证用户名和密码，生成 JWT Token 返回给前端
   - 获取当前用户信息：通过 Token 解析用户 ID，返回用户详细信息
   - 用户登出：提供登出接口（Token 由前端清除）
- **接口列表：**
   - `POST /api/auth/register` - 用户注册
   - `POST /api/auth/login` - 用户登录
   - `GET /api/auth/me` - 获取当前用户信息
   - `POST /api/auth/logout` - 用户登出

#### ✅ 业务资源 1 CRUD：活动管理 (Activity)
- **实现文件：** `ActivityController.java`, `ActivityService.java`, `ActivityRepository.java`, `Activity.java`
- **功能说明：**
   - 创建活动：支持标题、描述、时间、地点、最大参与人数等字段
   - 查询活动：支持分页查询、按状态筛选、查看活动详情
   - 更新活动：修改活动基本信息
   - 删除活动：软删除或硬删除活动记录
   - 报名活动：用户报名参加活动
   - 查看参与者：获取活动的所有参与者列表
- **接口列表：**
   - `POST /api/activities` - 创建活动
   - `GET /api/activities` - 获取活动列表（分页）
   - `GET /api/activities/{id}` - 获取活动详情
   - `PUT /api/activities/{id}` - 更新活动
   - `DELETE /api/activities/{id}` - 删除活动
   - `POST /api/activities/{id}/join` - 报名活动
   - `GET /api/activities/{id}/participants` - 获取参与者

#### ✅ 业务资源 2 CRUD：好友系统 (Friend)
- **实现文件：** `FriendController.java`, `FriendService.java`, `FriendRequestRepository.java`, `FriendshipRepository.java`
- **功能说明：**
   - 发送好友申请：用户 A 向用户 B 发送好友请求
   - 查看申请列表：分别查看收到的申请和发出的申请
   - 处理好友申请：接受或拒绝好友请求，自动建立双向好友关系
   - 查看好友列表：获取当前用户的所有好友
   - 删除好友：解除好友关系
   - 删除申请：取消或删除好友申请记录
- **接口列表：**
   - `POST /api/friends/requests` - 发送好友申请
   - `GET /api/friends/requests` - 获取申请列表
   - `GET /api/friends/requests/{id}` - 获取申请详情
   - `POST /api/friends/requests/{id}/handle` - 处理申请
   - `DELETE /api/friends/requests/{id}` - 删除申请
   - `GET /api/friends` - 获取好友列表
   - `GET /api/friends/{id}` - 获取好友详情
   - `DELETE /api/friends/{id}` - 删除好友

#### ✅ 其他核心模块实现
- **日记模块 (Diary)：** 完整的 CRUD，支持关联活动、图片上传
- **评价模块 (Evaluation)：** 完整的 CRUD，支持评分和平均分自动计算
- **聊天模块 (Chat)：** 消息发送、会话管理、聊天记录查询
- **发现模块 (Discover)：** 附近的人（Redis Geo）、排行榜、随机匹配
- **用户上传 (Upload)：** 图片上传到服务器，返回访问 URL

#### ✅ 统一错误响应
- **实现文件：** `Result.java`, `GlobalExceptionHandler.java`
- **功能说明：**
   - 统一响应格式：`{ code: 0, msg: "success", data: {...} }`
   - 全局异常处理：捕获参数验证异常、业务异常、运行时异常
   - 标准化错误码：200/0 成功，400 参数错误，401 未授权，404 不存在，500 服务器错误

---

### 数据库

#### ✅ 数据模型定义
- **实体类设计：**
   - `User` - 用户表（ID、用户名、密码、昵称、头像、年龄、性别、标签、位置、平均评分）
   - `Activity` - 活动表（ID、创建者、标题、描述、时间、地点、最大人数、状态）
   - `ActivityDiary` - 活动日记表（ID、用户ID、活动ID、内容、图片、创建时间）
   - `FriendRequest` - 好友申请表（ID、发送者、接收者、状态、创建时间）
   - `Friendship` - 好友关系表（ID、用户ID、好友ID、创建时间）
   - `UserEvaluation` - 用户评价表（ID、评价者、被评价者、活动ID、评分等级、创建时间）

#### ✅ ORM 配置
- **技术栈：** Spring Data JPA + Hibernate
- **配置文件：** `application.yml`
  ```yaml
  spring:
    jpa:
      hibernate:
        ddl-auto: update  # 自动同步实体类到数据库
      show-sql: true      # 显示 SQL 语句
      format_sql: true    # 格式化 SQL
  ```

- **Repository 层：** 继承 `JpaRepository`，支持基础 CRUD 和自定义查询方法

#### ✅ 数据库迁移脚本
- 使用 JPA 的 `ddl-auto: update` 自动建表
- 首次启动时自动创建所有表结构
- 支持字段变更自动同步（新增字段、修改类型等）

---

### 部署

#### ✅ Dockerfile 编写
- **文件位置：** `backend/Dockerfile`
- **构建流程：**
  ```dockerfile
  FROM maven:3.9-eclipse-temurin-21 AS build
  WORKDIR /app
  COPY pom.xml .
  COPY src ./src
  RUN mvn clean package -DskipTests
  
  FROM eclipse-temurin:21-jre
  WORKDIR /app
  COPY --from=build /app/target/*.jar app.jar
  EXPOSE 8080
  ENTRYPOINT ["java", "-jar", "app.jar"]
  ```


#### ✅ docker-compose.yml 配置
- **服务编排：**
   - `mysql` - MySQL 8.0 数据库容器
   - `redis` - Redis 缓存容器
   - `backend` - Spring Boot 后端应用容器
- **关键配置：**
   - 健康检查：确保 MySQL 和 Redis 就绪后再启动后端
   - 环境变量：动态注入数据库主机、密码、Redis 主机
   - 数据卷持久化：MySQL 数据、上传文件映射到宿主机
  ```yaml
  services:
    mysql:
      healthcheck:
        test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
        interval: 5s
        retries: 10
    backend:
      depends_on:
        mysql:
          condition: service_healthy
        redis:
          condition: service_healthy
  ```


#### ✅ 本地联调验证
- **测试脚本：** `test_api.sh` - 全量 API 自动化测试
- **测试结果：** **49/49 接口全部通过** ✅
- **测试覆盖：**
   - 认证模块（JWT）：6 个接口
   - 图片上传：1 个接口
   - 用户模块：4 个接口
   - 活动模块：7 个接口
   - 日记模块：5 个接口
   - 好友模块：8 个接口
   - 聊天模块：4 个接口
   - 评价模块：6 个接口
   - 发现模块：6 个接口
   - 权限测试：2 个场景

---

## PR 链接

- **PR :** https://github.com/[你的GitHub用户名]/FunMate-Planet/pull/12


---

## 遇到的问题和解决

### 1. 问题：MySQL 连接失败 - Communications link failure
**现象：** 后端容器启动时报错 `Communications link failure`，无法连接 MySQL  
**原因：** Docker Compose 的 `depends_on` 只等待容器启动，不等待 MySQL 服务完全就绪  
**解决：**
- 在 `docker-compose.yml` 中为 MySQL 添加 `healthcheck` 配置
- 修改 `depends_on` 使用 `condition: service_healthy`
- 后端启动前会等待 MySQL 健康检查通过
```yaml
mysql:
  healthcheck:
    test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
    interval: 5s
    timeout: 5s
    retries: 10
backend:
  depends_on:
    mysql:
      condition: service_healthy
```


### 2. 问题：RedisTemplate Bean 缺失
**现象：** 启动报错 `No qualifying bean of type 'RedisTemplate' available`  
**原因：** `RedisConfig` 类缺少 `@Configuration` 和 `@Bean` 注解  
**解决：**
- 为 `RedisConfig` 类添加 `@Configuration` 注解
- 为 `redisTemplate()` 方法添加 `@Bean` 注解
- 配置 Key 和 Value 的序列化器（StringRedisSerializer + GenericJackson2JsonRedisSerializer）

### 3. 问题：好友模块 ID 提取失败导致测试失败
**现象：** 测试脚本中硬编码的用户 ID 与实际不符，导致好友相关接口测试失败  
**原因：** 每次测试生成的用户 ID 不同，登录接口返回的数据中没有包含 `id` 字段  
**解决：**
- 从用户列表接口动态查找用户 B 的真实 ID
- 如果找不到，再从 `auth/me` 接口获取
- 从好友申请列表的 `incoming` 数组中精确提取申请 ID
- 允许删除已处理的申请返回 404（正常情况）

### 4. 问题：部分 Controller 缺少 PUT/DELETE 方法
**现象：** 测试显示 `Request method 'PUT' is not supported`  
**原因：** DiaryController 和部分 Controller 只实现了 GET 和 POST，缺少更新和删除接口  
**解决：**
- 为 DiaryController 添加 `@PutMapping("/{id}")` 和 `@DeleteMapping("/{id}")`
- 为 UserController 添加 `GET /api/users` 和 `PUT /api/users/{id}`
- 为 ActivityController 添加 `GET /api/activities/{id}/participants`
- 遵循 RESTful 规范补全所有 CRUD 接口

### 5. 问题：密码明文存储存在安全隐患
**现象：** 数据库中密码以明文形式存储  
**解决：**
- 引入 `spring-security-crypto` 依赖
- 使用 `BCryptPasswordEncoder` 对密码进行加密
- 注册时加密存储，登录时使用 `passwordEncoder.matches()` 验证

---

## 心得体会

通过本次 FunMate-Planet 后端开发实践，我获得了以下收获：

### 1. **Spring Boot 全栈开发能力**
- 掌握了 Spring Boot 项目的完整架构：Controller → Service → Repository
- 理解了依赖注入（DI）、面向切面编程（AOP）等核心概念
- 学会了使用 Spring Data JPA 进行数据库操作，简化了 CRUD 开发

### 2. **RESTful API 设计规范**
- 深入理解了 RESTful 风格：资源命名、HTTP 方法语义、状态码使用
- 学会了设计统一的响应格式和错误处理机制
- 掌握了 API 版本管理、分页查询、参数验证等最佳实践

### 3. **安全认证与授权**
- 实现了基于 JWT 的无状态认证机制
- 理解了 Token 的生成、验证、刷新流程
- 学会了使用拦截器实现统一的权限校验

### 4. **Docker 容器化部署**
- 掌握了 Dockerfile 编写和多阶段构建优化
- 学会了使用 Docker Compose 编排多容器应用
- 理解了健康检查、数据卷、网络配置等关键概念

### 5. **问题解决与调试能力**
- 学会了通过日志定位问题（MySQL 连接、Bean 注入等）
- 掌握了自动化测试脚本编写，提高测试效率
- 培养了系统性思维，从架构层面考虑问题

### 6. **团队协作与代码规范**
- 遵循 Controller-Service 分层架构，保证controller只调用service,Servic里写具体实现逻辑，保持代码清晰
- 使用统一的代码风格和注释规范
- 通过 Git PR 流程进行代码审查和合并

