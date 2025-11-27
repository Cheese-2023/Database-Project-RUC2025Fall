-- ========================================
-- 县域数据导入与转换脚本
-- 用途：将原始县域统计数据导入到新设计的数据库结构中
-- ========================================

USE county_risk_warning_system;

-- ========================================
-- 1. 创建临时导入表（与原始数据结构一致）
-- ========================================

DROP TABLE IF EXISTS temp_raw_data;
CREATE TABLE temp_raw_data (
    年份 YEAR,
    省份 VARCHAR(100),
    城市 VARCHAR(100),
    区县 VARCHAR(100),
    区县代码 VARCHAR(10),
    行政区域土地面积_平方公里 DECIMAL(10,2),
    乡及镇个数_个 INT,
    乡个数_个 INT,
    镇个数_个 INT,
    街道办事处个数_个 INT,
    村民委员会个数_个 INT,
    年末总户数_户 INT,
    乡村户数_户 INT,
    年末总人口_万人 DECIMAL(8,2),
    乡村人口_万人 DECIMAL(8,2),
    户籍人口数_万人 DECIMAL(8,2),
    年末单位从业人员_人 INT,
    城镇单位在岗职工人数_人 INT,
    乡村从业人员数_人 INT,
    农林牧渔业从业人员数_人 INT,
    年末第二产业单位从业人员_人 INT,
    年末第三产业单位从业人员_人 INT,
    农业机械总动力_万千瓦特 DECIMAL(8,2),
    固定电话用户_户 INT,
    移动电话用户数_户 INT,
    宽带接入用户数_户 INT,
    地区生产总值_万元 BIGINT,
    第一产业增加值_万元 BIGINT,
    第二产业增加值_万元 BIGINT,
    工业增加值_万元 BIGINT,
    第三产业增加值_万元 BIGINT,
    农业增加值_万元 BIGINT,
    牧业增加值_万元 BIGINT,
    人均地区生产总值_元人 DECIMAL(10,2),
    城镇单位在岗职工平均工资_元 DECIMAL(10,2),
    城镇居民人均可支配收入_元 DECIMAL(10,2),
    农村居民人均可支配收入_元 DECIMAL(10,2),
    地方财政一般预算收入_万元 BIGINT,
    各项税收_万元 BIGINT,
    地方财政一般预算支出_万元 BIGINT,
    城乡居民储蓄存款余额_万元 BIGINT,
    年末金融机构各项贷款余额_万元 BIGINT,
    出口额_美元 BIGINT,
    实际利用外资金额_美元 BIGINT,
    农作物总播种面积_千公顷 DECIMAL(8,2),
    常用耕地面积_公顷 INT,
    机收面积_公顷 INT,
    设施农业占地面积_公顷 INT,
    农用机械总动力_千万瓦 DECIMAL(8,2),
    粮食总产量_吨 INT,
    棉花产量_吨 INT,
    油料产量_吨 INT,
    肉类总产量_吨 INT,
    农林牧渔业总产值_万元 BIGINT,
    规模以上工业企业数_个 INT,
    规模以上工业总产值_万元 BIGINT,
    城镇固定资产投资完成额_万元 BIGINT,
    全社会固定资产投资_万元 BIGINT,
    社会消费品零售总额_万元 BIGINT,
    房地产开发投资_亿元 DECIMAL(8,2),
    普通小学学校数_个 INT,
    普通中学学校数_个 INT,
    普通小学专任教师数_人 INT,
    普通中学专任教师数_人 INT,
    普通小学在校生数_人 INT,
    普通中学在校学生数_人 INT,
    中等职业教育学校在校学生数_人 INT,
    医院卫生院床位数_床 INT,
    医院和卫生院卫生人员数_卫生技术人员_人 INT,
    医院和卫生院卫生人员数_执业医师_人 INT,
    各种社会福利收养性单位数_个 INT,
    各种社会福利收养性单位床位数_床 INT,
    全社会用电量_万千瓦时 BIGINT,
    城乡居民生活用电量_万千瓦时 BIGINT,
    废气中氮氧化物排放量_吨 DECIMAL(10,2),
    废气中烟尘排放量_吨 DECIMAL(10,2),
    工业废气中二氧化硫排放量_吨 DECIMAL(10,2),
    艺术表演场馆数_剧场影剧院_个 INT,
    公共图书馆总藏量_千册 DECIMAL(10,2),
    体育场馆机构数_个 INT,
    INDEX idx_county_year (区县代码, 年份)
) ENGINE=InnoDB COMMENT='临时原始数据导入表';

-- ========================================
-- 2. 数据导入脚本说明
-- ========================================
/*
数据导入说明：
1. 将原始CSV/Excel数据导入到 temp_raw_data 表中
2. 执行以下SQL语句可以导入CSV文件（需要调整文件路径）：

LOAD DATA INFILE '/path/to/your/county_data.csv'
INTO TABLE temp_raw_data
FIELDS TERMINATED BY ','
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS;

3. 或者使用MySQL Workbench的数据导入向导
4. 执行下面的数据转换和插入脚本
*/

-- ========================================
-- 3. 数据转换和插入脚本
-- ========================================

-- 3.1 插入县域基础信息
INSERT IGNORE INTO county_basic (
    county_code, county_name, city_name, province_name, 
    land_area_km2, development_level, administrative_level
)
SELECT DISTINCT
    区县代码,
    区县,
    城市,
    省份,
    行政区域土地面积_平方公里,
    CASE 
        WHEN 人均地区生产总值_元人 > 80000 THEN '发达'
        WHEN 人均地区生产总值_元人 > 50000 THEN '中等发达'
        WHEN 人均地区生产总值_元人 > 25000 THEN '欠发达'
        ELSE '贫困'
    END as development_level,
    CASE 
        WHEN 区县 LIKE '%市' THEN '县级市'
        WHEN 区县 LIKE '%区' THEN '区'
        WHEN 区县 LIKE '%自治县' THEN '自治县'
        ELSE '县'
    END as administrative_level
FROM temp_raw_data
WHERE 区县代码 IS NOT NULL AND 区县代码 != '';

-- 3.2 插入行政区划结构数据
INSERT IGNORE INTO administrative_structure (
    county_code, year, townships_total, townships_rural, townships_urban, 
    street_offices, village_committees
)
SELECT 
    区县代码,
    年份,
    乡及镇个数_个,
    乡个数_个,
    镇个数_个,
    街道办事处个数_个,
    村民委员会个数_个
FROM temp_raw_data
WHERE 区县代码 IS NOT NULL;

-- 3.3 插入人口统计数据
INSERT IGNORE INTO population_statistics (
    county_code, year, total_households, rural_households, 
    total_population_万, rural_population_万, registered_population_万
)
SELECT 
    区县代码,
    年份,
    年末总户数_户,
    乡村户数_户,
    年末总人口_万人,
    乡村人口_万人,
    户籍人口数_万人
FROM temp_raw_data
WHERE 区县代码 IS NOT NULL;

-- 3.4 插入就业统计数据
INSERT IGNORE INTO employment_statistics (
    county_code, year, unit_employees_total, urban_employees_total, 
    rural_employees_total, agriculture_employees, secondary_industry_employees, 
    tertiary_industry_employees
)
SELECT 
    区县代码,
    年份,
    年末单位从业人员_人,
    城镇单位在岗职工人数_人,
    乡村从业人员数_人,
    农林牧渔业从业人员数_人,
    年末第二产业单位从业人员_人,
    年末第三产业单位从业人员_人
FROM temp_raw_data
WHERE 区县代码 IS NOT NULL;

-- 计算就业率（需要人口数据支撑）
UPDATE employment_statistics es
JOIN population_statistics ps ON es.county_code = ps.county_code AND es.year = ps.year
SET es.employment_rate = CASE 
    WHEN ps.total_population_万 > 0 THEN 
        (es.total_employment_万 / (ps.total_population_万 * 0.65)) * 100  -- 假设65%为劳动年龄人口比例
    ELSE NULL
END
WHERE es.employment_rate IS NULL;

-- 3.5 插入经济总量数据
INSERT IGNORE INTO economic_aggregate (
    county_code, year, gdp_万元, primary_industry_万元, secondary_industry_万元,
    industrial_value_万元, tertiary_industry_万元, agriculture_value_万元,
    livestock_value_万元, gdp_per_capita
)
SELECT 
    区县代码,
    年份,
    地区生产总值_万元,
    第一产业增加值_万元,
    第二产业增加值_万元,
    工业增加值_万元,
    第三产业增加值_万元,
    农业增加值_万元,
    牧业增加值_万元,
    人均地区生产总值_元人
FROM temp_raw_data
WHERE 区县代码 IS NOT NULL;

-- 计算GDP增长率
UPDATE economic_aggregate ea1
JOIN economic_aggregate ea2 ON ea1.county_code = ea2.county_code AND ea1.year = ea2.year + 1
SET ea1.gdp_growth_rate = CASE 
    WHEN ea2.gdp_万元 > 0 THEN 
        ((ea1.gdp_万元 - ea2.gdp_万元) / ea2.gdp_万元) * 100
    ELSE NULL
END
WHERE ea1.gdp_growth_rate IS NULL;

-- 3.6 插入财政金融数据
INSERT IGNORE INTO fiscal_finance (
    county_code, year, fiscal_revenue_万元, tax_revenue_万元, 
    fiscal_expenditure_万元, savings_balance_万元, loan_balance_万元
)
SELECT 
    区县代码,
    年份,
    地方财政一般预算收入_万元,
    各项税收_万元,
    地方财政一般预算支出_万元,
    城乡居民储蓄存款余额_万元,
    年末金融机构各项贷款余额_万元
FROM temp_raw_data
WHERE 区县代码 IS NOT NULL;

-- 3.7 插入农业生产数据
INSERT IGNORE INTO agriculture_production (
    county_code, year, crop_area_千公顷, cultivated_area_公顷, 
    mechanized_area_公顷, facility_agriculture_公顷, agricultural_machinery_万千瓦,
    grain_production_吨, cotton_production_吨, oil_production_吨, 
    meat_production_吨, agriculture_output_万元
)
SELECT 
    区县代码,
    年份,
    农作物总播种面积_千公顷,
    常用耕地面积_公顷,
    机收面积_公顷,
    设施农业占地面积_公顷,
    农用机械总动力_千万瓦 * 10,  -- 转换为万千瓦
    粮食总产量_吨,
    棉花产量_吨,
    油料产量_吨,
    肉类总产量_吨,
    农林牧渔业总产值_万元
FROM temp_raw_data
WHERE 区县代码 IS NOT NULL;

-- 3.8 插入工业发展数据
INSERT IGNORE INTO industry_development (
    county_code, year, large_industrial_enterprises, industrial_output_万元,
    export_amount_美元, foreign_investment_美元
)
SELECT 
    区县代码,
    年份,
    规模以上工业企业数_个,
    规模以上工业总产值_万元,
    出口额_美元,
    实际利用外资金额_美元
FROM temp_raw_data
WHERE 区县代码 IS NOT NULL;

-- 3.9 插入基础设施数据
INSERT IGNORE INTO infrastructure (
    county_code, year, fixed_phones, mobile_phones, broadband_users,
    power_consumption_万千瓦时, residential_power_万千瓦时
)
SELECT 
    区县代码,
    年份,
    固定电话用户_户,
    移动电话用户数_户,
    宽带接入用户数_户,
    全社会用电量_万千瓦时,
    城乡居民生活用电量_万千瓦时
FROM temp_raw_data
WHERE 区县代码 IS NOT NULL;

-- 3.10 插入投资消费数据
INSERT IGNORE INTO investment_consumption (
    county_code, year, urban_fixed_investment_万元, total_fixed_investment_万元,
    retail_sales_万元, real_estate_investment_亿元
)
SELECT 
    区县代码,
    年份,
    城镇固定资产投资完成额_万元,
    全社会固定资产投资_万元,
    社会消费品零售总额_万元,
    房地产开发投资_亿元
FROM temp_raw_data
WHERE 区县代码 IS NOT NULL;

-- 3.11 插入教育卫生数据
INSERT IGNORE INTO education_health (
    county_code, year, primary_schools, middle_schools, primary_teachers,
    middle_teachers, primary_students, middle_students, vocational_students,
    hospital_beds, health_workers, doctors, welfare_institutions, welfare_beds
)
SELECT 
    区县代码,
    年份,
    普通小学学校数_个,
    普通中学学校数_个,
    普通小学专任教师数_人,
    普通中学专任教师数_人,
    普通小学在校生数_人,
    普通中学在校学生数_人,
    中等职业教育学校在校学生数_人,
    医院卫生院床位数_床,
    医院和卫生院卫生人员数_卫生技术人员_人,
    医院和卫生院卫生人员数_执业医师_人,
    各种社会福利收养性单位数_个,
    各种社会福利收养性单位床位数_床
FROM temp_raw_data
WHERE 区县代码 IS NOT NULL;

-- 3.12 插入收入与生活水平数据
INSERT IGNORE INTO income_living_standard (
    county_code, year, urban_average_wage, urban_disposable_income, rural_disposable_income
)
SELECT 
    区县代码,
    年份,
    城镇单位在岗职工平均工资_元,
    城镇居民人均可支配收入_元,
    农村居民人均可支配收入_元
FROM temp_raw_data
WHERE 区县代码 IS NOT NULL;

-- 3.13 插入环境与文化数据
INSERT IGNORE INTO environment_culture (
    county_code, year, nox_emission_吨, dust_emission_吨, so2_emission_吨,
    theaters_count, library_collection_千册, sports_venues
)
SELECT 
    区县代码,
    年份,
    废气中氮氧化物排放量_吨,
    废气中烟尘排放量_吨,
    工业废气中二氧化硫排放量_吨,
    艺术表演场馆数_剧场影剧院_个,
    公共图书馆总藏量_千册,
    体育场馆机构数_个
FROM temp_raw_data
WHERE 区县代码 IS NOT NULL;

-- ========================================
-- 4. 数据质量检查和修复
-- ========================================

-- 4.1 检查数据完整性
INSERT INTO data_quality_checks (table_name, county_code, year, check_type, issue_description, severity)
SELECT 
    'population_statistics' as table_name,
    county_code,
    year,
    '完整性' as check_type,
    '人口数据缺失' as issue_description,
    '严重' as severity
FROM county_basic cb
WHERE NOT EXISTS (
    SELECT 1 FROM population_statistics ps 
    WHERE ps.county_code = cb.county_code 
    AND ps.year BETWEEN 2000 AND 2023
);

-- 4.2 检查数据一致性
INSERT INTO data_quality_checks (table_name, county_code, year, check_type, issue_description, severity)
SELECT 
    'population_statistics' as table_name,
    ps.county_code,
    ps.year,
    '一致性' as check_type,
    CONCAT('乡村人口(', ps.rural_population_万, ')超过总人口(', ps.total_population_万, ')') as issue_description,
    '严重' as severity
FROM population_statistics ps
WHERE ps.rural_population_万 > ps.total_population_万;

-- 4.3 检查异常值
INSERT INTO data_quality_checks (table_name, county_code, year, check_type, issue_description, severity)
SELECT 
    'economic_aggregate' as table_name,
    ea.county_code,
    ea.year,
    '准确性' as check_type,
    CONCAT('人均GDP异常: ', ea.gdp_per_capita, '元') as issue_description,
    CASE 
        WHEN ea.gdp_per_capita < 1000 OR ea.gdp_per_capita > 1000000 THEN '严重'
        WHEN ea.gdp_per_capita < 5000 OR ea.gdp_per_capita > 500000 THEN '中等'
        ELSE '轻微'
    END as severity
FROM economic_aggregate ea
WHERE ea.gdp_per_capita < 1000 OR ea.gdp_per_capita > 1000000;

-- ========================================
-- 5. 初始风险评估数据生成
-- ========================================

-- 5.1 创建风险评估存储过程
DELIMITER //
CREATE PROCEDURE InitialRiskAssessment()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE county_code_var VARCHAR(10);
    DECLARE year_var YEAR;
    
    DECLARE data_cursor CURSOR FOR 
        SELECT DISTINCT county_code, year 
        FROM economic_aggregate 
        WHERE year >= 2020  -- 只对近期数据进行初始评估
        ORDER BY county_code, year;
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    OPEN data_cursor;
    
    assessment_loop: LOOP
        FETCH data_cursor INTO county_code_var, year_var;
        IF done THEN
            LEAVE assessment_loop;
        END IF;
        
        -- 插入基础风险评估数据
        INSERT IGNORE INTO comprehensive_risk_assessment (
            county_code, year, 
            economic_risk_score, social_risk_score, environment_risk_score,
            governance_risk_score, development_risk_score, comprehensive_risk_score,
            risk_level, risk_trend
        )
        SELECT 
            county_code_var,
            year_var,
            -- 经济风险评估（基于GDP增长率、人均GDP、财政状况）
            LEAST(1.0, GREATEST(0.0, 
                CASE 
                    WHEN ea.gdp_growth_rate < -5 THEN 0.9
                    WHEN ea.gdp_growth_rate < 0 THEN 0.7
                    WHEN ea.gdp_growth_rate < 3 THEN 0.5
                    WHEN ea.gdp_growth_rate < 6 THEN 0.3
                    ELSE 0.1
                END * 0.4 +
                CASE 
                    WHEN ea.gdp_per_capita < 20000 THEN 0.9
                    WHEN ea.gdp_per_capita < 35000 THEN 0.7
                    WHEN ea.gdp_per_capita < 50000 THEN 0.5
                    WHEN ea.gdp_per_capita < 70000 THEN 0.3
                    ELSE 0.1
                END * 0.3 +
                CASE 
                    WHEN ff.fiscal_self_sufficiency < 20 THEN 0.9
                    WHEN ff.fiscal_self_sufficiency < 40 THEN 0.7
                    WHEN ff.fiscal_self_sufficiency < 60 THEN 0.5
                    WHEN ff.fiscal_self_sufficiency < 80 THEN 0.3
                    ELSE 0.1
                END * 0.3
            )) as economic_risk,
            
            -- 社会风险评估（基于人口变化、就业状况、收入差距）
            LEAST(1.0, GREATEST(0.0,
                CASE 
                    WHEN ps.urbanization_rate < 30 THEN 0.7
                    WHEN ps.urbanization_rate > 80 THEN 0.5
                    ELSE 0.2
                END * 0.3 +
                CASE 
                    WHEN es.employment_rate < 85 THEN 0.8
                    WHEN es.employment_rate < 90 THEN 0.6
                    WHEN es.employment_rate < 95 THEN 0.4
                    ELSE 0.2
                END * 0.4 +
                CASE 
                    WHEN ils.income_gap_ratio > 3 THEN 0.7
                    WHEN ils.income_gap_ratio > 2.5 THEN 0.5
                    WHEN ils.income_gap_ratio > 2 THEN 0.3
                    ELSE 0.1
                END * 0.3
            )) as social_risk,
            
            -- 环境风险评估（基于污染排放）
            LEAST(1.0, GREATEST(0.0,
                CASE 
                    WHEN (ec.nox_emission_吨 + ec.so2_emission_吨 + ec.dust_emission_吨) / cb.land_area_km2 > 10 THEN 0.9
                    WHEN (ec.nox_emission_吨 + ec.so2_emission_吨 + ec.dust_emission_吨) / cb.land_area_km2 > 5 THEN 0.7
                    WHEN (ec.nox_emission_吨 + ec.so2_emission_吨 + ec.dust_emission_吨) / cb.land_area_km2 > 2 THEN 0.5
                    WHEN (ec.nox_emission_吨 + ec.so2_emission_吨 + ec.dust_emission_吨) / cb.land_area_km2 > 1 THEN 0.3
                    ELSE 0.1
                END
            )) as environment_risk,
            
            -- 治理风险（基于公共服务水平）
            LEAST(1.0, GREATEST(0.0,
                CASE 
                    WHEN eh.beds_per_1000_people < 3 THEN 0.7
                    WHEN eh.beds_per_1000_people < 5 THEN 0.5
                    WHEN eh.beds_per_1000_people < 7 THEN 0.3
                    ELSE 0.1
                END * 0.5 +
                CASE 
                    WHEN eh.student_teacher_ratio_primary > 25 THEN 0.7
                    WHEN eh.student_teacher_ratio_primary > 20 THEN 0.5
                    WHEN eh.student_teacher_ratio_primary > 15 THEN 0.3
                    ELSE 0.1
                END * 0.5
            )) as governance_risk,
            
            -- 发展风险（基于投资和消费）
            LEAST(1.0, GREATEST(0.0,
                CASE 
                    WHEN ic.investment_efficiency < 0.5 THEN 0.7
                    WHEN ic.investment_efficiency < 1 THEN 0.5
                    WHEN ic.investment_efficiency < 1.5 THEN 0.3
                    ELSE 0.1
                END
            )) as development_risk,
            
            -- 综合风险得分（加权平均）
            0.5 as comprehensive_risk,  -- 临时值，后续计算
            
            '中风险' as risk_level,
            '稳定' as risk_trend
            
        FROM county_basic cb
        LEFT JOIN economic_aggregate ea ON cb.county_code = ea.county_code AND ea.year = year_var
        LEFT JOIN fiscal_finance ff ON cb.county_code = ff.county_code AND ff.year = year_var
        LEFT JOIN population_statistics ps ON cb.county_code = ps.county_code AND ps.year = year_var
        LEFT JOIN employment_statistics es ON cb.county_code = es.county_code AND es.year = year_var
        LEFT JOIN income_living_standard ils ON cb.county_code = ils.county_code AND ils.year = year_var
        LEFT JOIN environment_culture ec ON cb.county_code = ec.county_code AND ec.year = year_var
        LEFT JOIN education_health eh ON cb.county_code = eh.county_code AND eh.year = year_var
        LEFT JOIN investment_consumption ic ON cb.county_code = ic.county_code AND ic.year = year_var
        WHERE cb.county_code = county_code_var;
        
    END LOOP;
    
    CLOSE data_cursor;
    
    -- 计算综合风险得分
    UPDATE comprehensive_risk_assessment
    SET comprehensive_risk_score = (
        COALESCE(economic_risk_score, 0.5) * 0.35 +
        COALESCE(social_risk_score, 0.5) * 0.25 +
        COALESCE(environment_risk_score, 0.5) * 0.15 +
        COALESCE(governance_risk_score, 0.5) * 0.15 +
        COALESCE(development_risk_score, 0.5) * 0.10
    );
    
    -- 更新风险等级
    UPDATE comprehensive_risk_assessment
    SET risk_level = CASE 
        WHEN comprehensive_risk_score >= 0.8 THEN '高风险'
        WHEN comprehensive_risk_score >= 0.6 THEN '中高风险'
        WHEN comprehensive_risk_score >= 0.4 THEN '中风险'
        WHEN comprehensive_risk_score >= 0.2 THEN '中低风险'
        ELSE '低风险'
    END;
    
END //
DELIMITER ;

-- ========================================
-- 6. 数据导入验证和统计
-- ========================================

-- 6.1 创建数据统计视图
CREATE OR REPLACE VIEW data_import_summary AS
SELECT 
    '县域基础信息' as table_name,
    COUNT(*) as record_count,
    COUNT(DISTINCT county_code) as county_count,
    NULL as year_range
FROM county_basic
UNION ALL
SELECT 
    '人口统计',
    COUNT(*),
    COUNT(DISTINCT county_code),
    CONCAT(MIN(year), '-', MAX(year))
FROM population_statistics
UNION ALL
SELECT 
    '经济总量',
    COUNT(*),
    COUNT(DISTINCT county_code),
    CONCAT(MIN(year), '-', MAX(year))
FROM economic_aggregate
UNION ALL
SELECT 
    '财政金融',
    COUNT(*),
    COUNT(DISTINCT county_code),
    CONCAT(MIN(year), '-', MAX(year))
FROM fiscal_finance
UNION ALL
SELECT 
    '农业生产',
    COUNT(*),
    COUNT(DISTINCT county_code),
    CONCAT(MIN(year), '-', MAX(year))
FROM agriculture_production
UNION ALL
SELECT 
    '工业发展',
    COUNT(*),
    COUNT(DISTINCT county_code),
    CONCAT(MIN(year), '-', MAX(year))
FROM industry_development
UNION ALL
SELECT 
    '教育卫生',
    COUNT(*),
    COUNT(DISTINCT county_code),
    CONCAT(MIN(year), '-', MAX(year))
FROM education_health
UNION ALL
SELECT 
    '环境文化',
    COUNT(*),
    COUNT(DISTINCT county_code),
    CONCAT(MIN(year), '-', MAX(year))
FROM environment_culture;

-- ========================================
-- 7. 执行脚本说明
-- ========================================

/*
执行步骤：
1. 先将原始数据导入到 temp_raw_data 表中
2. 执行上述数据转换脚本
3. 运行数据质量检查
4. 执行初始风险评估：CALL InitialRiskAssessment();
5. 查看导入统计：SELECT * FROM data_import_summary;
6. 清理临时表：DROP TABLE temp_raw_data;

注意事项：
- 确保原始数据格式正确，字段顺序与 temp_raw_data 表一致
- 检查字符编码问题，建议使用 UTF-8
- 大数据量导入时注意调整 MySQL 的相关配置参数
- 定期备份数据库，避免数据丢失
*/

SELECT 'Data import script ready!' as message;
