USE county_risk_warning_system;

-- Insert Alert Rules if not exist
INSERT IGNORE INTO alert_rules (rule_name, rule_category, rule_expression, threshold_high, threshold_medium, threshold_low, description, status) VALUES
('GDP连续下降预警', '经济预警', 'GDP增长率连续3年为负', 0.9, 0.7, 0.5, '监测GDP持续下降的县域经济风险', '启用'),
('财政收支严重失衡', '经济预警', '财政自给率<20%且债务收入比>300%', 0.95, 0.8, 0.6, '监测财政不可持续风险', '启用'),
('人口大幅流失预警', '社会预警', '连续5年人口净流出且年均流失率>5%', 0.9, 0.7, 0.5, '监测县域人口空心化风险', '启用'),
('就业形势严峻预警', '社会预警', '就业率<75%或连续3年下降', 0.85, 0.65, 0.4, '监测就业市场恶化风险', '启用'),
('环境污染严重预警', '环境预警', '空气质量连续重度污染或排放强度超标', 0.9, 0.7, 0.5, '监测环境质量恶化风险', '启用');

-- Insert Alerts (Sample Data)
INSERT INTO alerts (county_code, year, risk_level, alert_type, title, description, status, created_at) VALUES
('654223', 2023, 'YELLOW', '经济预警', '沙湾县GDP增速放缓', '沙湾县2023年GDP增速低于全省平均水平，需关注经济发展动力不足问题。', '新建', NOW()),
('654026', 2023, 'YELLOW', '社会预警', '昭苏县人口流失风险', '昭苏县连续三年人口净流出，劳动力减少可能影响长期发展。', '新建', NOW()),
('653127', 2023, 'ORANGE', '环境预警', '麦盖提县土地沙化风险', '麦盖提县土地沙化面积有所增加，生态环境脆弱性上升。', '已确认', DATE_SUB(NOW(), INTERVAL 1 DAY)),
('652823', 2023, 'RED', '综合预警', '尉犁县综合发展风险', '尉犁县在经济结构和社会治理方面存在多重风险因素叠加。', '处理中', DATE_SUB(NOW(), INTERVAL 2 DAY)),
('632726', 2023, 'YELLOW', '经济预警', '曲麻莱县财政压力预警', '曲麻莱县财政自给率较低，对转移支付依赖度高。', '已处理', DATE_SUB(NOW(), INTERVAL 5 DAY));

-- Insert Data Quality Checks (Sample Data)
INSERT INTO data_quality_checks (table_name, county_code, year, check_type, field_name, issue_description, severity, status, detected_at) VALUES
('economic_aggregate', '654223', 2023, '完整性', 'gdp_growth_rate', 'GDP增长率字段为空', '中等', '待处理', NOW()),
('population_statistics', '654026', 2023, '准确性', 'total_population', '人口数据与去年相比波动异常（>10%）', '严重', '待处理', NOW()),
('fiscal_finance', '653127', 2023, '一致性', 'fiscal_income', '财政收入与分项之和不匹配', '中等', '处理中', DATE_SUB(NOW(), INTERVAL 1 DAY)),
('social_welfare', '652823', 2023, '时效性', 'update_time', '数据更新延迟超过30天', '轻微', '已解决', DATE_SUB(NOW(), INTERVAL 3 DAY)),
('environment_protection', '632726', 2023, '有效性', 'pm25_index', 'PM2.5指数超出合理范围', '严重', '已忽略', DATE_SUB(NOW(), INTERVAL 7 DAY));
