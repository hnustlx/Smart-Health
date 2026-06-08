#!/bin/bash
# 批量导入健康知识到 Chroma 知识库
# 用法: ./scripts/seed_knowledge.sh [admin_username] [admin_password]
# 默认: admin admin123

set -e

BASE_URL="${API_BASE_URL:-http://localhost:8080}"
ADMIN_USER="${1:-admin}"
ADMIN_PASS="${2:-admin123}"
DATA_FILE="$(dirname "$0")/knowledge.jsonl"

echo "=== 智能健康 知识库导入工具 ==="
echo "后端地址: $BASE_URL"
echo "数据文件: $DATA_FILE"
echo ""

# 1. 管理员登录
echo ">>> 管理员登录..."
LOGIN_RESP=$(curl -s -X POST "$BASE_URL/api/v1/user/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$ADMIN_USER\",\"password\":\"$ADMIN_PASS\"}")

TOKEN=$(echo "$LOGIN_RESP" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('token',''))" 2>/dev/null)

if [ -z "$TOKEN" ]; then
  echo "登录失败: $LOGIN_RESP"
  exit 1
fi
echo "登录成功 ✓"

# 2. 统计总条数
TOTAL=$(wc -l < "$DATA_FILE")
echo "总知识条数: $TOTAL"

# 3. 批量导入
echo ""
echo ">>> 开始导入..."

SUCCESS=0
FAIL=0
LINE_NUM=0

while IFS= read -r line; do
  LINE_NUM=$((LINE_NUM + 1))

  # 跳过空行
  [ -z "$line" ] && continue

  # 解析 JSONL
  DOCUMENT=$(echo "$line" | python3 -c "
import sys, json
d = json.loads(sys.stdin.readline())
body = {
    'document': d['document'],
    'metadata': {
        'title': d['title'],
        'category': d['category'],
        'level': d['level'],
        'status': 'enabled',
        'keywords': ''
    }
}
print(json.dumps(body, ensure_ascii=False))
" 2>/dev/null)

  if [ -z "$DOCUMENT" ]; then
    echo "  第 $LINE_NUM 行: 解析失败，跳过"
    FAIL=$((FAIL + 1))
    continue
  fi

  # POST
  RESP=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/api/v1/admin/knowledge" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d "$DOCUMENT")

  if [ "$RESP" = "200" ]; then
    SUCCESS=$((SUCCESS + 1))
  else
    FAIL=$((FAIL + 1))
    echo "  第 $LINE_NUM 行: HTTP $RESP"
  fi

  # 每 50 行打印一次进度
  if [ $((LINE_NUM % 50)) -eq 0 ]; then
    echo "  进度: $LINE_NUM/$TOTAL (成功: $SUCCESS, 失败: $FAIL)"
  fi

  # 小幅延时避免压垮后端
  sleep 0.05
done < "$DATA_FILE"

echo ""
echo "=== 导入完成 ==="
echo "  总计: $TOTAL"
echo "  成功: $SUCCESS"
echo "  失败: $FAIL"

# 4. 验证
echo ""
echo ">>> 验证知识库数量..."
COUNT=$(curl -s "$BASE_URL/api/v1/admin/knowledge/list" \
  -H "Authorization: Bearer $TOKEN" | python3 -c "
import sys, json
data = json.load(sys.stdin).get('data', [])
print(len(data))
" 2>/dev/null)

echo "  知识库当前总量: $COUNT"
