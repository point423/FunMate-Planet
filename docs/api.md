# 趣搭星球 - API 接口设计

## 一、接口通用规范

### 基础信息
- 接口前缀：`/api`
- 请求方式：RESTful 风格（GET/POST/PUT/DELETE）
- 数据格式：请求/响应均为 JSON
- 认证方式：JWT Token（放在请求头`Authorization: Bearer {token}`）

### 响应通用格式
{
"code": 200,        
"message": "success",
"data": {}          
}

### 状态码说明
| 状态码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 201 | 资源创建成功 |
| 400 | 请求参数错误 |
| 401 | 未登录/Token过期 |
| 404 | 资源不存在 |
| 500 | 服务端错误 |

---

## 二、核心接口详情

### 1. 认证模块

#### 1.1 用户注册
- 接口路径：`/auth/register`
- 请求方式：POST
- 请求参数：
  {
  "username": "张三",    
  "password": "123456",
  "nickname": "小张"
  }
- 响应数据：
  {
  "code": 201,
  "message": "注册成功",
  "data": null
  }

#### 1.2 用户登录
- 接口路径：`/auth/login`
- 请求方式：POST
- 请求参数：
  {
  "username": "张三",
  "password": "123456"        
  }
- 响应数据：
  {
  "code": 200,
  "message": "登录成功",
  "data": {
  "token": "eyJhbGciOiJIUzI1NiJ9.xxx.xxx",
  "isNewUser": false
  }
  }

#### 1.3 用户登出
- 接口路径：`/auth/logout`
- 请求方式：POST
- 请求头：`Authorization: Bearer {token}`
- 响应数据：
  {
  "code": 200,
  "message": "登出成功",
  "data": null
  }

---

### 2. 用户模块

#### 2.1 获取当前用户信息
- 接口路径：`/users/me`
- 请求方式：GET
- 请求头：`Authorization: Bearer {token}`
- 响应数据：
  {
  "code": 200,
  "message": "success",
  "data": {
  "id": 1001,
  "username": "张三",
  "nickname": "小张",
  "avatar": "http://localhost:8080/static/avatar/1001.jpg",
  "tags": "篮球,电影,美食",
  "followingCount": 10,
  "followerCount": 20,
  "friendCount": 5,
  "averageScore": 4.5
  }
  }

#### 2.2 编辑当前用户资料
- 接口路径：`/users/me`
- 请求方式：PUT
- 请求头：`Authorization: Bearer {token}`
- 请求参数：
  {
  "nickname": "小张更新",
  "avatar": "http://localhost:8080/static/avatar/1001_new.jpg",
  "tags": "篮球,电影,美食,骑行"
  }
- 响应数据：
  {
  "code": 200,
  "message": "更新成功",
  "data": null
  }

#### 2.3 上报用户地理位置
- 接口路径：`/users/location`
- 请求方式：POST
- 请求头：`Authorization: Bearer {token}`
- 请求参数：
  {
  "longitude": 116.403963,
  "latitude": 39.915119    
  }
- 响应数据：
  {
  "code": 200,
  "message": "地理位置上报成功",
  "data": null
  }

#### 2.4 获取指定用户的主页信息
- 接口路径：`/users/{id}`
- 请求方式：GET
- 请求头：`Authorization: Bearer {token}`
- 路径参数：`id` - 用户 ID（必填）
- 响应数据：
  {
  "code": 200,
  "message": "success",
  "data": {
  "id": 1002,
  "nickname": "李四",
  "avatar": "http://localhost:8080/static/avatar/1002.jpg",
  "tags": "电影,美食,骑行",
  "activityCount": 15,
  "averageScore": 4.8,
  "rank": 3,
  "isFollowing": true,
  "isFriend": false,
  "publicDiaries": [],
  "recentActivities": []
  }
  }

---

### 3. 关注模块（单向）

#### 3.1 关注/取消关注用户
- 接口路径：`/users/{id}/follow`
- 请求方式：POST
- 请求头：`Authorization: Bearer {token}`
- 路径参数：`id` - 目标用户 ID（必填）
- 请求参数：
  {
  "follow": true
  }
- 响应数据：
  {
  "code": 200,
  "message": "关注成功",
  "data": null
  }

#### 3.2 获取指定用户的粉丝列表
- 接口路径：`/users/{id}/followers`
- 请求方式：GET
- 请求头：`Authorization: Bearer {token}`
- 路径参数：`id` - 用户 ID（必填）
- URL 参数：
   - `pageNum`: 页码，默认 1
   - `pageSize`: 每页数量，默认 10
- 响应数据：
  {
  "code": 200,
  "message": "success",
  "data": {
  "total": 20,
  "list": []
  }
  }

#### 3.3 获取指定用户的关注列表
- 接口路径：`/users/{id}/following`
- 请求方式：GET
- 请求头：`Authorization: Bearer {token}`
- 路径参数：`id` - 用户 ID（必填）
- URL 参数：
   - `pageNum`: 页码，默认 1
   - `pageSize`: 每页数量，默认 10
- 响应数据：
  {
  "code": 200,
  "message": "success",
  "data": {
  "total": 10,
  "list": []
  }
  }

---

### 4. 好友模块（双向）

#### 4.1 获取我的双向好友列表
- 接口路径：`/friends`
- 请求方式：GET
- 请求头：`Authorization: Bearer {token}`
- 响应数据：
  {
  "code": 200,
  "message": "success",
  "data": [
  {
  "id": 1003,
  "nickname": "王五",
  "avatar": "http://localhost:8080/static/avatar/1003.jpg"
  }
  ]
  }

#### 4.2 获取好友申请列表
- 接口路径：`/friends/requests`
- 请求方式：GET
- 请求头：`Authorization: Bearer {token}`
- 响应数据：
  {
  "code": 200,
  "message": "success",
  "data": {
  "incoming": [],
  "outgoing": []
  }
  }

#### 4.3 发送好友申请
- 接口路径：`/friends/requests`
- 请求方式：POST
- 请求头：`Authorization: Bearer {token}`
- 请求参数：
  {
  "targetUserId": 1002
  }
- 响应数据：
  {
  "code": 201,
  "message": "申请已发送",
  "data": null
  }

#### 4.4 处理好友申请
- 接口路径：`/friends/requests/{id}/handle`
- 请求方式：POST
- 请求头：`Authorization: Bearer {token}`
- 路径参数：`id` - 申请 ID（必填）
- 请求参数：
  {
  "accept": true
  }
- 响应数据：
  {
  "code": 200,
  "message": "处理成功",
  "data": null
  }

---

### 5. 活动模块

#### 5.1 创建活动
- 接口路径：`/activities`
- 请求方式：POST
- 请求头：`Authorization: Bearer {token}`
- 请求参数：
  {
  "title": "周末篮球局",
  "description": "周六下午2点篮球场打球",
  "activityTime": "2026-03-15 14:00:00",
  "location": "西湖文化广场篮球场",
  "maxParticipants": 10
  }
- 响应数据：
  {
  "code": 201,
  "message": "活动创建成功",
  "data": {
  "id": 2001
  }
  }

#### 5.2 分页获取活动列表
- 接口路径：`/activities`
- 请求方式：GET
- 请求头：`Authorization: Bearer {token}`
- URL 参数：
   - `pageNum`: 页码，默认 1
   - `pageSize`: 每页数量，默认 10
   - `status`: 活动状态（0:招募中，1:进行中，2:已结束）
- 响应数据：
  {
  "code": 200,
  "message": "success",
  "data": {
  "total": 50,
  "list": []
  }
  }

#### 5.3 获取活动详情
- 接口路径：`/activities/{id}`
- 请求方式：GET
- 请求头：`Authorization: Bearer {token}`
- 路径参数：`id` - 活动 ID（必填）
- 响应数据：
  {
  "code": 200,
  "message": "success",
  "data": {
  "id": 2001,
  "creator": {},
  "title": "周末篮球局",
  "description": "周六下午2点篮球场打球",
  "activityTime": "2026-03-15 14:00:00",
  "location": "西湖文化广场篮球场",
  "status": 0,
  "participants": []
  }
  }

#### 5.4 参与活动
- 接口路径：`/activities/{id}/join`
- 请求方式：POST
- 请求头：`Authorization: Bearer {token}`
- 路径参数：`id` - 活动 ID（必填）
- 响应数据：
  {
  "code": 200,
  "message": "参与成功",
  "data": null
  }

#### 5.5 结束活动
- 接口路径：`/activities/{id}/end`
- 请求方式：POST
- 请求头：`Authorization: Bearer {token}`
- 路径参数：`id` - 活动 ID（必填）
- 响应数据：
  {
  "code": 200,
  "message": "活动已结束",
  "data": null
  }

---

### 6. 日记模块

#### 6.1 发布活动日记
- 接口路径：`/diaries`
- 请求方式：POST
- 请求头：`Authorization: Bearer {token}`
- 请求参数：
  {
  "activityId": 2001,
  "content": "今天打球超开心！",
  "images": "[\"http://localhost:8080/static/diary/3001_1.jpg\"]",
  "tags": "篮球,运动"
  }
- 响应数据：
  {
  "code": 201,
  "message": "发布成功",
  "data": {
  "id": 3001
  }
  }

#### 6.2 获取日记列表
- 接口路径：`/diaries`
- 请求方式：GET
- 请求头：`Authorization: Bearer {token}`
- URL 参数：
   - `userId`: 用户 ID（不传则查自己的）
   - `pageNum`: 页码，默认 1
   - `pageSize`: 每页数量，默认 10
- 响应数据：
  {
  "code": 200,
  "message": "success",
  "data": {
  "total": 20,
  "list": []
  }
  }

#### 6.3 获取日记详情
- 接口路径：`/diaries/{id}`
- 请求方式：GET
- 请求头：`Authorization: Bearer {token}`
- 路径参数：`id` - 日记 ID（必填）
- 响应数据：
  {
  "code": 200,
  "message": "success",
  "data": {
  "id": 3001,
  "user": {},
  "activity": {},
  "content": "今天打球超开心！",
  "images": ["http://localhost:8080/static/diary/3001_1.jpg"],
  "tags": "篮球,运动",
  "createTime": "2026-03-15 16:00:00"
  }
  }

---

### 7. 评价模块

#### 7.1 评价活动伙伴
- 接口路径：`/evaluations`
- 请求方式：POST
- 请求头：`Authorization: Bearer {token}`
- 请求参数：
  {
  "targetUserId": 1002,
  "activityId": 2001,
  "scoreLevel": 3
  }
- 响应数据：
  {
  "code": 201,
  "message": "评价成功",
  "data": null
  }

---

### 8. 聊天模块

#### 8.1 获取我的会话列表
- 接口路径：`/chat/conversations`
- 请求方式：GET
- 请求头：`Authorization: Bearer {token}`
- 响应数据：
  {
  "code": 200,
  "message": "success",
  "data": []
  }

#### 8.2 获取聊天记录
- 接口路径：`/chat/messages`
- 请求方式：GET
- 请求头：`Authorization: Bearer {token}`
- URL 参数：
   - `targetUserId`: 目标用户 ID（必填）
   - `pageNum`: 页码，默认 1
   - `pageSize`: 每页数量，默认 50
- 响应数据：
  {
  "code": 200,
  "message": "success",
  "data": {
  "total": 100,
  "list": []
  }
  }

#### 8.3 发送消息
- 接口路径：`/chat/messages`
- 请求方式：POST
- 请求头：`Authorization: Bearer {token}`
- 请求参数：
  {
  "receiverId": 1003,
  "content": "明天一起去骑行吗？"
  }
- 响应数据：
  {
  "code": 201,
  "message": "发送成功",
  "data": null
  }

---

### 9. 发现模块

#### 9.1 获取附近的搭子列表
- 接口路径：`/discover/nearby`
- 请求方式：GET
- 请求头：`Authorization: Bearer {token}`
- URL 参数：
   - `radius`: 搜索半径（公里），默认 10
   - `tags`: 兴趣标签，逗号分隔
   - `pageNum`: 页码，默认 1
   - `pageSize`: 每页数量，默认 10
- 响应数据：
  {
  "code": 200,
  "message": "success",
  "data": {
  "total": 50,
  "list": []
  }
  }

#### 9.2 自动匹配：随机推荐一位搭子
- 接口路径：`/discover/random`
- 请求方式：GET
- 请求头：`Authorization: Bearer {token}`
- 响应数据：
  {
  "code": 200,
  "message": "success",
  "data": {}
  }

#### 9.3 获取每周好评排行榜
- 接口路径：`/discover/ranking`
- 请求方式：GET
- 请求头：`Authorization: Bearer {token}`
- 响应数据：
  {
  "code": 200,
  "message": "success",
  "data": []
  }

---

### 10. 文件上传模块

#### 10.1 图片上传
- 接口路径：`/upload/image`
- 请求方式：POST
- 请求头：`Authorization: Bearer {token}`
- 请求参数：`multipart/form-data` 格式，参数名`file`（必填，图片文件）
- 响应数据：
  {
  "code": 200,
  "message": "图片上传成功",
  "data": {
  "url": "http://localhost:8080/static/upload/20260304/123456.jpg"
  }
  }