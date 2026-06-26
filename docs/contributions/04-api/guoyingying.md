# API 设计与实现贡献说明

姓名：郭盈盈

学号：2312190102

日期：2026-04-01

## 我完成的工作

### 1. API 设计

- [x] 用户认证与用户资料相关接口的前端对接设计：对接 `/auth/login`、`/auth/register`、`/users/me`、`/users/location`，并将新用户登录后的标签初始化流程接入 `useAuth` 与 `userStore`。
- [x] 社交与发现类接口的前端参数约定：封装 `/friends`、`/friends/requests`、`/friends/requests/{id}/handle`、`/discover/nearby`、`/discover/random`、`/discover/ranking` 等请求，统一好友申请、附近用户、排行榜和自动匹配场景的调用方式。
- [x] 活动、日记、聊天相关接口的页面调用约定：对接 `/activities`、`/activity-invitations`、`/diaries`、`/chat/messages`、`/chat/conversations` 等接口，明确分页参数、路径参数、表单上传和状态流转方式。

### 2. 文档对应与接口对齐

- [x] 根据 `docs/api.yaml` 与 `docs/api.md` 对照前端请求路径、请求方法和参数结构，确保前端接口命名与项目 RESTful 设计保持一致。
- [x] 在联调过程中对前端字段格式做兼容适配，例如将标签数组转换为后端需要的逗号分隔字符串，将评分统一转换为 `scoreLevel`，并补齐按用户名搜索、好友申请处理等实际接口路径。

### 3. 前端实现

- [x] HTTP 客户端封装：在 `frontend/src/api/index.ts` 中统一配置 `baseURL`、超时时间、JSON 请求头、Token 注入、响应解包和 401 失效跳转；同时对 `FormData` 请求移除手动 `Content-Type`，保证图片上传接口可正常提交。
- [x] 认证与全局状态联动：在 `frontend/src/api/auth.ts`、`frontend/src/stores/user.ts`、`frontend/src/composables/useAuth.ts` 中完成登录、注册、获取当前用户信息、定位上报的封装，并把 token 持久化、本地登录态、新用户标签初始化跳转整合到一起。
- [x] 用户与社交接口封装：在 `frontend/src/api/user.ts` 中完成用户详情、按用户名查询、个人资料更新、兴趣标签保存、好友申请发送与处理、好友列表获取、互评接口调用等封装，并接入 `TagSetupView`、`MyProfileView`、`UserDetailView`、`ChatView`。
- [x] 搭子发现接口封装：在 `frontend/src/api/dazi.ts` 中完成附近用户、随机匹配、排行榜接口封装，支持半径、标签、分页与经纬度参数；同时增加本地定位回退逻辑，使 `HomeView` 和 `FindPartnerView` 能直接复用同一套接口能力。
- [x] 活动、日记与上传接口封装：在 `frontend/src/api/activity.ts` 中完成活动创建与编辑、活动详情、我的活动、邀请处理、活动评价、日记创建与查询、共享卡片更新、图片上传等 API 封装，并支撑 `StartActivityModal`、`DiaryEditor`、`JournalView`、`ActivityDetailView`、`ChatView` 等页面功能。
- [x] 聊天与 AI 接口封装：在 `frontend/src/api/chat.ts`、`frontend/src/api/ai.ts` 中完成聊天消息列表、会话列表、发送消息以及 AI 建议生成接口封装，支持聊天页和 AI 助手页的实际调用。

## PR 链接

- PR #X: https://github.com/xxx/xxx/pull/X

## 遇到的问题和解决

1. 问题：前后端联调时，部分接口的请求路径和参数格式与前端最初假设不一致，例如好友申请改为 `/friends/requests`，标签字段由数组改为逗号分隔字符串。

   解决：将接口封装集中收口到 `src/api` 层统一调整，避免页面散落修改；同时在 `updateProfile`、`updateTags`、`handleFriendRequest` 等方法中做参数转换，保证页面调用方式稳定。

2. 问题：后端统一返回结构为 `{ code, message, data }`，且上传图片使用 `multipart/form-data`，如果直接沿用默认请求头会影响 Spring 端解析。

   解决：在 `src/api/index.ts` 中增加统一响应解包逻辑，并针对 `FormData` 请求删除手动设置的 `Content-Type`，由浏览器自动补全 boundary，保证上传接口和普通 JSON 接口都能正常工作。

3. 问题：搭子匹配与附近用户场景依赖定位信息，用户未授权定位或刷新页面后容易导致请求参数缺失。

   解决：通过 `useLocation` 先上报当前位置并把经纬度缓存到 `localStorage`，`getNearbyUsers` 再优先读取传入参数，其次回退到本地缓存，降低接口调用失败率。

## 心得体会

这次 API 对接让我更清楚地认识到，前端不仅是“调用接口”，更重要的是把接口差异、登录态、参数格式和错误处理收敛到统一访问层。这样页面只关注业务展示，联调修改也只需要在 `src/api` 和状态层集中处理，整体开发效率和可维护性都会更高。
