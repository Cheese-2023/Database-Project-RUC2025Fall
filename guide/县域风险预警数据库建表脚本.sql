-- ========================================
-- 县域风险预警与可视化决策系统数据库脚本
-- ========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS county_risk_warning_system 
DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

USE county_risk_warning_system;

-- ========================================
-- 1. 基础信息表
-- ========================================

-- 1.1 县域基础信息表
CREATE TABLE county_basic (
    county_code VARCHAR(10) PRIMARY KEY COMMENT '县代码',
    county_name VARCHAR(100) NOT NULL COMMENT '县名称',
    city_name VARCHAR(100) COMMENT '地级市名称',
    province_name VARCHAR(100) COMMENT '省份名称',
    region_code VARCHAR(10) COMMENT '区域划分代码（东部、中部、西部等）',
    development_level ENUM('发达', '中等发达', '欠发达', '贫困') DEFAULT '中等发达' COMMENT '发展水平',
    land_area_km2 DECIMAL(10,2) COMMENT '行政区域土地面积(平方公里)',
    administrative_level ENUM('县级市', '县', '区', '自治县') COMMENT '行政级别',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_province (province_name),
    INDEX idx_city (city_name),
    INDEX idx_development (development_level)
) ENGINE=InnoDB COMMENT='县域基础信息表';

-- 1.2 行政区划结构表
CREATE TABLE administrative_structure (
    id INT AUTO_INCREMENT PRIMARY KEY,
    county_code VARCHAR(10) NOT NULL,
    year YEAR NOT NULL,
    townships_total INT COMMENT '乡及镇个数',
    townships_rural INT COMMENT '乡个数',
    townships_urban INT COMMENT '镇个数',
    street_offices INT COMMENT '街道办事处个数',
    village_committees INT COMMENT '村民委员会个数',
    FOREIGN KEY (county_code) REFERENCES county_basic(county_code) ON DELETE CASCADE,
    UNIQUE KEY uk_county_year (county_code, year),
    INDEX idx_year (year)
) ENGINE=InnoDB COMMENT='行政区划结构表';

-- ========================================
-- 2. 人口统计表
-- ========================================

CREATE TABLE population_statistics (
    id INT AUTO_INCREMENT PRIMARY KEY,
    county_code VARCHAR(10) NOT NULL,
    year YEAR NOT NULL,
    total_households INT COMMENT '年末总户数',
    rural_households INT COMMENT '乡村户数',
    total_population_万 DECIMAL(8,2) COMMENT '年末总人口(万人)',
    rural_population_万 DECIMAL(8,2) COMMENT '乡村人口(万人)',
    registered_population_万 DECIMAL(8,2) COMMENT '户籍人口数(万人)',
    -- 计算字段
    population_density DECIMAL(8,2) GENERATED ALWAYS AS (
        CASE 
            WHEN EXISTS(SELECT 1 FROM county_basic cb WHERE cb.county_code = county_code AND cb.land_area_km2 > 0)
            THEN (total_population_万 * 10000) / (SELECT land_area_km2 FROM county_basic WHERE county_code = population_statistics.county_code)
            ELSE NULL
        END
    ) STORED COMMENT '人口密度(人/平方公里)',
    urbanization_rate DECIMAL(5,2) GENERATED ALWAYS AS (
        CASE 
            WHEN total_population_万 > 0 
            THEN ((total_population_万 - rural_population_万) / total_population_万) * 100
            ELSE NULL
        END
    ) STORED COMMENT '城镇化率(%)',
    FOREIGN KEY (county_code) REFERENCES county_basic(county_code) ON DELETE CASCADE,
    UNIQUE KEY uk_county_year (county_code, year),
    INDEX idx_year (year),
    INDEX idx_population (total_population_万)
) ENGINE=InnoDB COMMENT='人口统计表';

-- ========================================
-- 3. 就业统计表
-- ========================================

CREATE TABLE employment_statistics (
    id INT AUTO_INCREMENT PRIMARY KEY,
    county_code VARCHAR(10) NOT NULL,
    year YEAR NOT NULL,
    unit_employees_total INT COMMENT '年末单位从业人员总数',
    urban_employees_total INT COMMENT '城镇单位在岗职工人数',
    rural_employees_total INT COMMENT '乡村从业人员数',
    agriculture_employees INT COMMENT '农林牧渔业从业人员数',
    secondary_industry_employees INT COMMENT '第二产业单位从业人员',
    tertiary_industry_employees INT COMMENT '第三产业单位从业人员',
    -- 计算字段
    total_employment_万 DECIMAL(8,2) GENERATED ALWAYS AS (
        (COALESCE(agriculture_employees, 0) + COALESCE(secondary_industry_employees, 0) + COALESCE(tertiary_industry_employees, 0)) / 10000
    ) STORED COMMENT '总就业人数(万人)',
    employment_rate DECIMAL(5,2) COMMENT '就业率(%)',
    FOREIGN KEY (county_code) REFERENCES county_basic(county_code) ON DELETE CASCADE,
    UNIQUE KEY uk_county_year (county_code, year),
    INDEX idx_year (year)
) ENGINE=InnoDB COMMENT='就业统计表';

-- ========================================
-- 4. 经济总量表
-- ========================================

CREATE TABLE economic_aggregate (
    id INT AUTO_INCREMENT PRIMARY KEY,
    county_code VARCHAR(10) NOT NULL,
    year YEAR NOT NULL,
    gdp_万元 BIGINT COMMENT '地区生产总值(万元)',
    primary_industry_万元 BIGINT COMMENT '第一产业增加值',
    secondary_industry_万元 BIGINT COMMENT '第二产业增加值',
    industrial_value_万元 BIGINT COMMENT '工业增加值',
    tertiary_industry_万元 BIGINT COMMENT '第三产业增加值',
    agriculture_value_万元 BIGINT COMMENT '农业增加值',
    livestock_value_万元 BIGINT COMMENT '牧业增加值',
    gdp_per_capita DECIMAL(10,2) COMMENT '人均GDP(元/人)',
    -- 计算字段
    gdp_growth_rate DECIMAL(5,2) COMMENT 'GDP增长率(%)',
    industry_structure_1 DECIMAL(5,2) GENERATED ALWAYS AS (
        CASE WHEN gdp_万元 > 0 THEN (primary_industry_万元 / gdp_万元) * 100 ELSE NULL END
    ) STORED COMMENT '第一产业比重(%)',
    industry_structure_2 DECIMAL(5,2) GENERATED ALWAYS AS (
        CASE WHEN gdp_万元 > 0 THEN (secondary_industry_万元 / gdp_万元) * 100 ELSE NULL END
    ) STORED COMMENT '第二产业比重(%)',
    industry_structure_3 DECIMAL(5,2) GENERATED ALWAYS AS (
        CASE WHEN gdp_万元 > 0 THEN (tertiary_industry_万元 / gdp_万元) * 100 ELSE NULL END
    ) STORED COMMENT '第三产业比重(%)',
    FOREIGN KEY (county_code) REFERENCES county_basic(county_code) ON DELETE CASCADE,
    UNIQUE KEY uk_county_year (county_code, year),
    INDEX idx_year (year),
    INDEX idx_gdp (gdp_万元)
) ENGINE=InnoDB COMMENT='经济总量表';

-- ========================================
-- 5. 财政金融表
-- ========================================

CREATE TABLE fiscal_finance (
    id INT AUTO_INCREMENT PRIMARY KEY,
    county_code VARCHAR(10) NOT NULL,
    year YEAR NOT NULL,
    fiscal_revenue_万元 BIGINT COMMENT '地方财政一般预算收入',
    tax_revenue_万元 BIGINT COMMENT '各项税收',
    fiscal_expenditure_万元 BIGINT COMMENT '地方财政一般预算支出',
    savings_balance_万元 BIGINT COMMENT '城乡居民储蓄存款余额',
    loan_balance_万元 BIGINT COMMENT '年末金融机构各项贷款余额',
    -- 计算字段
    fiscal_self_sufficiency DECIMAL(5,2) GENERATED ALWAYS AS (
        CASE WHEN fiscal_expenditure_万元 > 0 THEN (fiscal_revenue_万元 / fiscal_expenditure_万元) * 100 ELSE NULL END
    ) STORED COMMENT '财政自给率(%)',
    debt_to_revenue_ratio DECIMAL(5,2) COMMENT '债务收入比(%)',
    FOREIGN KEY (county_code) REFERENCES county_basic(county_code) ON DELETE CASCADE,
    UNIQUE KEY uk_county_year (county_code, year),
    INDEX idx_year (year)
) ENGINE=InnoDB COMMENT='财政金融表';

-- ========================================
-- 6. 农业生产表
-- ========================================

CREATE TABLE agriculture_production (
    id INT AUTO_INCREMENT PRIMARY KEY,
    county_code VARCHAR(10) NOT NULL,
    year YEAR NOT NULL,
    crop_area_千公顷 DECIMAL(8,2) COMMENT '农作物总播种面积',
    cultivated_area_公顷 INT COMMENT '常用耕地面积',
    mechanized_area_公顷 INT COMMENT '机收面积',
    facility_agriculture_公顷 INT COMMENT '设施农业占地面积',
    agricultural_machinery_万千瓦 DECIMAL(8,2) COMMENT '农用机械总动力',
    grain_production_吨 INT COMMENT '粮食总产量',
    cotton_production_吨 INT COMMENT '棉花产量',
    oil_production_吨 INT COMMENT '油料产量',
    meat_production_吨 INT COMMENT '肉类总产量',
    agriculture_output_万元 BIGINT COMMENT '农林牧渔业总产值',
    -- 计算字段
    mechanization_rate DECIMAL(5,2) GENERATED ALWAYS AS (
        CASE WHEN cultivated_area_公顷 > 0 THEN (mechanized_area_公顷 / cultivated_area_公顷) * 100 ELSE NULL END
    ) STORED COMMENT '农业机械化率(%)',
    grain_yield_per_hectare DECIMAL(8,2) GENERATED ALWAYS AS (
        CASE WHEN crop_area_千公顷 > 0 THEN grain_production_吨 / (crop_area_千公顷 * 1000) ELSE NULL END
    ) STORED COMMENT '粮食单产(吨/公顷)',
    FOREIGN KEY (county_code) REFERENCES county_basic(county_code) ON DELETE CASCADE,
    UNIQUE KEY uk_county_year (county_code, year),
    INDEX idx_year (year)
) ENGINE=InnoDB COMMENT='农业生产表';

-- ========================================
-- 7. 工业发展表
-- ========================================

CREATE TABLE industry_development (
    id INT AUTO_INCREMENT PRIMARY KEY,
    county_code VARCHAR(10) NOT NULL,
    year YEAR NOT NULL,
    large_industrial_enterprises INT COMMENT '规模以上工业企业数',
    industrial_output_万元 BIGINT COMMENT '规模以上工业总产值',
    export_amount_美元 BIGINT COMMENT '出口额',
    foreign_investment_美元 BIGINT COMMENT '实际利用外资金额',
    -- 计算字段
    industrial_concentration DECIMAL(5,2) COMMENT '工业集中度(%)',
    export_dependency DECIMAL(5,2) COMMENT '出口依存度(%)',
    FOREIGN KEY (county_code) REFERENCES county_basic(county_code) ON DELETE CASCADE,
    UNIQUE KEY uk_county_year (county_code, year),
    INDEX idx_year (year)
) ENGINE=InnoDB COMMENT='工业发展表';

-- ========================================
-- 8. 基础设施表
-- ========================================

CREATE TABLE infrastructure (
    id INT AUTO_INCREMENT PRIMARY KEY,
    county_code VARCHAR(10) NOT NULL,
    year YEAR NOT NULL,
    fixed_phones INT COMMENT '固定电话用户数',
    mobile_phones INT COMMENT '移动电话用户数',
    broadband_users INT COMMENT '宽带接入用户数',
    power_consumption_万千瓦时 BIGINT COMMENT '全社会用电量',
    residential_power_万千瓦时 BIGINT COMMENT '城乡居民生活用电量',
    -- 计算字段
    digital_penetration_rate DECIMAL(5,2) COMMENT '数字化普及率(%)',
    power_consumption_per_capita DECIMAL(8,2) COMMENT '人均用电量',
    FOREIGN KEY (county_code) REFERENCES county_basic(county_code) ON DELETE CASCADE,
    UNIQUE KEY uk_county_year (county_code, year),
    INDEX idx_year (year)
) ENGINE=InnoDB COMMENT='基础设施表';

-- ========================================
-- 9. 投资消费表
-- ========================================

CREATE TABLE investment_consumption (
    id INT AUTO_INCREMENT PRIMARY KEY,
    county_code VARCHAR(10) NOT NULL,
    year YEAR NOT NULL,
    urban_fixed_investment_万元 BIGINT COMMENT '城镇固定资产投资完成额',
    total_fixed_investment_万元 BIGINT COMMENT '全社会固定资产投资',
    retail_sales_万元 BIGINT COMMENT '社会消费品零售总额',
    real_estate_investment_亿元 DECIMAL(8,2) COMMENT '房地产开发投资',
    -- 计算字段
    investment_efficiency DECIMAL(5,2) COMMENT '投资效率(%)',
    consumption_rate DECIMAL(5,2) COMMENT '消费率(%)',
    FOREIGN KEY (county_code) REFERENCES county_basic(county_code) ON DELETE CASCADE,
    UNIQUE KEY uk_county_year (county_code, year),
    INDEX idx_year (year)
) ENGINE=InnoDB COMMENT='投资消费表';

-- ========================================
-- 10. 教育卫生表
-- ========================================

CREATE TABLE education_health (
    id INT AUTO_INCREMENT PRIMARY KEY,
    county_code VARCHAR(10) NOT NULL,
    year YEAR NOT NULL,
    primary_schools INT COMMENT '普通小学学校数',
    middle_schools INT COMMENT '普通中学学校数',
    primary_teachers INT COMMENT '普通小学专任教师数',
    middle_teachers INT COMMENT '普通中学专任教师数',
    primary_students INT COMMENT '普通小学在校生数',
    middle_students INT COMMENT '普通中学在校学生数',
    vocational_students INT COMMENT '中等职业教育学校在校学生数',
    hospital_beds INT COMMENT '医院、卫生院床位数',
    health_workers INT COMMENT '卫生技术人员数',
    doctors INT COMMENT '执业医师数',
    welfare_institutions INT COMMENT '社会福利收养性单位数',
    welfare_beds INT COMMENT '社会福利收养性单位床位数',
    education_investment_万元 BIGINT COMMENT '教育投入',
    health_investment_万元 BIGINT COMMENT '卫生投入',
    -- 计算字段
    student_teacher_ratio_primary DECIMAL(5,2) GENERATED ALWAYS AS (
        CASE WHEN primary_teachers > 0 THEN primary_students / primary_teachers ELSE NULL END
    ) STORED COMMENT '小学生师比',
    beds_per_1000_people DECIMAL(5,2) COMMENT '每千人床位数',
    FOREIGN KEY (county_code) REFERENCES county_basic(county_code) ON DELETE CASCADE,
    UNIQUE KEY uk_county_year (county_code, year),
    INDEX idx_year (year)
) ENGINE=InnoDB COMMENT='教育卫生表';

-- ========================================
-- 11. 收入与生活水平表
-- ========================================

CREATE TABLE income_living_standard (
    id INT AUTO_INCREMENT PRIMARY KEY,
    county_code VARCHAR(10) NOT NULL,
    year YEAR NOT NULL,
    urban_average_wage DECIMAL(10,2) COMMENT '城镇单位在岗职工平均工资',
    urban_disposable_income DECIMAL(10,2) COMMENT '城镇居民人均可支配收入',
    rural_disposable_income DECIMAL(10,2) COMMENT '农村居民人均可支配收入',
    -- 计算字段
    income_gap_ratio DECIMAL(5,2) GENERATED ALWAYS AS (
        CASE WHEN rural_disposable_income > 0 THEN urban_disposable_income / rural_disposable_income ELSE NULL END
    ) STORED COMMENT '城乡收入差距比',
    poverty_incidence DECIMAL(5,2) COMMENT '贫困发生率(%)',
    engel_coefficient DECIMAL(5,2) COMMENT '恩格尔系数(%)',
    FOREIGN KEY (county_code) REFERENCES county_basic(county_code) ON DELETE CASCADE,
    UNIQUE KEY uk_county_year (county_code, year),
    INDEX idx_year (year)
) ENGINE=InnoDB COMMENT='收入与生活水平表';

-- ========================================
-- 12. 环境与文化表
-- ========================================

CREATE TABLE environment_culture (
    id INT AUTO_INCREMENT PRIMARY KEY,
    county_code VARCHAR(10) NOT NULL,
    year YEAR NOT NULL,
    nox_emission_吨 DECIMAL(10,2) COMMENT '氮氧化物排放量',
    dust_emission_吨 DECIMAL(10,2) COMMENT '烟尘排放量',
    so2_emission_吨 DECIMAL(10,2) COMMENT '工业废气中二氧化硫排放量',
    theaters_count INT COMMENT '艺术表演场馆数',
    library_collection_千册 DECIMAL(10,2) COMMENT '公共图书馆总藏量',
    sports_venues INT COMMENT '体育场馆机构数',
    -- 计算字段
    air_quality_index DECIMAL(5,2) COMMENT '空气质量指数',
    green_coverage_rate DECIMAL(5,2) COMMENT '绿化覆盖率(%)',
    emission_intensity DECIMAL(8,4) COMMENT '排放强度(吨/平方公里)',
    FOREIGN KEY (county_code) REFERENCES county_basic(county_code) ON DELETE CASCADE,
    UNIQUE KEY uk_county_year (county_code, year),
    INDEX idx_year (year)
) ENGINE=InnoDB COMMENT='环境与文化表';

-- ========================================
-- 13. 风险评估体系表
-- ========================================

-- 13.1 风险指标定义表
CREATE TABLE risk_indicators (
    indicator_id INT AUTO_INCREMENT PRIMARY KEY,
    indicator_code VARCHAR(50) UNIQUE NOT NULL,
    indicator_name VARCHAR(200) NOT NULL,
    category ENUM('经济风险', '社会风险', '环境风险', '治理风险', '发展风险') NOT NULL,
    subcategory VARCHAR(100),
    calculation_method TEXT COMMENT '计算方法说明',
    data_source VARCHAR(200),
    weight DECIMAL(5,4) DEFAULT 0.0000,
    threshold_high DECIMAL(8,4) COMMENT '高风险阈值',
    threshold_medium DECIMAL(8,4) COMMENT '中风险阈值',
    threshold_low DECIMAL(8,4) COMMENT '低风险阈值',
    status ENUM('启用', '停用') DEFAULT '启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category (category),
    INDEX idx_status (status)
) ENGINE=InnoDB COMMENT='风险指标定义表';

-- 13.2 风险得分计算表
CREATE TABLE risk_scores (
    id INT AUTO_INCREMENT PRIMARY KEY,
    county_code VARCHAR(10) NOT NULL,
    year YEAR NOT NULL,
    indicator_id INT NOT NULL,
    raw_value DECIMAL(15,4) COMMENT '原始指标值',
    normalized_value DECIMAL(8,4) COMMENT '标准化后的值',
    risk_score DECIMAL(8,4) COMMENT '风险得分(0-1)',
    risk_level ENUM('低风险', '中低风险', '中风险', '中高风险', '高风险'),
    calculation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (county_code) REFERENCES county_basic(county_code) ON DELETE CASCADE,
    FOREIGN KEY (indicator_id) REFERENCES risk_indicators(indicator_id) ON DELETE CASCADE,
    UNIQUE KEY uk_county_year_indicator (county_code, year, indicator_id),
    INDEX idx_year (year),
    INDEX idx_risk_level (risk_level)
) ENGINE=InnoDB COMMENT='风险得分计算表';

-- 13.3 综合风险评估表
CREATE TABLE comprehensive_risk_assessment (
    id INT AUTO_INCREMENT PRIMARY KEY,
    county_code VARCHAR(10) NOT NULL,
    year YEAR NOT NULL,
    economic_risk_score DECIMAL(8,4) COMMENT '经济风险得分',
    social_risk_score DECIMAL(8,4) COMMENT '社会风险得分',
    environment_risk_score DECIMAL(8,4) COMMENT '环境风险得分',
    governance_risk_score DECIMAL(8,4) COMMENT '治理风险得分',
    development_risk_score DECIMAL(8,4) COMMENT '发展风险得分',
    comprehensive_risk_score DECIMAL(8,4) COMMENT '综合风险得分',
    risk_level ENUM('低风险', '中低风险', '中风险', '中高风险', '高风险'),
    risk_trend ENUM('改善', '稳定', '恶化'),
    major_risk_factors TEXT COMMENT '主要风险因素',
    assessment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (county_code) REFERENCES county_basic(county_code) ON DELETE CASCADE,
    UNIQUE KEY uk_county_year (county_code, year),
    INDEX idx_year (year),
    INDEX idx_risk_level (risk_level),
    INDEX idx_comprehensive_score (comprehensive_risk_score)
) ENGINE=InnoDB COMMENT='综合风险评估表';

-- ========================================
-- 14. 预警管理表
-- ========================================

-- 14.1 用户表
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'EXPERT', 'ANALYST', 'VIEWER', 'DATA_ENGINEER') NOT NULL DEFAULT 'VIEWER',
    real_name VARCHAR(100),
    email VARCHAR(200),
    phone VARCHAR(20),
    department VARCHAR(100),
    status ENUM('启用', '停用') DEFAULT '启用',
    last_login TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_role (role)
) ENGINE=InnoDB COMMENT='系统用户表';

-- 14.2 预警规则表
CREATE TABLE alert_rules (
    rule_id INT AUTO_INCREMENT PRIMARY KEY,
    rule_name VARCHAR(200) NOT NULL,
    rule_category ENUM('经济预警', '社会预警', '环境预警', '综合预警') NOT NULL,
    rule_expression TEXT COMMENT '规则表达式',
    conditions_json JSON COMMENT '预警条件JSON格式',
    threshold_high DECIMAL(8,4) COMMENT '高风险阈值',
    threshold_medium DECIMAL(8,4) COMMENT '中风险阈值',
    threshold_low DECIMAL(8,4) COMMENT '低风险阈值',
    weight DECIMAL(5,4) DEFAULT 1.0000,
    priority INT DEFAULT 1 COMMENT '优先级(1-10)',
    status ENUM('启用', '停用') DEFAULT '启用',
    description TEXT,
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(user_id),
    INDEX idx_category (rule_category),
    INDEX idx_status (status),
    INDEX idx_priority (priority)
) ENGINE=InnoDB COMMENT='预警规则表';

-- 14.3 预警记录表
CREATE TABLE alerts (
    alert_id INT AUTO_INCREMENT PRIMARY KEY,
    county_code VARCHAR(10) NOT NULL,
    year YEAR NOT NULL,
    risk_score_id INT COMMENT '关联的风险评估记录',
    risk_level ENUM('GREEN', 'YELLOW', 'ORANGE', 'RED') NOT NULL,
    alert_type ENUM('经济预警', '社会预警', '环境预警', '综合预警'),
    triggered_rules TEXT COMMENT '触发的规则列表',
    title VARCHAR(500) NOT NULL,
    description TEXT COMMENT '预警详细说明',
    recommendation TEXT COMMENT '处置建议',
    severity ENUM('严重', '中等', '轻微') DEFAULT '中等',
    status ENUM('新建', '已确认', '处理中', '已处理', '已关闭') DEFAULT '新建',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    confirmed_by INT COMMENT '确认人',
    confirmed_at TIMESTAMP NULL,
    handled_by INT COMMENT '处理人',
    handled_at TIMESTAMP NULL,
    closed_by INT COMMENT '关闭人',
    closed_at TIMESTAMP NULL,
    review_comment TEXT COMMENT '审核意见',
    FOREIGN KEY (county_code) REFERENCES county_basic(county_code) ON DELETE CASCADE,
    FOREIGN KEY (confirmed_by) REFERENCES users(user_id),
    FOREIGN KEY (handled_by) REFERENCES users(user_id),
    FOREIGN KEY (closed_by) REFERENCES users(user_id),
    INDEX idx_county_year (county_code, year),
    INDEX idx_risk_level (risk_level),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB COMMENT='预警记录表';

-- 14.4 规则触发记录表（多对多关系）
CREATE TABLE alert_rule_triggers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    alert_id INT NOT NULL,
    rule_id INT NOT NULL,
    trigger_score DECIMAL(8,4) COMMENT '触发时的得分',
    trigger_details JSON COMMENT '触发详情',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (alert_id) REFERENCES alerts(alert_id) ON DELETE CASCADE,
    FOREIGN KEY (rule_id) REFERENCES alert_rules(rule_id) ON DELETE CASCADE,
    UNIQUE KEY uk_alert_rule (alert_id, rule_id)
) ENGINE=InnoDB COMMENT='规则触发记录表';

-- ========================================
-- 15. 系统管理表
-- ========================================

-- 15.1 数据操作日志表
CREATE TABLE data_logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    table_name VARCHAR(100) NOT NULL,
    record_id VARCHAR(50),
    county_code VARCHAR(10),
    year YEAR,
    user_id INT,
    operation_type ENUM('INSERT', 'UPDATE', 'DELETE', 'SELECT') NOT NULL,
    changed_fields JSON COMMENT '变更字段详情',
    old_values JSON COMMENT '变更前的值',
    new_values JSON COMMENT '变更后的值',
    ip_address VARCHAR(45),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    comment TEXT,
    FOREIGN KEY (county_code) REFERENCES county_basic(county_code),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    INDEX idx_table_name (table_name),
    INDEX idx_timestamp (timestamp),
    INDEX idx_operation (operation_type)
) ENGINE=InnoDB COMMENT='数据操作日志表';

-- 15.2 数据质量检查表
CREATE TABLE data_quality_checks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    table_name VARCHAR(100) NOT NULL,
    county_code VARCHAR(10),
    year YEAR,
    check_type ENUM('完整性', '一致性', '准确性', '时效性', '有效性') NOT NULL,
    field_name VARCHAR(100),
    issue_description TEXT NOT NULL,
    expected_value VARCHAR(500),
    actual_value VARCHAR(500),
    severity ENUM('严重', '中等', '轻微') DEFAULT '中等',
    status ENUM('待处理', '处理中', '已解决', '已忽略') DEFAULT '待处理',
    detected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    detected_by INT COMMENT '检测人员',
    resolved_at TIMESTAMP NULL,
    resolved_by INT COMMENT '处理人员',
    resolution_comment TEXT,
    FOREIGN KEY (county_code) REFERENCES county_basic(county_code),
    FOREIGN KEY (detected_by) REFERENCES users(user_id),
    FOREIGN KEY (resolved_by) REFERENCES users(user_id),
    INDEX idx_table_name (table_name),
    INDEX idx_severity_status (severity, status),
    INDEX idx_detected_at (detected_at)
) ENGINE=InnoDB COMMENT='数据质量检查表';

-- 15.3 系统配置表
CREATE TABLE system_configs (
    config_id INT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) UNIQUE NOT NULL,
    config_value TEXT,
    config_type ENUM('STRING', 'NUMBER', 'BOOLEAN', 'JSON') DEFAULT 'STRING',
    category VARCHAR(50) DEFAULT 'GENERAL',
    description TEXT,
    is_editable BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by INT,
    FOREIGN KEY (updated_by) REFERENCES users(user_id),
    INDEX idx_category (category)
) ENGINE=InnoDB COMMENT='系统配置表';

-- ========================================
-- 插入初始数据
-- ========================================

-- 插入系统管理员用户
INSERT INTO users (username, password_hash, role, real_name, email, department) VALUES
('admin', SHA2('admin123', 256), 'ADMIN', '系统管理员', 'admin@example.com', '信息技术部'),
('analyst1', SHA2('analyst123', 256), 'ANALYST', '风险分析师', 'analyst1@example.com', '风险管理部'),
('expert1', SHA2('expert123', 256), 'EXPERT', '风险专家', 'expert1@example.com', '政策研究部'),
('viewer1', SHA2('viewer123', 256), 'VIEWER', '普通用户', 'viewer1@example.com', '综合办公室');

-- 插入基础风险指标
INSERT INTO risk_indicators (indicator_code, indicator_name, category, subcategory, weight, threshold_high, threshold_medium, threshold_low, calculation_method) VALUES
('GDP_GROWTH', 'GDP增长率', '经济风险', '经济增长', 0.15, 0.8, 0.6, 0.4, 'GDP年增长率低于阈值则风险增加'),
('GDP_PER_CAPITA', '人均GDP水平', '经济风险', '发展水平', 0.10, 0.7, 0.5, 0.3, '人均GDP相对全国平均水平'),
('FISCAL_SELF_SUFFICIENCY', '财政自给率', '经济风险', '财政状况', 0.12, 0.8, 0.6, 0.4, '地方财政收入/支出比例'),
('DEBT_RATIO', '政府债务率', '经济风险', '债务风险', 0.10, 0.9, 0.7, 0.5, '债务余额/财政收入比例'),
('POPULATION_DECLINE', '人口流失率', '社会风险', '人口变化', 0.08, 0.8, 0.6, 0.4, '年度人口净流出率'),
('EMPLOYMENT_RATE', '就业率', '社会风险', '就业状况', 0.10, 0.8, 0.6, 0.4, '就业人口/劳动年龄人口'),
('INCOME_GAP', '城乡收入差距', '社会风险', '收入分配', 0.06, 0.7, 0.5, 0.3, '城镇居民收入/农村居民收入'),
('AIR_QUALITY', '空气质量', '环境风险', '大气污染', 0.08, 0.8, 0.6, 0.4, '基于PM2.5、PM10等综合评价'),
('EMISSION_INTENSITY', '污染排放强度', '环境风险', '污染控制', 0.07, 0.9, 0.7, 0.5, '污染物排放量/GDP比值'),
('EDUCATION_INVESTMENT', '教育投入', '治理风险', '公共服务', 0.05, 0.6, 0.4, 0.2, '教育支出/财政支出比例'),
('HEALTH_INVESTMENT', '医疗投入', '治理风险', '公共服务', 0.05, 0.6, 0.4, 0.2, '医疗卫生支出/财政支出比例'),
('INNOVATION_CAPACITY', '创新能力', '发展风险', '创新发展', 0.04, 0.7, 0.5, 0.3, '高新技术企业数量、专利数量等综合评价');

-- 插入预警规则
INSERT INTO alert_rules (rule_name, rule_category, rule_expression, threshold_high, threshold_medium, threshold_low, description, created_by) VALUES
('GDP连续下降预警', '经济预警', 'GDP增长率连续3年为负', 0.9, 0.7, 0.5, '监测GDP持续下降的县域经济风险', 1),
('财政收支严重失衡', '经济预警', '财政自给率<20%且债务收入比>300%', 0.95, 0.8, 0.6, '监测财政不可持续风险', 1),
('人口大幅流失预警', '社会预警', '连续5年人口净流出且年均流失率>5%', 0.9, 0.7, 0.5, '监测县域人口空心化风险', 1),
('就业形势严峻预警', '社会预警', '就业率<75%或连续3年下降', 0.85, 0.65, 0.4, '监测就业市场恶化风险', 1),
('环境污染严重预警', '环境预警', '空气质量连续重度污染或排放强度超标', 0.9, 0.7, 0.5, '监测环境质量恶化风险', 1),
('县域发展综合预警', '综合预警', '经济、社会、环境风险得分均高于阈值', 0.8, 0.6, 0.4, '县域综合发展风险预警', 1);

-- 插入系统配置
INSERT INTO system_configs (config_key, config_value, config_type, category, description) VALUES
('RISK_CALCULATION_FREQUENCY', '30', 'NUMBER', 'RISK', '风险评估计算频率（天）'),
('ALERT_NOTIFICATION_ENABLED', 'true', 'BOOLEAN', 'ALERT', '是否启用预警通知'),
('DATA_RETENTION_PERIOD', '10', 'NUMBER', 'DATA', '数据保留期限（年）'),
('MAX_ALERT_LEVEL', 'RED', 'STRING', 'ALERT', '最高预警级别'),
('SYSTEM_VERSION', '2.0.0', 'STRING', 'GENERAL', '系统版本'),
('DEFAULT_RISK_WEIGHTS', '{"economic":0.35,"social":0.25,"environment":0.15,"governance":0.15,"development":0.10}', 'JSON', 'RISK', '默认风险权重配置');

-- ========================================
-- 创建视图
-- ========================================

-- 创建县域概况视图
CREATE VIEW county_overview AS
SELECT 
    cb.county_code,
    cb.county_name,
    cb.city_name,
    cb.province_name,
    cb.development_level,
    cb.land_area_km2,
    ps.total_population_万,
    ps.urbanization_rate,
    ea.gdp_万元,
    ea.gdp_per_capita,
    ea.gdp_growth_rate,
    ff.fiscal_self_sufficiency,
    ils.income_gap_ratio,
    COALESCE(cra.risk_level, '未评估') as current_risk_level,
    cra.comprehensive_risk_score
FROM county_basic cb
LEFT JOIN population_statistics ps ON cb.county_code = ps.county_code AND ps.year = (SELECT MAX(year) FROM population_statistics WHERE county_code = cb.county_code)
LEFT JOIN economic_aggregate ea ON cb.county_code = ea.county_code AND ea.year = (SELECT MAX(year) FROM economic_aggregate WHERE county_code = cb.county_code)
LEFT JOIN fiscal_finance ff ON cb.county_code = ff.county_code AND ff.year = (SELECT MAX(year) FROM fiscal_finance WHERE county_code = cb.county_code)
LEFT JOIN income_living_standard ils ON cb.county_code = ils.county_code AND ils.year = (SELECT MAX(year) FROM income_living_standard WHERE county_code = cb.county_code)
LEFT JOIN comprehensive_risk_assessment cra ON cb.county_code = cra.county_code AND cra.year = (SELECT MAX(year) FROM comprehensive_risk_assessment WHERE county_code = cb.county_code);

-- 创建风险趋势视图
CREATE VIEW risk_trend_analysis AS
SELECT 
    county_code,
    year,
    economic_risk_score,
    social_risk_score,
    environment_risk_score,
    governance_risk_score,
    development_risk_score,
    comprehensive_risk_score,
    risk_level,
    risk_trend,
    LAG(comprehensive_risk_score, 1) OVER (PARTITION BY county_code ORDER BY year) as prev_year_score,
    CASE 
        WHEN LAG(comprehensive_risk_score, 1) OVER (PARTITION BY county_code ORDER BY year) IS NULL THEN '首次评估'
        WHEN comprehensive_risk_score > LAG(comprehensive_risk_score, 1) OVER (PARTITION BY county_code ORDER BY year) + 0.1 THEN '风险上升'
        WHEN comprehensive_risk_score < LAG(comprehensive_risk_score, 1) OVER (PARTITION BY county_code ORDER BY year) - 0.1 THEN '风险下降'
        ELSE '风险平稳'
    END as score_change_trend
FROM comprehensive_risk_assessment
ORDER BY county_code, year;

COMMIT;

-- ========================================
-- 脚本执行完成提示
-- ========================================
SELECT '数据库创建完成！' as message, 
       NOW() as created_time,
       VERSION() as mysql_version;
