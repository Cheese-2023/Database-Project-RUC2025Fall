#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
扩展的县域数据导入脚本 - 包含财政金融和环境文化数据
"""

import pandas as pd
import mysql.connector
from mysql.connector import Error
import sys

DB_CONFIG = {
    'host': 'localhost',
    'user': 'root',
    'password': 'xxxx',
    'database': 'county_risk_warning_system',
    'charset': 'utf8mb4'
}

def connect_db():
    try:
        connection = mysql.connector.connect(**DB_CONFIG)
        if connection.is_connected():
            print("✓ 成功连接到MySQL数据库")
            return connection
    except Error as e:
        print(f"✗ 数据库连接失败: {e}")
        sys.exit(1)

def import_fiscal_finance(connection, df):
    """导入财政金融数据"""
    print("\n开始导入财政金融数据...")
    cursor = connection.cursor()
    
    insert_query = """
    INSERT INTO fiscal_finance 
    (county_code, year, fiscal_revenue_万元, tax_revenue_万元, fiscal_expenditure_万元,
     savings_balance_万元, loan_balance_万元, fiscal_self_sufficiency, debt_to_revenue_ratio)
    VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
    ON DUPLICATE KEY UPDATE 
    fiscal_revenue_万元 = VALUES(fiscal_revenue_万元),
    tax_revenue_万元 = VALUES(tax_revenue_万元),
    fiscal_expenditure_万元 = VALUES(fiscal_expenditure_万元),
    savings_balance_万元 = VALUES(savings_balance_万元),
    loan_balance_万元 = VALUES(loan_balance_万元),
    fiscal_self_sufficiency = VALUES(fiscal_self_sufficiency),
    debt_to_revenue_ratio = VALUES(debt_to_revenue_ratio)
    """
    
    count = 0
    for _, row in df.iterrows():
        try:
            # 计算财政自给率
            fiscal_self_sufficiency = None
            debt_to_revenue_ratio = None
            
            revenue = float(row['地方财政一般预算收入_万元']) if pd.notna(row['地方财政一般预算收入_万元']) else 0
            expenditure = float(row['地方财政一般预算支出_万元']) if pd.notna(row['地方财政一般预算支出_万元']) else 0
            loan = float(row['年末金融机构各项贷款余额_万元']) if pd.notna(row['年末金融机构各项贷款余额_万元']) else 0
            
            if expenditure > 0:
                fiscal_self_sufficiency = (revenue / expenditure) * 100
            
            if revenue > 0:
                debt_to_revenue_ratio = (loan / revenue) * 100
            
            cursor.execute(insert_query, (
                row['区县代码'],
                row['年份'],
                row['地方财政一般预算收入_万元'],
                row['各项税收_万元'],
                row['地方财政一般预算支出_万元'],
                row['城乡居民储蓄存款余额_万元'],
                row['年末金融机构各项贷款余额_万元'],
                fiscal_self_sufficiency,
                debt_to_revenue_ratio
            ))
            count += 1
        except Error as e:
            print(f"  警告: 记录导入失败: {e}")
    
    connection.commit()
    print(f"✓ 成功导入 {count} 条财政金融记录")

def import_environment_culture(connection, df):
    """导入环境文化数据"""
    print("\n开始导入环境文化数据...")
    cursor = connection.cursor()
    
    insert_query = """
    INSERT INTO environment_culture 
    (county_code, year, nox_emission_吨, dust_emission_吨, so2_emission_吨,
     theaters_count, library_collection_千册, sports_venues,
     air_quality_index, green_coverage_rate, emission_intensity)
    VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
    ON DUPLICATE KEY UPDATE 
    nox_emission_吨 = VALUES(nox_emission_吨),
    dust_emission_吨 = VALUES(dust_emission_吨),
    so2_emission_吨 = VALUES(so2_emission_吨),
    theaters_count = VALUES(theaters_count),
    library_collection_千册 = VALUES(library_collection_千册),
    sports_venues = VALUES(sports_venues),
    air_quality_index = VALUES(air_quality_index),
    green_coverage_rate = VALUES(green_coverage_rate),
    emission_intensity = VALUES(emission_intensity)
    """
    
    count = 0
    for _, row in df.iterrows():
        try:
            # 计算空气质量指数(AQI) - 基于污染物排放量估算
            air_quality_index = None
            emission_intensity = None
            
            nox = float(row['废气中氮氧化物排放量_吨']) if pd.notna(row['废气中氮氧化物排放量_吨']) else 0
            dust = float(row['废气中烟尘排放量_吨']) if pd.notna(row['废气中烟尘排放量_吨']) else 0
            so2 = float(row['工业废气中二氧化硫排放量_吨']) if pd.notna(row['工业废气中二氧化硫排放量_吨']) else 0
            gdp = float(row['地区生产总值_万元']) if pd.notna(row['地区生产总值_万元']) else 0
            
            # 简化的AQI计算: 基于总排放量，范围0-500
            total_emission = nox + dust + so2
            if total_emission > 0:
                # 将排放量映射到AQI (粗略估算)
                air_quality_index = min(50 + (total_emission / 100), 500)
            else:
                air_quality_index = 50  # 默认良好
            
            # 排放强度 = 总排放量 / GDP
            if gdp > 0:
                emission_intensity = total_emission / gdp
            
            # 绿化覆盖率 - 由于数据中没有，设置一个合理的默认值
            green_coverage_rate = 35.0  # 默认35%
            
            cursor.execute(insert_query, (
                row['区县代码'],
                row['年份'],
                row['废气中氮氧化物排放量_吨'],
                row['废气中烟尘排放量_吨'],
                row['工业废气中二氧化硫排放量_吨'],
                row['艺术表演场馆数_剧场、影剧院_个'],
                row['公共图书馆总藏量_千册'],
                row['体育场馆机构数_个'],
                air_quality_index,
                green_coverage_rate,
                emission_intensity
            ))
            count += 1
        except Error as e:
            print(f"  警告: 记录导入失败 (县域代码: {row.get('区县代码', 'unknown')}): {e}")
    
    connection.commit()
    print(f"✓ 成功导入 {count} 条环境文化记录")

def main():
    print("="*60)
    print("县域风险预警系统 - 财政金融与环境文化数据导入")
    print("="*60)
    
    excel_file = '../data/中国县域数据库6.0版.xlsx'
    
    connection = connect_db()
    
    try:
        print(f"\n正在读取Excel文件...")
        df = pd.read_excel(excel_file, engine='openpyxl')
        print(f"✓ 成功读取 {len(df)} 条记录")
        
        # 填充缺失值
        df = df.fillna(0)
        
        # 导入数据
        import_fiscal_finance(connection, df)
        import_environment_culture(connection, df)
        
        print("\n" + "="*60)
        print("✓ 数据导入完成！")
        print("="*60)
        print("\n建议下一步:")
        print("1. 刷新前端浏览器 (Ctrl+Shift+R)")
        print('2. 在数据管理页面点击"立即重新计算风险"')
        print("3. 查看Dashboard看到不同的风险等级分布")
        
    except Exception as e:
        print(f"\n✗ 导入失败: {e}")
        import traceback
        traceback.print_exc()
        connection.rollback()
    finally:
        if connection.is_connected():
            connection.close()

if __name__ == "__main__":
    main()
