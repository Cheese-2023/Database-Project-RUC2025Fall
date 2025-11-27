#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
风险评估算法实现
功能：计算县域五大维度风险得分和综合风险等级
"""

import mysql.connector
from mysql.connector import Error
import sys
from typing import Dict, Tuple

# 数据库配置
DB_CONFIG = {
    'host': 'localhost',
    'user': 'root',
    'password': 'xxxx',
    'database': 'county_risk_warning_system',
    'charset': 'utf8mb4'
}

# 风险评估权重配置
RISK_WEIGHTS = {
    'economic': 0.35,      # 经济风险 35%
    'social': 0.25,        # 社会风险 25%
    'environment': 0.15,   # 环境风险 15%
    'governance': 0.15,    # 治理风险 15%
    'development': 0.10    # 发展风险 10%
}

def connect_db():
    """连接数据库"""
    try:
        connection = mysql.connector.connect(**DB_CONFIG)
        if connection.is_connected():
            return connection
    except Error as e:
        print(f"数据库连接失败: {e}")
        sys.exit(1)

def calculate_economic_risk(cursor, county_code: str, year: int) -> float:
    """
    计算经济风险得分 (0-100分，分数越高风险越大)
    考虑因素：
    - GDP增长率
    - 财政自给率
    - 产业结构合理性
    """
    risk_score = 0.0
    
    # 获取经济数据
    query = """
    SELECT gdp_growth_rate, industry_structure_1, industry_structure_2, industry_structure_3
    FROM economic_aggregate
    WHERE county_code = %s AND year = %s
    """
    cursor.execute(query, (county_code, year))
    result = cursor.fetchone()
    
    if result:
        gdp_growth = result[0] if result[0] else 0
        structure_1 = result[1] if result[1] else 0
        structure_2 = result[2] if result[2] else 0
        structure_3 = result[3] if result[3] else 0
        
        # GDP增长率风险 (权重40%)
        if gdp_growth < -3:
            risk_score += 40
        elif gdp_growth < 0:
            risk_score += 30
        elif gdp_growth < 3:
            risk_score += 20
        elif gdp_growth < 6:
            risk_score += 10
        
        # 产业结构风险 (权重30%)
        # 第一产业占比过高表示风险较大
        if structure_1 > 50:
            risk_score += 30
        elif structure_1 > 30:
            risk_score += 20
        elif structure_1 > 20:
            risk_score += 10
        
        # 第二产业占比过低也有风险
        if structure_2 < 20:
            risk_score += 15
        elif structure_2 < 30:
            risk_score += 10
    
    # 获取财政数据
    query = """
    SELECT fiscal_self_sufficiency
    FROM fiscal_finance
    WHERE county_code = %s AND year = %s
    """
    cursor.execute(query, (county_code, year))
    result = cursor.fetchone()
    
    if result:
        fiscal_rate = result[0] if result[0] else 0
        
        # 财政自给率风险 (权重30%)
        if fiscal_rate < 30:
            risk_score += 30
        elif fiscal_rate < 50:
            risk_score += 20
        elif fiscal_rate < 70:
            risk_score += 10
    
    return min(risk_score, 100)

def calculate_social_risk(cursor, county_code: str, year: int) -> float:
    """
    计算社会风险得分
    考虑因素：
    - 人口变化趋势
    - 城镇化率
    - 就业率
    """
    risk_score = 0.0
    
    # 获取人口数据
    query = """
    SELECT total_population_万, urbanization_rate
    FROM population_statistics
    WHERE county_code = %s AND year = %s
    """
    cursor.execute(query, (county_code, year))
    result = cursor.fetchone()
    
    if result:
        urbanization = result[1] if result[1] else 0
        
        # 城镇化率风险 (权重40%)
        if urbanization < 30:
            risk_score += 40
        elif urbanization < 40:
            risk_score += 30
        elif urbanization < 50:
            risk_score += 20
        elif urbanization < 60:
            risk_score += 10
    
    # 获取就业数据
    query = """
    SELECT employment_rate
    FROM employment_statistics
    WHERE county_code = %s AND year = %s
    """
    cursor.execute(query, (county_code, year))
    result = cursor.fetchone()
    
    if result:
        employment = result[0] if result[0] else 0
        
        # 就业率风险 (权重40%)
        if employment < 70:
            risk_score += 40
        elif employment < 80:
            risk_score += 30
        elif employment < 85:
            risk_score += 20
        elif employment < 90:
            risk_score += 10
    
    return min(risk_score, 100)

def calculate_environment_risk(cursor, county_code: str, year: int) -> float:
    """
    计算环境风险得分
    考虑因素：空气质量、污染排放强度、绿化覆盖率
    """
    risk_score = 0.0
    
    query = """
    SELECT air_quality_index, emission_intensity, green_coverage_rate
    FROM environment_culture
    WHERE county_code = %s AND year = %s
    """
    cursor.execute(query, (county_code, year))
    result = cursor.fetchone()
    
    if result:
        aqi = float(result[0]) if result[0] else 0
        emission = float(result[1]) if result[1] else 0
        green_rate = float(result[2]) if result[2] else 0
        
        # 空气质量风险 (权重40%)
        if aqi > 150:
            risk_score += 40
        elif aqi > 100:
            risk_score += 30
        elif aqi > 50:
            risk_score += 10
            
        # 排放强度风险 (权重30%)
        if emission > 1.0: # 假设阈值
            risk_score += 30
        elif emission > 0.5:
            risk_score += 15
            
        # 绿化覆盖率风险 (权重30%)
        if green_rate < 20:
            risk_score += 30
        elif green_rate < 30:
            risk_score += 20
        elif green_rate < 40:
            risk_score += 10
            
    return min(risk_score, 100)

def calculate_governance_risk(cursor, county_code: str, year: int) -> float:
    """
    计算治理风险得分
    考虑因素：教育投入、医疗投入
    """
    risk_score = 0.0
    
    # 获取教育卫生投入
    query = """
    SELECT education_investment_万元, health_investment_万元
    FROM education_health
    WHERE county_code = %s AND year = %s
    """
    cursor.execute(query, (county_code, year))
    eh_result = cursor.fetchone()
    
    # 获取财政支出
    query_fiscal = """
    SELECT fiscal_expenditure_万元
    FROM fiscal_finance
    WHERE county_code = %s AND year = %s
    """
    cursor.execute(query_fiscal, (county_code, year))
    fiscal_result = cursor.fetchone()
    
    if eh_result and fiscal_result and fiscal_result[0]:
        edu_inv = float(eh_result[0]) if eh_result[0] else 0
        health_inv = float(eh_result[1]) if eh_result[1] else 0
        fiscal_exp = float(fiscal_result[0])
        
        if fiscal_exp > 0:
            edu_ratio = (edu_inv / fiscal_exp) * 100
            health_ratio = (health_inv / fiscal_exp) * 100
            
            # 教育投入占比风险 (权重50%)
            if edu_ratio < 10:
                risk_score += 50
            elif edu_ratio < 15:
                risk_score += 30
            elif edu_ratio < 20:
                risk_score += 10
                
            # 医疗投入占比风险 (权重50%)
            if health_ratio < 5:
                risk_score += 50
            elif health_ratio < 8:
                risk_score += 30
            elif health_ratio < 10:
                risk_score += 10
                
    return min(risk_score, 100)

def calculate_development_risk(cursor, county_code: str, year: int) -> float:
    """
    计算发展风险得分
    考虑因素：投资效率、消费率
    """
    risk_score = 0.0
    
    # 获取投资消费数据
    query = """
    SELECT investment_efficiency, consumption_rate
    FROM investment_consumption
    WHERE county_code = %s AND year = %s
    """
    cursor.execute(query, (county_code, year))
    result = cursor.fetchone()
    
    if result:
        inv_eff = float(result[0]) if result[0] else 0
        cons_rate = float(result[1]) if result[1] else 0
        
        # 投资效率风险 (权重50%)
        if inv_eff < 0.5:
            risk_score += 50
        elif inv_eff < 0.8:
            risk_score += 30
        elif inv_eff < 1.0:
            risk_score += 10
            
        # 消费率风险 (权重50%)
        if cons_rate < 30:
            risk_score += 50
        elif cons_rate < 40:
            risk_score += 30
        elif cons_rate < 50:
            risk_score += 10
    
    return min(risk_score, 100)

def calculate_comprehensive_risk(risk_scores: Dict[str, float]) -> Tuple[float, str]:
    """
    计算综合风险得分和风险等级
    """
    comprehensive = (
        risk_scores['economic'] * RISK_WEIGHTS['economic'] +
        risk_scores['social'] * RISK_WEIGHTS['social'] +
        risk_scores['environment'] * RISK_WEIGHTS['environment'] +
        risk_scores['governance'] * RISK_WEIGHTS['governance'] +
        risk_scores['development'] * RISK_WEIGHTS['development']
    )
    
    # 风险等级判定
    if comprehensive < 20:
        level = '低风险'
    elif comprehensive < 35:
        level = '中低风险'
    elif comprehensive < 50:
        level = '中风险'
    elif comprehensive < 70:
        level = '中高风险'
    else:
        level = '高风险'
    
    return comprehensive, level

def save_risk_assessment(cursor, county_code: str, year: int, 
                        risk_scores: Dict[str, float], 
                        comprehensive: float, level: str):
    """保存风险评估结果"""
    insert_query = """
    INSERT INTO comprehensive_risk_assessment 
    (county_code, year, economic_risk_score, social_risk_score, 
     environment_risk_score, governance_risk_score, development_risk_score,
     comprehensive_risk_score, risk_level, assessment_date)
    VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, NOW())
    ON DUPLICATE KEY UPDATE
    economic_risk_score = VALUES(economic_risk_score),
    social_risk_score = VALUES(social_risk_score),
    environment_risk_score = VALUES(environment_risk_score),
    governance_risk_score = VALUES(governance_risk_score),
    development_risk_score = VALUES(development_risk_score),
    comprehensive_risk_score = VALUES(comprehensive_risk_score),
    risk_level = VALUES(risk_level),
    assessment_date = NOW()
    """
    
    cursor.execute(insert_query, (
        county_code, year,
        risk_scores['economic'],
        risk_scores['social'],
        risk_scores['environment'],
        risk_scores['governance'],
        risk_scores['development'],
        comprehensive,
        level
    ))

def main():
    """主函数"""
    print("="*60)
    print("县域风险评估计算工具")
    print("="*60)
    
    connection = connect_db()
    cursor = connection.cursor()
    
    try:
        # 获取所有县域和年份
        query = """
        SELECT DISTINCT county_code, year 
        FROM economic_aggregate 
        ORDER BY county_code, year
        """
        cursor.execute(query)
        records = cursor.fetchall()
        
        print(f"共需计算 {len(records)} 条风险评估记录\n")
        
        count = 0
        for county_code, year in records:
            # 计算五大维度风险
            risk_scores = {
                'economic': calculate_economic_risk(cursor, county_code, year),
                'social': calculate_social_risk(cursor, county_code, year),
                'environment': calculate_environment_risk(cursor, county_code, year),
                'governance': calculate_governance_risk(cursor, county_code, year),
                'development': calculate_development_risk(cursor, county_code, year)
            }
            
            # 计算综合风险
            comprehensive, level = calculate_comprehensive_risk(risk_scores)
            
            # 保存结果
            save_risk_assessment(cursor, county_code, year, risk_scores, comprehensive, level)
            
            count += 1
            if count % 100 == 0:
                print(f"已完成 {count}/{len(records)} 条记录的风险评估")
        
        connection.commit()
        print(f"\n✓ 风险评估完成！共计算 {count} 条记录")
        
    except Exception as e:
        print(f"✗ 风险评估过程中发生错误: {e}")
        connection.rollback()
    finally:
        cursor.close()
        connection.close()

if __name__ == "__main__":
    main()
