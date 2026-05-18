# CI/CD 配置贡献说明

姓名：彭静婷  
学号：2312190309  
角色：后端  
日期：2026-04-29

## 完成的工作

### 工作流相关
- [x] **参与编写 / 审查 `.github/workflows/ci.yml`**：创建了适配 Java Maven 的自动化流水线，实现了后端与前端测试的并行运行。
- [x] **配置 Codecov 覆盖率上传**：配置了 `jacoco.xml` 的自动上传，并使用 `flags: backend` 进行了模块区分。
- [x] **添加 README 状态徽章**：在项目主页顶部集成了 CI 运行状态及后端核心 Service 覆盖率徽章。

### 代码适配
- [x] **本地测试命令与 CI 一致**：本地通过 `mvn test` 运行，CI 环境同样调用 Maven 生命周期，确保了环境一致性。
- [x] **代码通过 Lint 检查**：通过 Maven Compiler 插件严格模式确保了代码零警告。
- [x] **核心覆盖率达标**：Service 模块指令覆盖率已达到 62%，满足作业要求。

### 可选项
- [x] **使用 act 本地验证工作流**：在本地环境模拟了 GitHub Actions 的执行过程，验证了 YAML 配置的正确性。

## PR 链接
- PR : https://github.com/point423/FunMate-Planet/pull/17

## CI 运行链接
- https://github.com/point423/FunMate-Planet/actions/workflows/ci.yml

## 遇到的问题和解决
1. **问题**：CI 环境下默认没有 MySQL 和 Redis，导致后端测试上下文加载失败。  
   **解决**：在项目中引入了 H2 内存数据库，并配置了测试专用的 `application-test.yml`，使得 CI 流水线可以在无外部数据库依赖的情况下完成 100% 的自动化测试。
2. **问题**：GitHub Actions 中 Maven 依赖下载速度慢。  
   **解决**：配置了 `actions/setup-java@v4` 的 `cache: maven` 功能，有效缩短了后续构建的耗时。

## 心得体会
通过本次 CI/CD 的配置，我深刻体会到了“自动化”在软件质量控制中的核心地位。原本需要手动运行的测试和覆盖率统计，现在只要一次 `git push` 就能自动完成，极大地解放了开发者的生产力。同时，README 中的绿色徽章不仅是一种成就感，更是对代码健壮性的公开承诺。
