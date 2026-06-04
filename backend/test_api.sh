#!/bin/bash

# 趣搭星球 - 全量 API 自动化测试脚本 (修复 Gitleaks 版)
BASE_URL="http://localhost:8080/api"
PASS=0
FAIL=0

echo "正在重置 Redis 测试环境..."
docker exec fm-redis redis-cli FLUSHALL > /dev/null
echo "Redis 数据已重置。"

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

# 1. 认证逻辑 (动态获取 Token)
echo -e "\n[1. 认证模块]"
USER_A="user_$(date +%s)"
REG_A=$(curl -s -X POST "$BASE_URL/auth/register" -H "Content-Type: application/json" -d "{\"username\":\"$USER_A\",\"password\":\"123456\"}")
LOGIN_A=$(curl -s -X POST "$BASE_URL/auth/login" -H "Content-Type: application/json" -d "{\"username\":\"$USER_A\",\"password\":\"123456\"}")
TOKEN_A=$(echo $LOGIN_A | sed 's/.*"token":"\([^"]*\)".*/\1/')
AUTH_A="Authorization: Bearer $TOKEN_A"

# 10. JWT 权限测试 (触发 Gitleaks 拦截的点)
echo -e "\n[10. JWT 权限测试]"
# ✅ 安全修复：添加 gitleaks:allow 标记，防止 CI 报错
INVALID_TOKEN=$(curl -s -X GET "$BASE_URL/users/me" \
    -H "Authorization: Bearer invalid_token_12345") # gitleaks:allow
check_response "10.1 无效Token访问(应失败)" "$INVALID_TOKEN" "401"

echo -e "\n=========================================================="
echo "   测试完成统计: ✅ 通过: $PASS | ❌ 失败: $FAIL"
echo "=========================================================="
