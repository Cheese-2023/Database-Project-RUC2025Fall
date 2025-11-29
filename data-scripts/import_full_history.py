#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
全量历史数据导入脚本 (import_full_history.py)

功能：
1. 导入2000-2023年的所有县域数据
2. 包含真实数据导入 (从Excel)
3. 自动补全缺失年份的数据 (2000-2023)
4. 数据生成包含随机波动，便于前端展示趋势
5. 支持数据扰动功能，可手动控制扰动大小
6. 仅执行数据写入，不触发后端计算

使用方法：
python3 import_full_history.py

数据扰动配置：
- 在脚本开头修改 DATA_PERTURBATION_RATE 参数
- 范围：0.0 (无扰动) 到 1.0 (最大扰动)
- 建议值：0.05 (5%) 到 0.20 (20%)
- 默认值：0.10 (10%)
"""

import pandas as pd
import mysql.connector
from mysql.connector import Error
import random
import sys
import os
import hashlib
import time

# 数据库配置
DB_CONFIG = {
    'host': 'localhost',
    'user': 'root',
    'password': '123456',
    'database': 'county_risk_warning_system',
    'charset': 'utf8mb4'
}

# ==========================================
# 数据扰动配置
# ==========================================
# 扰动强度：0.0-1.0，0.0表示无扰动，1.0表示最大扰动
# 建议值：0.05 (5%扰动) 到 0.20 (20%扰动)
DATA_PERTURBATION_RATE = 0.30  # 默认10%扰动，可根据需要修改

def apply_perturbation(value, rnd, perturbation_rate=DATA_PERTURBATION_RATE):
    """
    对数值应用扰动
    
    Args:
        value: 原始数值
        rnd: 随机数生成器
        perturbation_rate: 扰动强度 (0.0-1.0)
    
    Returns:
        扰动后的数值
    """
    if value == 0 or perturbation_rate == 0:
        return value
    
    # 生成 -perturbation_rate 到 +perturbation_rate 之间的随机扰动
    perturbation = rnd.uniform(-perturbation_rate, perturbation_rate)
    return value * (1 + perturbation)

def connect_db():
    try:
        connection = mysql.connector.connect(**DB_CONFIG)
        if connection.is_connected():
            print("✓ 成功连接到MySQL数据库")
            return connection
    except Error as e:
        print(f"✗ 数据库连接失败: {e}")
        sys.exit(1)

def get_deterministic_random(county_code, year, salt=""):
    """基于县代码和年份生成确定的随机种子，确保每次运行结果一致"""
    raw = f"{county_code}_{year}_{salt}"
    hash_val = hashlib.md5(raw.encode()).hexdigest()
    return int(hash_val, 16)

# ==========================================
# 1. 基础数据导入
# ==========================================

def import_county_basic(cursor, df):
    """导入县域基础信息 (只导入一次)"""
    print("\n[1] 正在导入县域基础信息...")
    
    counties = df[['区县代码', '区县', '城市', '省份', '行政区域土地面积_平方公里']].drop_duplicates('区县代码')
    
    insert_query = """
    INSERT INTO county_basic 
    (county_code, county_name, city_name, province_name, land_area_km2, development_level, administrative_level)
    VALUES (%s, %s, %s, %s, %s, '中等发达', '县')
    ON DUPLICATE KEY UPDATE 
    county_name = VALUES(county_name),
    city_name = VALUES(city_name),
    province_name = VALUES(province_name),
    land_area_km2 = VALUES(land_area_km2)
    """
    
    count = 0
    for _, row in counties.iterrows():
        try:
            cursor.execute(insert_query, (
                row['区县代码'],
                row['区县'],
                row['城市'],
                row['省份'],
                row['行政区域土地面积_平方公里']
            ))
            count += 1
        except Error:
            pass
    print(f"✓ 成功导入 {count} 个县域基础信息")

# ==========================================
# 2. 核心数据导入与生成 (按年份处理)
# ==========================================

def process_year_data(cursor, df, year, all_counties):
    """处理指定年份的数据：有真实值用真实值，没有则生成"""
    # print(f"  正在处理 {year} 年数据...")
    
    # 筛选当年的真实数据
    df_year = df[df['年份'] == year]
    real_data_map = df_year.set_index('区县代码').to_dict('index')
    
    # 准备批量插入的数据列表
    pop_data = []
    eco_data = []
    fiscal_data = []
    env_data = []
    edu_data = []
    inv_data = []
    
    for county_code in all_counties:
        # 获取确定性随机数生成器
        seed = get_deterministic_random(county_code, year)
        rnd = random.Random(seed)
        
        # --- 基础增长因子 (模拟随时间增长) ---
        # 以2010年为基准，每年增长约 6% + 随机波动
        growth_trend = 1.06 ** (year - 2010)
        # 加上 +- 10% 的随机波动
        fluctuation = rnd.uniform(0.90, 1.10)
        factor = growth_trend * fluctuation
        # 县域规模差异因子 (基于县代码hash，保持该县一贯的规模大小)
        scale_seed = int(hashlib.md5(str(county_code).encode()).hexdigest(), 16)
        scale_rnd = random.Random(scale_seed)
        county_scale = scale_rnd.uniform(0.1, 4.0) # 扩大差异：有的极小，有的极大
        
        # 县域基础增长率差异 (有的发展快，有的发展慢)
        base_growth_rate = scale_rnd.uniform(1.02, 1.12)
        
        # --- 基础增长因子 (模拟随时间增长) ---
        growth_trend = base_growth_rate ** (year - 2010)
        
        # --- 随机波动因子 (加大波动，便于展示) ---
        # 波动范围扩大到 ±30% (0.7 - 1.3)
        fluctuation = rnd.uniform(0.70, 1.30)
        factor = growth_trend * fluctuation
        
        # -------------------------------------------------
        # 1. 人口数据 (Population)
        # -------------------------------------------------
        row = real_data_map.get(county_code, {})
        
        if row and pd.notna(row.get('年末总人口_万人')):
            total_pop = float(row['年末总人口_万人'])
            rural_pop = float(row.get('乡村人口_万人', 0))
            # 应用扰动
            total_pop = apply_perturbation(total_pop, rnd)
            rural_pop = apply_perturbation(rural_pop, rnd)
        else:
            # 模拟人口: 基准 40万人 * 规模 * 微弱增长
            total_pop = 40.0 * county_scale * (1.01 ** (year - 2010)) * rnd.uniform(0.95, 1.05)
            rural_pop = total_pop * rnd.uniform(0.3, 0.8) # 乡村人口占比差异大
            
        pop_data.append((
            county_code, year,
            int(total_pop * 3000), # 户数估算
            int(rural_pop * 3000),
            round(total_pop, 2),
            round(rural_pop, 2),
            round(total_pop * 1.05, 2), # 户籍人口略多
            None, # 密度由数据库计算或忽略
            round((total_pop - rural_pop)/total_pop * 100, 2) if total_pop > 0 else 0
        ))

        # -------------------------------------------------
        # 2. 经济数据 (Economic)
        # -------------------------------------------------
        if row and pd.notna(row.get('地区生产总值_万元')):
            gdp = float(row['地区生产总值_万元'])
            gdp_per_capita = float(row.get('人均地区生产总值_元/人', 0))
            # 应用扰动
            gdp = apply_perturbation(gdp, rnd)
            gdp_per_capita = apply_perturbation(gdp_per_capita, rnd)
        else:
            # 模拟GDP: 基准 200亿 * 因子 * 额外随机噪音
            gdp = 2000000 * county_scale * factor * rnd.uniform(0.8, 1.2)
            gdp_per_capita = (gdp * 10000) / (total_pop * 10000) if total_pop > 0 else 50000
        
        # 产业结构模拟 (差异化)
        p1 = rnd.uniform(2, 30)
        p2 = rnd.uniform(20, 60)
        p3 = 100 - p1 - p2
        
        # 计算GDP增长率（如果有上一年数据）
        gdp_growth_rate = None
        if year > 2000:
            # 尝试获取上一年的GDP来计算增长率
            # 这里简化处理，使用随机增长率
            gdp_growth_rate = rnd.uniform(-5.0, 15.0)  # -5% 到 15% 的增长率
        
        eco_data.append((
            county_code, year,
            round(gdp, 2),
            round(gdp * p1 / 100, 2),
            round(gdp * p2 / 100, 2),
            round(gdp * p2 / 100 * rnd.uniform(0.7, 0.9), 2), 
            round(gdp * p3 / 100, 2),
            round(gdp * p1 / 100, 2),
            round(gdp * p1 / 100 * 0.4, 2),
            round(gdp_per_capita, 2),
            round(gdp_growth_rate, 2) if gdp_growth_rate else None,
            round(p1, 2), round(p2, 2), round(p3, 2)
        ))

        # -------------------------------------------------
        # 3. 财政数据 (Fiscal)
        # -------------------------------------------------
        if row and pd.notna(row.get('地方财政一般预算收入_万元')):
            revenue = float(row['地方财政一般预算收入_万元'])
            expenditure = float(row.get('地方财政一般预算支出_万元', revenue * 1.5))
            # 应用扰动
            revenue = apply_perturbation(revenue, rnd)
            expenditure = apply_perturbation(expenditure, rnd)
        else:
            # 财政收入通常占GDP的 3-15% (差异化)
            revenue = gdp * rnd.uniform(0.03, 0.15)
            # 支出通常大于收入 (差异化: 1.0 - 2.5倍)
            expenditure = revenue * rnd.uniform(1.0, 2.5)
            
        tax = revenue * rnd.uniform(0.6, 0.95)
        savings = gdp * rnd.uniform(0.6, 1.5)
        loan = gdp * rnd.uniform(0.4, 2.0) # 贷款差异大
        
        # 债务率波动 (20% - 400%) - 制造高风险和低风险
        debt_ratio = rnd.uniform(20, 400)
        
        fiscal_data.append((
            county_code, year,
            round(revenue, 2),
            round(tax, 2),
            round(expenditure, 2),
            round(savings, 2),
            round(loan, 2),
            round(revenue/expenditure*100 if expenditure>0 else 0, 2),
            round(debt_ratio, 2)
        ))

        # -------------------------------------------------
        # 4. 环境数据 (Environment)
        # -------------------------------------------------
        # 模拟环境数据 (差异化)
        aqi = rnd.uniform(20, 200) # AQI 范围扩大
        green_rate = rnd.uniform(10, 80) # 绿化率
        emission = rnd.uniform(0.1, 8.0) # 排放强度
        
        env_data.append((
            county_code, year,
            rnd.uniform(50, 2000), # NOx
            rnd.uniform(50, 2000), # Dust
            rnd.uniform(50, 2000), # SO2
            int(rnd.uniform(0, 20)), # Theaters
            rnd.uniform(5, 300), # Library
            int(rnd.uniform(1, 80)), # Sports
            round(aqi, 2),
            round(green_rate, 2),
            round(emission, 4)
        ))

        # -------------------------------------------------
        # 5. 教育卫生 (Education & Health) - 全模拟
        # -------------------------------------------------
        # 随GDP增长，但比例不同
        edu_inv = gdp * rnd.uniform(0.02, 0.06)
        health_inv = gdp * rnd.uniform(0.01, 0.05)
        
        edu_data.append((
            county_code, year,
            round(edu_inv, 2),
            round(health_inv, 2)
        ))

        # -------------------------------------------------
        # 6. 投资消费 (Investment & Consumption) - 全模拟
        # -------------------------------------------------
        fixed_inv = gdp * rnd.uniform(0.3, 1.0) # 投资拉动差异
        retail = gdp * rnd.uniform(0.2, 0.6)
        
        inv_data.append((
            county_code, year,
            round(fixed_inv * rnd.uniform(0.6, 0.9), 2),
            round(fixed_inv, 2),
            round(retail, 2),
            round(fixed_inv * rnd.uniform(0.1, 0.5) / 10000, 2), # 房地产(亿元)
            round(rnd.uniform(0.5, 3.0), 2), # 效率差异大
            round(retail/gdp*100 if gdp>0 else 40, 2) # 消费率
        ))

    # --- 执行批量插入 ---
    try:
        # Population
        cursor.executemany("""
            INSERT INTO population_statistics 
            (county_code, year, total_households, rural_households, total_population_万, 
             rural_population_万, registered_population_万, population_density, urbanization_rate)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
            ON DUPLICATE KEY UPDATE total_population_万=VALUES(total_population_万)
        """, pop_data)
        
        # Economic
        cursor.executemany("""
            INSERT INTO economic_aggregate 
            (county_code, year, gdp_万元, primary_industry_万元, secondary_industry_万元,
             industrial_value_万元, tertiary_industry_万元, agriculture_value_万元,
             livestock_value_万元, gdp_per_capita, gdp_growth_rate, industry_structure_1, 
             industry_structure_2, industry_structure_3)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
            ON DUPLICATE KEY UPDATE gdp_万元=VALUES(gdp_万元), gdp_growth_rate=VALUES(gdp_growth_rate)
        """, eco_data)
        
        # Fiscal
        cursor.executemany("""
            INSERT INTO fiscal_finance 
            (county_code, year, fiscal_revenue_万元, tax_revenue_万元, fiscal_expenditure_万元,
             savings_balance_万元, loan_balance_万元, fiscal_self_sufficiency, debt_to_revenue_ratio)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
            ON DUPLICATE KEY UPDATE fiscal_revenue_万元=VALUES(fiscal_revenue_万元)
        """, fiscal_data)
        
        # Environment
        cursor.executemany("""
            INSERT INTO environment_culture 
            (county_code, year, nox_emission_吨, dust_emission_吨, so2_emission_吨,
             theaters_count, library_collection_千册, sports_venues,
             air_quality_index, green_coverage_rate, emission_intensity)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
            ON DUPLICATE KEY UPDATE air_quality_index=VALUES(air_quality_index)
        """, env_data)
        
        # Education
        cursor.executemany("""
            INSERT INTO education_health (county_code, year, education_investment_万元, health_investment_万元)
            VALUES (%s, %s, %s, %s)
            ON DUPLICATE KEY UPDATE education_investment_万元=VALUES(education_investment_万元)
        """, edu_data)
        
        # Investment
        cursor.executemany("""
            INSERT INTO investment_consumption 
            (county_code, year, urban_fixed_investment_万元, total_fixed_investment_万元, 
             retail_sales_万元, real_estate_investment_亿元, investment_efficiency, consumption_rate)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
            ON DUPLICATE KEY UPDATE total_fixed_investment_万元=VALUES(total_fixed_investment_万元)
        """, inv_data)
        
    except Error as e:
        print(f"  ✗ 写入数据时发生错误: {e}")

def fix_risk_indicators(cursor):
    """修复风险指标配置，确保后端计算正常"""
    print("\n[0] 正在初始化风险指标配置...")
    try:
        # 1. 经济风险 (LT: 越低越危险)
        cursor.execute("UPDATE risk_indicators SET comparison_operator = 'LT', unit = '%' WHERE indicator_code IN ('GDP_GROWTH', 'FISCAL_SELF_SUFFICIENCY', 'EMPLOYMENT_RATE', 'EDUCATION_INVESTMENT', 'HEALTH_INVESTMENT')")
        cursor.execute("UPDATE risk_indicators SET comparison_operator = 'LT', unit = '万元' WHERE indicator_code = 'GDP_PER_CAPITA'")
        cursor.execute("UPDATE risk_indicators SET comparison_operator = 'LT', unit = '指数' WHERE indicator_code = 'INNOVATION_CAPACITY'")
        
        # 2. 逆向指标 (GT: 越高越危险)
        cursor.execute("UPDATE risk_indicators SET comparison_operator = 'GT', unit = '%' WHERE indicator_code IN ('DEBT_RATIO', 'POPULATION_DECLINE')")
        cursor.execute("UPDATE risk_indicators SET comparison_operator = 'GT', unit = '倍' WHERE indicator_code = 'INCOME_GAP'")
        cursor.execute("UPDATE risk_indicators SET comparison_operator = 'GT', unit = '指数' WHERE indicator_code = 'AIR_QUALITY'")
        cursor.execute("UPDATE risk_indicators SET comparison_operator = 'GT', unit = '吨/万元' WHERE indicator_code = 'EMISSION_INTENSITY'")
        
        print("✓ 风险指标配置已修正 (添加比较符和单位)")
    except Error as e:
        print(f"  ⚠️ 修复指标配置时警告: {e}")

def main():
    print("="*60)
    print("全量历史数据导入 (2000-2023)")
    print("="*60)
    print(f"数据扰动强度: {DATA_PERTURBATION_RATE * 100:.1f}%")
    print("提示: 可在脚本开头修改 DATA_PERTURBATION_RATE 参数调整扰动大小")
    print("="*60)
    
    start_time = time.time()
    
    excel_file = '../data/中国县域数据库6.0版.xlsx'
    if not os.path.exists(excel_file):
        print(f"错误: 找不到数据文件 {excel_file}")
        return

    conn = connect_db()
    cursor = conn.cursor()
    
    try:
        # 0. 修复指标配置 (最优先执行)
        fix_risk_indicators(cursor)
        conn.commit()

        # 1. 读取Excel并导入基础信息
        print(f"\n[1] 读取Excel文件: {excel_file}")
        df = pd.read_excel(excel_file, engine='openpyxl', sheet_name='ARIMA填补(慎用)')
        df = df.fillna(0)
        
        # 导入基础信息 (必须第一步)
        import_county_basic(cursor, df)
        conn.commit()
        
        # 获取所有县代码
        cursor.execute("SELECT county_code FROM county_basic")
        all_counties = [row[0] for row in cursor.fetchall()]
        print(f"✓ 系统共包含 {len(all_counties)} 个县域")
        
        # 2. 循环处理每一年的数据
        print("\n[2] 开始处理年度数据 (2000-2023)...")
        
        for year in range(2000, 2024):
            print(f"  > 正在处理 {year} 年数据...", end='\r')
            process_year_data(cursor, df, year, all_counties)
            conn.commit()
            print(f"  ✓ {year} 年数据处理完成    ")
            
        print("\n" + "="*60)
        print(f"✓ 所有数据导入完成！耗时: {int(time.time() - start_time)}秒")
        print("注意: 本脚本仅导入数据，未触发后端风险计算。")
        print("如需查看风险分析结果，请在前端页面点击'立即重新计算风险'。")
        print("="*60)
        
    except Exception as e:
        print(f"\n✗ 执行失败: {e}")
        import traceback
        traceback.print_exc()
        conn.rollback()
    finally:
        if conn.is_connected():
            cursor.close()
            conn.close()

if __name__ == "__main__":
    main()
