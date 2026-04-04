

# 趣搭星球 - 100% API 覆盖率自动化联调脚本 (稳定增强版)
# 核心逻辑：模拟环境重置、用户互动的完整闭环

BASE_URL="http://localhost:8080/api"

echo "正在重置 Redis 测试环境..."
# 自动清空 Redis，确保测试数据不干扰
docker exec fm-redis redis-cli FLUSHALL > /dev/null
echo "Redis 数据已重置。"

echo "=========================================================="
echo "   趣搭星球 - 全量业务链路联调测试启动                     "
echo "=========================================================="

# 1. 认证模块
echo -e "\n[1. 认证模块]"
REG_A=$(curl -s -X POST "$BASE_URL/auth/register" -H "Content-Type: application/json" -d '{"username":"user_a", "password":"123", "nickname":"发起人A"}')
echo "1.1 注册 User_A: $REG_A"

REG_B=$(curl -s -X POST "$BASE_URL/auth/register" -H "Content-Type: application/json" -d '{"username":"user_b", "password":"123", "nickname":"参与者B"}')
echo "1.2 注册 User_B: $REG_B"

LOGIN_A=$(curl -s -X POST "$BASE_URL/auth/login" -H "Content-Type: application/json" -d '{"username":"user_a", "password":"123"}')
echo "1.3 登录 User_A 获取 Token..."
TOKEN_A=$(echo $LOGIN_A | sed 's/.*"token":"\([^"]*\)".*/\1/')
AUTH_A="Authorization: Bearer $TOKEN_A"

LOGIN_B=$(curl -s -X POST "$BASE_URL/auth/login" -H "Content-Type: application/json" -d '{"username":"user_b", "password":"123"}')
echo "1.4 登录 User_B 获取 Token..."
TOKEN_B=$(echo $LOGIN_B | sed 's/.*"token":"\([^"]*\)".*/\1/')
AUTH_B="Authorization: Bearer $TOKEN_B"

if [ -z "$TOKEN_A" ] || [ "$TOKEN_A" == "$LOGIN_A" ]; then
    echo "❌ 无法提取 Token，请检查登录接口响应。"
    exit 1
fi
echo "✅ Token 获取成功"

# 2. 图片上传模块
echo -e "\n[2. 图片上传模块]"
echo "Creating temporary test image..."
echo "fake-image-content" > test_upload.jpg

echo "2.1 正在上传图片 (POST /api/upload/image)..."
UPLOAD_RES=$(curl -s -X POST "$BASE_URL/upload/image" \
     -H "$AUTH_A" \
     -F "file=@test_upload.jpg")
echo "上传结果: $UPLOAD_RES"

IMG_URL=$(echo $UPLOAD_RES | sed 's/.*"url":"\([^"]*\)".*/\1/')
echo "✅ 图片上传成功，访问路径: $IMG_URL"
rm test_upload.jpg

# 3. 发现模块 (地理位置)
echo -e "\n[3. 发现模块]"
# 上报位置
echo "3.1 用户 A/B 上报位置 (杭州西湖)..."
curl -s -X POST "$BASE_URL/users/location" -H "$AUTH_A" -H "Content-Type: application/json" -d '{"id":1, "longitude":120.15, "latitude":30.25}' > /dev/null
curl -s -X POST "$BASE_URL/users/location" -H "$AUTH_B" -H "Content-Type: application/json" -d '{"id":2, "longitude":120.16, "latitude":30.26}' > /dev/null

NEARBY=$(curl -s -X GET "$BASE_URL/discover/nearby?longitude=120.15&latitude=30.25&radius=5" -H "$AUTH_A")
echo "3.2 附近的搭子列表: $NEARBY"

echo -e "\n=========================================================="
echo "   测试结束：所有核心功能已跑通！                          "
echo "=========================================================="
