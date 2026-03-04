# 趣搭星球 - API接口设计
## 一、接口通用规范
### 基础信息
- 接口前缀：`/api`
- 请求方式：RESTful风格（GET/POST/PUT/DELETE）
- 数据格式：请求/响应均为JSON
- 认证方式：JWT Token（放在请求头`Authorization: Bearer {token}`）
- 响应通用格式：
```json
{
  "code": 200,        
  "message": "success",
  "data": {}          
}
```

## 二、核心接口详情
### 1. 认证模块
#### 1.1 用户注册
- 接口路径：`/auth/register`
- 请求方式：POST
- 请求参数：
```json
{
  "username": "张三",    
  "email": "zhangsan@qq.com", 
  "password": "123456", 
  "phone": "13800138000" 
}
```
- 响应数据：
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "userId": 1001,
    "username": "张三"
  }
}
```

#### 1.2 用户登录
- 接口路径：`/auth/login`
- 请求方式：POST
- 请求参数：
```json
{
  "email": "zhangsan@qq.com", 
  "password": "123456"        
}
```
- 响应数据：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.xxx.xxx", 
    "expireTime": "2026-03-10 12:00:00"      
  }
}
```

### 2. 用户模块
#### 2.1 获取当前用户信息
- 接口路径：`/users/me`
- 请求方式：GET
- 请求头：`Authorization: Bearer {token}`
- 响应数据：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 1001,
    "username": "张三",
    "email": "zhangsan@qq.com",
    "avatar": "http://localhost:8080/static/avatar/1001.jpg",
    "interests": ["篮球", "电影", "美食"] 
  }
}
```

#### 2.2 上报用户地理位置
- 接口路径：`/users/location`
- 请求方式：POST
- 请求头：`Authorization: Bearer {token}`
- 请求参数：
```json
{
  "longitude": 116.403963, 
  "latitude": 39.915119    
}
```
- 响应数据：
```json
{
  "code": 200,
  "message": "地理位置上报成功",
  "data": null
}
```

### 3. 搭子推荐模块
#### 3.1 获取搭子推荐列表
- 接口路径：`/dazi/recommend`
- 请求方式：GET
- 请求头：`Authorization: Bearer {token}`
- 请求参数（URL 参数）：
    - radius: 筛选半径（公里），默认 5
    - pageNum: 页码，默认 1
    - pageSize: 每页数量，默认 10
- 响应数据：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "userId": 1002,
        "username": "李四",
        "avatar": "http://localhost:8080/static/avatar/1002.jpg",
        "distance": 1.2, 
        "commonInterests": ["电影", "美食"], 
        "score": 8.5 
      }
    ],
    "total": 20, 
    "pageNum": 1,
    "pageSize": 10
  }
}
```

### 4. 活动日记模块
#### 4.1 创建活动
- 接口路径：`/activities`
- 请求方式：POST
- 请求头：`Authorization: Bearer {token}`
- 请求参数：
```json
{
  "title": "周末篮球局", 
  "description": "周六下午2点篮球场打球", 
  "longitude": 116.403963, 
  "latitude": 39.915119,   
  "time": "2026-03-15 14:00:00", 
  "tags": ["篮球", "运动"] 
}
```
- 响应数据：
```json
{
  "code": 200,
  "message": "活动创建成功",
  "data": {
    "activityId": 2001
  }
}
```

#### 4.2 获取活动详情
- 接口路径：`/activities/{activityId}`
- 请求方式：GET
- 请求头：`Authorization: Bearer {token}`
- 路径参数：`activityId` - 活动 ID（必填）
- 响应数据：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "activityId": 2001,
    "title": "周末篮球局",
    "description": "周六下午2点篮球场打球",
    "longitude": 116.403963,
    "latitude": 39.915119,
    "time": "2026-03-15 14:00:00",
    "tags": ["篮球", "运动"],
    "creator": {
      "userId": 1001,
      "username": "张三"
    },
    "participants": [ 
      {
        "userId": 1002,
        "username": "李四"
      }
    ],
    "diaryList": [ 
      {
        "diaryId": 3001,
        "content": "今天打球超开心！",
        "images": ["http://localhost:8080/static/diary/3001_1.jpg"],
        "createTime": "2026-03-15 16:00:00",
        "author": "李四"
      }
    ]
  }
}
```

### 5. 文件上传模块
#### 5.1 图片上传
- 接口路径：`/upload/image`
- 请求方式：POST
- 请求头：`Authorization: Bearer {token}`
- 请求参数：`multipart/form-data` 格式，参数名`file`（必填，图片文件）
- 响应数据：
```json
{
  "code": 200,
  "message": "图片上传成功",
  "data": {
    "url": "http://localhost:8080/static/upload/20260304/123456.jpg" 
  }
}
```


