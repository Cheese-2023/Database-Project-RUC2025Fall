#!/bin/bash
# 直接运行风险计算（不启动Web服务）

echo "=========================================="
echo "直接运行风险计算"
echo "=========================================="
echo ""

cd "$(dirname "$0")/../backend" || exit 1

echo "正在编译项目..."
mvn clean compile -q

if [ $? -ne 0 ]; then
    echo "✗ 编译失败，请检查错误信息"
    exit 1
fi

echo "✓ 编译成功"
echo ""
echo "开始运行风险计算..."
echo "注意: 这可能需要较长时间（几分钟到几十分钟）"
echo "请查看日志了解计算进度"
echo ""

# 运行计算（需要先确保后端依赖已安装）
echo "正在启动风险计算..."
java -cp "target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" com.county.risk.RiskCalculationMain 2>&1 | tee ../logs/risk_calculation_$(date +%Y%m%d_%H%M%S).log

echo ""
echo "=========================================="
echo "计算完成！"
echo "=========================================="

