#!/bin/bash
# 检查风险计算状态脚本

echo "=========================================="
echo "检查风险计算状态"
echo "=========================================="

mysql -u root -p20050210Wbz -D county_risk_warning_system <<EOF
SELECT 
    '基础数据年份范围' as type,
    MIN(year) as min_year,
    MAX(year) as max_year,
    COUNT(DISTINCT year) as year_count
FROM economic_aggregate
UNION ALL
SELECT 
    '风险评估年份范围' as type,
    MIN(year) as min_year,
    MAX(year) as max_year,
    COUNT(DISTINCT year) as year_count
FROM comprehensive_risk_assessment;

SELECT 
    '缺失的年份' as info,
    GROUP_CONCAT(year ORDER BY year) as missing_years
FROM (
    SELECT DISTINCT year 
    FROM economic_aggregate 
    WHERE year NOT IN (SELECT DISTINCT year FROM comprehensive_risk_assessment)
    ORDER BY year
) as missing;
EOF

echo ""
echo "=========================================="

