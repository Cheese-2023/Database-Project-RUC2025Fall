-- 检查缺失的计算数据
-- 找出哪些县域在哪些年份缺少风险评估

SELECT 
    '缺失风险评估的县域-年份组合' as info,
    COUNT(*) as missing_count
FROM (
    SELECT DISTINCT 
        e.county_code,
        e.year
    FROM economic_aggregate e
    LEFT JOIN comprehensive_risk_assessment c 
        ON e.county_code = c.county_code AND e.year = c.year
    WHERE c.county_code IS NULL
    AND e.year >= 2012
) as missing;

-- 查看2012年缺失的县域数量
SELECT 
    '2012年缺失的县域数量' as info,
    COUNT(*) as missing_count
FROM (
    SELECT DISTINCT e.county_code
    FROM economic_aggregate e
    LEFT JOIN comprehensive_risk_assessment c 
        ON e.county_code = c.county_code AND e.year = c.year
    WHERE c.county_code IS NULL
    AND e.year = 2012
) as missing_2012;

-- 查看2013-2023年缺失的县域数量
SELECT 
    '2013-2023年缺失的县域数量' as info,
    COUNT(*) as missing_count
FROM (
    SELECT DISTINCT 
        e.county_code,
        e.year
    FROM economic_aggregate e
    LEFT JOIN comprehensive_risk_assessment c 
        ON e.county_code = c.county_code AND e.year = c.year
    WHERE c.county_code IS NULL
    AND e.year >= 2013
) as missing_2013_2023;

