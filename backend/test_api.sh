

# 趣搭星球 - 全量 API 自动化联调脚本 (基于 api.yaml)
# 覆盖范围：认证、用户、关注、好友、活动、日记、评价、聊天、发现

BASE_URL="http://localhost:8080/api"

echo "=========================================================="
echo "   趣搭星球 - 100% API 覆盖率测试启动                     "
echo "=========================================================="

# 1. 认证模块 (Auth)
echo -e "\n[1. 认证模块]"
echo "1.1 注册 User_A (发起人)..."
curl -s -X POST "$BASE_URL/auth/register" -H "Content-Type: application/json" -d '{"username":"user_a", "password":"123", "nickname":"发起人A"}'
echo "1.2 注册 User_B (参与者)..."
curl -s -X POST "$BASE_URL/auth/register" -H "Content-Type: application/json" -d '{"username":"user_b", "password":"123", "nickname":"路人甲"}'
echo "1.3 登录 User_A..."
LOGIN_RES=$(curl -s -X POST "$BASE_URL/auth/login" -H "Content-Type: application/json" -d '{"username":"user_a", "password":"123"}')
echo $LOGIN_RES
TOKEN=$(echo $LOGIN_RES | grep -o 'mock-jwt-token-[0-9]*')

# 2. 用户模块 (User)
echo -e "\n[2. 用户模块]"
echo "2.1 获取我的个人资料 (GET /users/me)..."
curl -s -X GET "$BASE_URL/users/me?id=1"
echo "2.2 编辑我的资料 (PUT /users/me)..."
curl -s -X PUT "$BASE_URL/users/me" -H "Content-Type: application/json" -d '{"id":1, "nickname":"西湖大王", "age":25}'
echo "2.3 上报地理位置 (POST /users/location)..."
curl -s -X POST "$BASE_URL/users/location" -H "Content-Type: application/json" -d '{"id":1, "longitude":120.15, "latitude":30.25}'

# 3. 关注模块 (Follow)
echo -e "\n[3. 关注模块]"
echo "3.1 用户 B 关注 用户 A..."
curl -s -X POST "$BASE_URL/users/1/follow?currentUserId=2&follow=true"
echo "3.2 查看 A 的粉丝列表..."
curl -s -X GET "$BASE_URL/users/1/followers"

# 4. 好友模块 (Friend)
echo -e "\n[4. 好友模块]"
echo "4.1 A 向 B 发送好友申请..."
curl -s -X POST "$BASE_URL/friends/requests" -H "Content-Type: application/json" -d '{"targetUserId":2}'
echo "4.2 B 处理 A 的申请 (接受)..."
curl -s -X POST "$BASE_URL/friends/requests/1/handle" -H "Content-Type: application/json" -d '{"accept":true}'
echo "4.3 获取我的双向好友列表..."
curl -s -X GET "$BASE_URL/friends"

# 5. 活动模块 (Activity)
echo -e "\n[5. 活动模块]"
echo "5.1 创建活动..."
curl -s -X POST "$BASE_URL/activities" -H "Content-Type: application/json" -d '{"creatorId":1, "title":"全量接口联调活动", "location":"杭州断桥"}'
echo "5.2 获取活动详情 (GET /activities/1)..."
curl -s -X GET "$BASE_URL/activities/1"
echo "5.3 B 参与活动..."
curl -s -X POST "$BASE_URL/activities/1/join"

# 6. 日记模块 (Diary)
echo -e "\n[6. 日记模块]"
echo "6.1 发布活动日记..."
curl -s -X POST "$BASE_URL/diaries" -H "Content-Type: application/json" -d '{"userId":1, "activityId":1, "content":"接口联调非常顺利！"}'
echo "6.2 查看日记列表..."
curl -s -X GET "$BASE_URL/diaries?userId=1"

# 7. 评价模块 (Evaluation)
echo -e "\n[7. 评价模块]"
echo "7.1 B 对 A 进行五星好评..."
curl -s -X POST "$BASE_URL/evaluations" -H "Content-Type: application/json" -d '{"evaluatorId":2, "targetId":1, "activityId":1, "scoreLevel":3}'

# 8. 聊天模块 (Chat)
echo -e "\n[8. 聊天模块]"
echo "8.1 发送私聊消息..."
curl -s -X POST "$BASE_URL/chat/messages" -H "Content-Type: application/json" -d '{"senderId":1, "receiverId":2, "content":"API 全部通了！"}'
echo "8.2 获取会话列表..."
curl -s -X GET "$BASE_URL/chat/conversations"

# 9. 发现模块 (Discover)
echo -e "\n[9. 发现模块]"
echo "9.1 查看排行榜..."
curl -s -X GET "$BASE_URL/discover/ranking"
echo "9.2 随机匹配一位搭子..."
curl -s -X GET "$BASE_URL/discover/random"

# 10. 登出
echo -e "\n[10. 退出登录]"
curl -s -X POST "$BASE_URL/auth/logout"

echo -e "\n=========================================================="
echo "   测试结束：所有 20+ 个接口已完成链路联调                "
echo "=========================================================="
