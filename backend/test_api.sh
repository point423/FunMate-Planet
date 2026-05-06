

# 趣搭星球 - 全量 API 自动化测试脚本
# 测试范围：认证、用户、活动、日记、好友、聊天、评价、发现、上传

BASE_URL="http://localhost:8080/api"
PASS=0
FAIL=0

echo "正在重置 Redis 测试环境..."
docker exec fm-redis redis-cli FLUSHALL > /dev/null
echo "Redis 数据已重置。"

echo "=========================================================="
echo "   趣搭星球 - 全量业务链路联调测试启动                     "
echo "=========================================================="

check_response() {
    local test_name=$1
    local response=$2
    local expected_code=${3:-0}

    local code=$(echo $response | grep -o '"code":[0-9]*' | cut -d':' -f2)

    if [ "$code" == "$expected_code" ]; then
        echo "✅ $test_name (code: $code)"
        PASS=$((PASS + 1))
    else
        echo "❌ $test_name (期望code: $expected_code, 实际code: $code)"
        echo "   响应: $response"
        FAIL=$((FAIL + 1))
    fi
}

# ==================== 1. 认证模块 ====================
echo -e "\n[1. 认证模块 - JWT]"

TIMESTAMP=$(date +%s)
USER_A="test_user_a_${TIMESTAMP}"
USER_B="test_user_b_${TIMESTAMP}"

REG_A=$(curl -s -X POST "$BASE_URL/auth/register" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"${USER_A}\",\"password\":\"123456\",\"nickname\":\"测试用户A\",\"age\":20,\"gender\":1}")
check_response "1.1 注册用户A" "$REG_A" "0"

REG_B=$(curl -s -X POST "$BASE_URL/auth/register" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"${USER_B}\",\"password\":\"123456\",\"nickname\":\"测试用户B\",\"age\":22,\"gender\":2}")
check_response "1.2 注册用户B" "$REG_B" "0"

LOGIN_A=$(curl -s -X POST "$BASE_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"${USER_A}\",\"password\":\"123456\"}")
check_response "1.3 登录用户A" "$LOGIN_A" "0"
TOKEN_A=$(echo $LOGIN_A | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
AUTH_A="Authorization: Bearer $TOKEN_A"

LOGIN_B=$(curl -s -X POST "$BASE_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"${USER_B}\",\"password\":\"123456\"}")
check_response "1.4 登录用户B" "$LOGIN_B" "0"
TOKEN_B=$(echo $LOGIN_B | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
AUTH_B="Authorization: Bearer $TOKEN_B"

ME=$(curl -s -X GET "$BASE_URL/auth/me" -H "$AUTH_A")
check_response "1.5 获取当前用户信息" "$ME" "0"

LOGOUT=$(curl -s -X POST "$BASE_URL/auth/logout" -H "$AUTH_A")
check_response "1.6 登出" "$LOGOUT" "0"

if [ -z "$TOKEN_A" ]; then
    echo "❌ Token 获取失败，终止测试"
    exit 1
fi
echo "✅ JWT 认证成功，Token: ${TOKEN_A:0:20}..."

# ==================== 2. 图片上传模块 ====================
echo -e "\n[2. 图片上传模块]"

echo "fake-image-content-for-testing" > test_upload.jpg

UPLOAD=$(curl -s -X POST "$BASE_URL/upload/image" \
    -H "$AUTH_A" \
    -F "file=@test_upload.jpg")
check_response "2.1 上传图片" "$UPLOAD" "0"
IMG_URL=$(echo $UPLOAD | grep -o '"url":"[^"]*"' | cut -d'"' -f4)
echo "   图片URL: $IMG_URL"

rm -f test_upload.jpg

# ==================== 3. 用户模块 ====================
echo -e "\n[3. 用户模块]"

USERS=$(curl -s -X GET "$BASE_URL/users" -H "$AUTH_A")
check_response "3.1 获取用户列表" "$USERS" "0"

USER_DETAIL=$(curl -s -X GET "$BASE_URL/users/1" -H "$AUTH_A")
check_response "3.2 获取用户详情" "$USER_DETAIL" "0"

UPDATE_USER=$(curl -s -X PUT "$BASE_URL/users/1" \
    -H "$AUTH_A" \
    -H "Content-Type: application/json" \
    -d '{"nickname":"更新后的昵称A","age":21,"tags":"运动,旅行"}')
check_response "3.3 更新用户信息" "$UPDATE_USER" "0"

UPDATE_LOC=$(curl -s -X POST "$BASE_URL/users/location" \
    -H "$AUTH_A" \
    -H "Content-Type: application/json" \
    -d '{"longitude":120.15,"latitude":30.25}')
check_response "3.4 更新用户位置" "$UPDATE_LOC" "0"

# ==================== 4. 活动模块 ====================
echo -e "\n[4. 活动模块 - CRUD]"

CREATE_ACTIVITY=$(curl -s -X POST "$BASE_URL/activities" \
    -H "$AUTH_A" \
    -H "Content-Type: application/json" \
    -d '{"title":"周末爬山活动","description":"一起去爬山吧","activityTime":"2026-04-10T08:00:00","location":"杭州西湖","maxParticipants":10}')
check_response "4.1 创建活动" "$CREATE_ACTIVITY" "0"
ACTIVITY_ID=$(echo $CREATE_ACTIVITY | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo "   活动ID: $ACTIVITY_ID"

ACTIVITIES=$(curl -s -X GET "$BASE_URL/activities" -H "$AUTH_A")
check_response "4.2 获取活动列表" "$ACTIVITIES" "0"

ACTIVITY_DETAIL=$(curl -s -X GET "$BASE_URL/activities/$ACTIVITY_ID" -H "$AUTH_A")
check_response "4.3 获取活动详情" "$ACTIVITY_DETAIL" "0"

UPDATE_ACTIVITY=$(curl -s -X PUT "$BASE_URL/activities/$ACTIVITY_ID" \
    -H "$AUTH_A" \
    -H "Content-Type: application/json" \
    -d '{"title":"周末爬山活动(已更新)","description":"更新后的描述","activityTime":"2026-04-10T09:00:00","location":"杭州灵隐寺","maxParticipants":15}')
check_response "4.4 更新活动" "$UPDATE_ACTIVITY" "0"

JOIN_ACTIVITY=$(curl -s -X POST "$BASE_URL/activities/$ACTIVITY_ID/join" -H "$AUTH_B")
check_response "4.5 用户B报名活动" "$JOIN_ACTIVITY" "0"

PARTICIPANTS=$(curl -s -X GET "$BASE_URL/activities/$ACTIVITY_ID/participants" -H "$AUTH_A")
check_response "4.6 获取活动参与者" "$PARTICIPANTS" "0"

DELETE_ACTIVITY=$(curl -s -X DELETE "$BASE_URL/activities/$ACTIVITY_ID" -H "$AUTH_A")
check_response "4.7 删除活动" "$DELETE_ACTIVITY" "0"

# ==================== 5. 日记模块 ====================
echo -e "\n[5. 日记模块 - CRUD]"

CREATE_ACTIVITY2=$(curl -s -X POST "$BASE_URL/activities" \
    -H "$AUTH_A" \
    -H "Content-Type: application/json" \
    -d '{"title":"摄影活动","description":"城市摄影","activityTime":"2026-04-12T14:00:00","location":"西湖边","maxParticipants":8}')
ACTIVITY_ID2=$(echo $CREATE_ACTIVITY2 | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

CREATE_DIARY=$(curl -s -X POST "$BASE_URL/diaries" \
    -H "$AUTH_A" \
    -H "Content-Type: application/json" \
    -d "{\"activityId\":$ACTIVITY_ID2,\"content\":\"今天的摄影活动很开心\",\"images\":\"$IMG_URL\"}")
check_response "5.1 创建日记" "$CREATE_DIARY" "0"
DIARY_ID=$(echo $CREATE_DIARY | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo "   日记ID: $DIARY_ID"

DIARIES=$(curl -s -X GET "$BASE_URL/diaries" -H "$AUTH_A")
check_response "5.2 获取日记列表" "$DIARIES" "0"

DIARY_DETAIL=$(curl -s -X GET "$BASE_URL/diaries/$DIARY_ID" -H "$AUTH_A")
check_response "5.3 获取日记详情" "$DIARY_DETAIL" "0"

UPDATE_DIARY=$(curl -s -X PUT "$BASE_URL/diaries/$DIARY_ID" \
    -H "$AUTH_A" \
    -H "Content-Type: application/json" \
    -d '{"content":"更新后的日记内容","images":"updated.jpg"}')
check_response "5.4 更新日记" "$UPDATE_DIARY" "0"

DELETE_DIARY=$(curl -s -X DELETE "$BASE_URL/diaries/$DIARY_ID" -H "$AUTH_A")
check_response "5.5 删除日记" "$DELETE_DIARY" "0"

# ==================== 6. 好友模块 ====================
echo -e "\n[6. 好友模块 - CRUD]"

# 从用户列表中查找用户B的真实ID
USER_B_ID=$(curl -s -X GET "$BASE_URL/users" -H "$AUTH_A" | grep -o "{\"id\":[0-9]*,\"username\":\"${USER_B}\"" | grep -o '"id":[0-9]*' | cut -d':' -f2)

if [ -z "$USER_B_ID" ] || [ "$USER_B_ID" == "" ]; then
    # 如果找不到，尝试从 auth/me 获取
    USER_B_ID=$(curl -s -X GET "$BASE_URL/auth/me" -H "$AUTH_B" | grep -o '"id":[0-9]*' | cut -d':' -f2)
fi

if [ -z "$USER_B_ID" ] || [ "$USER_B_ID" == "" ]; then
    USER_B_ID=2
fi
echo "   用户B ID: $USER_B_ID"

SEND_REQUEST=$(curl -s -X POST "$BASE_URL/friends/requests" \
    -H "$AUTH_A" \
    -H "Content-Type: application/json" \
    -d "{\"targetUserId\":$USER_B_ID}")
check_response "6.1 发送好友申请" "$SEND_REQUEST" "0"

REQUESTS=$(curl -s -X GET "$BASE_URL/friends/requests" -H "$AUTH_B")
check_response "6.2 获取好友申请列表" "$REQUESTS" "0"

# 从 incoming 数组中提取第一个申请的ID
REQUEST_ID=$(echo $REQUESTS | grep -o '"incoming":\[[^]]*\]' | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

if [ -z "$REQUEST_ID" ] || [ "$REQUEST_ID" == "" ]; then
    REQUEST_ID=1
fi
echo "   申请ID: $REQUEST_ID"

GET_REQUEST=$(curl -s -X GET "$BASE_URL/friends/requests/$REQUEST_ID" -H "$AUTH_B")
check_response "6.3 获取申请详情" "$GET_REQUEST" "0"

HANDLE_REQUEST=$(curl -s -X POST "$BASE_URL/friends/requests/$REQUEST_ID/handle" \
    -H "$AUTH_B" \
    -H "Content-Type: application/json" \
    -d '{"accept":true}')
check_response "6.4 接受好友申请" "$HANDLE_REQUEST" "0"

FRIENDS=$(curl -s -X GET "$BASE_URL/friends" -H "$AUTH_A")
check_response "6.5 获取好友列表" "$FRIENDS" "0"

FRIEND_DETAIL=$(curl -s -X GET "$BASE_URL/friends/$USER_B_ID" -H "$AUTH_A")
check_response "6.6 获取好友详情" "$FRIEND_DETAIL" "0"

DELETE_FRIEND=$(curl -s -X DELETE "$BASE_URL/friends/$USER_B_ID" -H "$AUTH_A")
check_response "6.7 删除好友" "$DELETE_FRIEND" "0"

DELETE_REQUEST=$(curl -s -X DELETE "$BASE_URL/friends/requests/$REQUEST_ID" -H "$AUTH_A")
DELETE_CODE=$(echo $DELETE_REQUEST | grep -o '"code":[0-9]*' | cut -d':' -f2)
if [ "$DELETE_CODE" == "0" ] || [ "$DELETE_CODE" == "404" ]; then
    echo "✅ 6.8 删除好友申请 (code: $DELETE_CODE)"
    PASS=$((PASS + 1))
else
    echo "❌ 6.8 删除好友申请 (期望code: 0或404, 实际code: $DELETE_CODE)"
    echo "   响应: $DELETE_REQUEST"
    FAIL=$((FAIL + 1))
fi

# ==================== 7. 聊天模块 ====================
echo -e "\n[7. 聊天模块]"

SEND_MSG=$(curl -s -X POST "$BASE_URL/chat/messages" \
    -H "$AUTH_A" \
    -H "Content-Type: application/json" \
    -d "{\"receiverId\":$USER_B_ID,\"content\":\"你好，很高兴认识你！\"}")
check_response "7.1 发送消息" "$SEND_MSG" "0"

CONVERSATIONS=$(curl -s -X GET "$BASE_URL/chat/conversations" -H "$AUTH_A")
check_response "7.2 获取会话列表" "$CONVERSATIONS" "0"

MESSAGES=$(curl -s -X GET "$BASE_URL/chat/messages?targetUserId=$USER_B_ID&pageNum=1&pageSize=50" -H "$AUTH_A")
check_response "7.3 获取聊天记录" "$MESSAGES" "0"

DELETE_MSG=$(curl -s -X DELETE "$BASE_URL/chat/messages/test-message-id" -H "$AUTH_A")
check_response "7.4 删除消息" "$DELETE_MSG" "0"

# ==================== 8. 评价模块 ====================
echo -e "\n[8. 评价模块 - CRUD]"

CREATE_EVAL=$(curl -s -X POST "$BASE_URL/evaluations" \
    -H "$AUTH_A" \
    -H "Content-Type: application/json" \
    -d "{\"targetId\":$USER_B_ID,\"activityId\":$ACTIVITY_ID2,\"scoreLevel\":3}")
check_response "8.1 创建评价" "$CREATE_EVAL" "0"
EVAL_ID=$(echo $CREATE_EVAL | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo "   评价ID: $EVAL_ID"

EVALS_BY_TARGET=$(curl -s -X GET "$BASE_URL/evaluations/target/$USER_B_ID" -H "$AUTH_A")
check_response "8.2 获取目标用户评价" "$EVALS_BY_TARGET" "0"

EVALS_BY_EVALUATOR=$(curl -s -X GET "$BASE_URL/evaluations/evaluator/1" -H "$AUTH_A")
check_response "8.3 获取评价者评价" "$EVALS_BY_EVALUATOR" "0"

EVAL_DETAIL=$(curl -s -X GET "$BASE_URL/evaluations/$EVAL_ID" -H "$AUTH_A")
check_response "8.4 获取评价详情" "$EVAL_DETAIL" "0"

UPDATE_EVAL=$(curl -s -X PUT "$BASE_URL/evaluations/$EVAL_ID" \
    -H "$AUTH_A" \
    -H "Content-Type: application/json" \
    -d '{"scoreLevel":2,"activityId":'$ACTIVITY_ID2'}')
check_response "8.5 更新评价" "$UPDATE_EVAL" "0"

DELETE_EVAL=$(curl -s -X DELETE "$BASE_URL/evaluations/$EVAL_ID" -H "$AUTH_A")
check_response "8.6 删除评价" "$DELETE_EVAL" "0"

# ==================== 9. 发现模块 ====================
echo -e "\n[9. 发现模块]"

UPDATE_LOC_A=$(curl -s -X POST "$BASE_URL/discover/location" \
    -H "$AUTH_A" \
    -H "Content-Type: application/json" \
    -d '{"longitude":120.15,"latitude":30.25}')
check_response "9.1 用户A更新位置" "$UPDATE_LOC_A" "0"

UPDATE_LOC_B=$(curl -s -X POST "$BASE_URL/discover/location" \
    -H "$AUTH_B" \
    -H "Content-Type: application/json" \
    -d '{"longitude":120.16,"latitude":30.26}')
check_response "9.2 用户B更新位置" "$UPDATE_LOC_B" "0"

NEARBY=$(curl -s -X GET "$BASE_URL/discover/nearby?longitude=120.15&latitude=30.25&radius=10" -H "$AUTH_A")
check_response "9.3 获取附近的人" "$NEARBY" "0"

RANKING=$(curl -s -X GET "$BASE_URL/discover/ranking?limit=10" -H "$AUTH_A")
check_response "9.4 获取用户排行榜" "$RANKING" "0"

RANDOM_USER=$(curl -s -X GET "$BASE_URL/discover/random" -H "$AUTH_A")
check_response "9.5 随机匹配用户" "$RANDOM_USER"

DELETE_LOC=$(curl -s -X DELETE "$BASE_URL/discover/location" -H "$AUTH_A")
check_response "9.6 删除位置信息" "$DELETE_LOC" "0"

# ==================== 10. JWT 权限测试 ====================
echo -e "\n[10. JWT 权限测试]"

NO_AUTH=$(curl -s -X GET "$BASE_URL/users")
check_response "10.1 无Token访问(应失败)" "$NO_AUTH" "401"

INVALID_TOKEN=$(curl -s -X GET "$BASE_URL/users" \
    -H "Authorization: Bearer invalid_token_12345")
check_response "10.2 无效Token访问(应失败)" "$INVALID_TOKEN" "401"

# ==================== 测试总结 ====================
echo -e "\n=========================================================="
echo "   测试完成统计                                              "
echo "=========================================================="
echo "✅ 通过: $PASS"
echo "❌ 失败: $FAIL"
echo "📊 总计: $((PASS + FAIL))"
echo "=========================================================="

if [ $FAIL -eq 0 ]; then
    echo "🎉 所有测试通过！"
    exit 0
else
    echo "⚠️  存在失败的测试，请检查上述输出"
    exit 1
fi
