#!/bin/bash
# 通过API触发风险计算（需要后端服务运行）

echo "=========================================="
echo "触发风险计算"
echo "=========================================="
echo ""

# 检查后端是否运行
if ! curl -s http://localhost:8080/api/risk/statistics > /dev/null 2>&1; then
    echo "错误: 后端服务未运行"
    echo ""
    echo "请先启动后端服务："
    echo "  cd backend"
    echo "  mvn spring-boot:run"
    echo ""
    echo "或者使用 nohup 在后台运行："
    echo "  cd backend"
    echo "  nohup mvn spring-boot:run > ../logs/backend.log 2>&1 &"
    exit 1
fi

echo "后端服务运行正常，开始触发风险计算..."
echo ""

# 调用计算API
response=$(curl -s -X POST "http://localhost:8080/api/risk/indicators/calculate" \
    -H "Content-Type: application/json" \
    -H "role: ADMIN" \
    -w "\nHTTP_CODE:%{http_code}")

http_code=$(echo "$response" | grep "HTTP_CODE" | cut -d: -f2)
body=$(echo "$response" | sed '/HTTP_CODE/d')

if [ "$http_code" = "200" ]; then
    echo "✓ 风险计算已触发！"
    echo ""
    echo "注意: 计算是异步执行的，可能需要较长时间（30-60分钟）"
    echo "请查看后端日志查看计算进度:"
    echo "  tail -f backend/logs/application.log"
    echo ""
    echo "或者查看风险计算专用日志:"
    echo "  tail -f backend/logs/risk-calculation.log"
else
    echo "✗ 触发失败 (HTTP $http_code)"
    echo "响应: $body"
fi

echo ""
echo "=========================================="

