# 趣搭星球 - 数据库设计

## 一、核心数据表设计

### 1. 用户表 (user)
存储用户核心信息、地理位置、社交统计及综合评分。
| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| id | BIGINT | 用户 ID | 主键，自增 |
| username | VARCHAR(50) | 用户名 | 唯一，非空 |
| password | VARCHAR(100) | 密码（加密） | 非空 |
| nickname | VARCHAR(50) | 昵称 | |
| avatar | VARCHAR(255) | 头像 URL | |
| age | INT | 年龄 | |
| gender | TINYINT | 性别 (0:保密, 1:男, 2:女) | |
| tags | VARCHAR(255) | 兴趣标签（逗号分隔） | |
| longitude | DECIMAL(10,7) | 经度（用于 Redis Geo） | |
| latitude | DECIMAL(10,7) | 纬度（用于 Redis Geo） | |
| following_count | INT | 关注数 | 默认 0 |
| follower_count | INT | 粉丝数 | 默认 0 |
| average_score | DECIMAL(3,2) | 平均评价得分（用于排行榜） | 默认 0.0 |
| create_time | DATETIME | 注册时间 | 非空 |

### 2. 好友/关注表 (friendship)
实现关注、粉丝的具体关系明细。
| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| id | BIGINT | 记录 ID | 主键 |
| user_id | BIGINT | 关注者 ID | 外键 (关联 user.id) |
| friend_id | BIGINT | 被关注者 ID | 外键 (关联 user.id) |
| create_time | DATETIME | 关注时间 | |

### 3. 活动表 (activity)
| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| id | BIGINT | 活动 ID | 主键 |
| creator_id | BIGINT | 发起人 ID | 外键 (关联 user.id) |
| title | VARCHAR(100) | 标题 | 非空 |
| description | TEXT | 描述 | |
| activity_time | DATETIME | 活动预定时间 | |
| location | VARCHAR(255) | 地点名称 | |
| status | TINYINT | 状态 (0:招募中, 1:进行中, 2:已结束) | |
| create_time | DATETIME | 创建时间 | |

### 4. 活动参与记录表 (activity_participant)
| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| id | BIGINT | ID | 主键 |
| activity_id | BIGINT | 活动 ID | 外键 (关联 activity.id) |
| user_id | BIGINT | 参与者 ID | 外键 (关联 user.id) |
| join_time | DATETIME | 加入时间 | |

### 5. 活动日记表 (activity_diary)
用于个人主页展示，记录每次活动的精彩瞬间。
| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| id | BIGINT | ID | 主键 |
| user_id | BIGINT | 所属用户 ID | 外键 (关联 user.id) |
| activity_id | BIGINT | 关联活动 ID | 外键 (关联 activity.id) |
| content | TEXT | 日记文字 | |
| images | TEXT | 图片 URL 列表 (JSON) | |
| tags | VARCHAR(255) | 本次活动自定义标签 | |
| create_time | DATETIME | 发布时间 | |

### 6. 用户评价表 (user_evaluation)
活动结束后互评，计算排行榜权重。
| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| id | BIGINT | ID | 主键 |
| evaluator_id | BIGINT | 评价人 ID | 外键 (关联 user.id) |
| target_id | BIGINT | 被评价人 ID | 外键 (关联 user.id) |
| activity_id | BIGINT | 活动 ID | 外键 (关联 activity.id) |
| score_level | TINYINT | 评价 (1:低, 2:中, 3:高) | |
| create_time | DATETIME | 评价时间 | |

### 7. 聊天消息表 (chat_message)
| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| id | BIGINT | ID | 主键 |
| sender_id | BIGINT | 发送者 ID | 外键 (关联 user.id) |
| receiver_id | BIGINT | 接收者 ID | 外键 (关联 user.id) |
| content | TEXT | 消息内容 | |
| is_read | BOOLEAN | 是否已读 | |
| create_time | DATETIME | 发送时间 | |

## 二、建表 SQL

```sql
CREATE DATABASE IF NOT EXISTS funmate_planet DEFAULT CHARSET utf8mb4;
USE funmate_planet;

-- 1. 用户表
CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    nickname VARCHAR(50),
    avatar VARCHAR(255),
    age INT,
    gender TINYINT DEFAULT 0 COMMENT '0:保密, 1:男, 2:女',
    tags VARCHAR(255),
    longitude DECIMAL(10,7),
    latitude DECIMAL(10,7),
    following_count INT DEFAULT 0,
    follower_count INT DEFAULT 0,
    average_score DECIMAL(3,2) DEFAULT 0.0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. 好友/关注表 ( user_id 和 friend_id 都关联 user.id )
CREATE TABLE friendship (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    friend_id BIGINT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY (user_id, friend_id),
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (friend_id) REFERENCES user(id)
);

-- 3. 活动表
CREATE TABLE activity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    creator_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    activity_time DATETIME,
    location VARCHAR(255),
    status TINYINT DEFAULT 0 COMMENT '0:招募中, 1:进行中, 2:已结束',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (creator_id) REFERENCES user(id)
);

-- 4. 参与记录
CREATE TABLE activity_participant (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    activity_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    join_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (activity_id) REFERENCES activity(id),
    FOREIGN KEY (user_id) REFERENCES user(id)
);

-- 5. 活动日记
CREATE TABLE activity_diary (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    activity_id BIGINT NOT NULL,
    content TEXT,
    images TEXT,
    tags VARCHAR(255),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (activity_id) REFERENCES activity(id)
);

-- 6. 评价表
CREATE TABLE user_evaluation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    evaluator_id BIGINT NOT NULL,
    target_id BIGINT NOT NULL,
    activity_id BIGINT NOT NULL,
    score_level TINYINT NOT NULL COMMENT '1:低, 2:中, 3:高',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (evaluator_id) REFERENCES user(id),
    FOREIGN KEY (target_id) REFERENCES user(id),
    FOREIGN KEY (activity_id) REFERENCES activity(id)
);

-- 7. 消息表
CREATE TABLE chat_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    content TEXT,
    is_read BOOLEAN DEFAULT FALSE,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES user(id),
    FOREIGN KEY (receiver_id) REFERENCES user(id)
);
```
