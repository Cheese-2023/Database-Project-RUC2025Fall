#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
生成完整的补充测试数据 - 包括教育健康和投资消费数据
"""

import mysql.connector
import random
import sys

DB_CONFIG = {
    'host': 'localhost',
    'user': 'root',
    'password': 'xxx',
    'database': 'county_risk_warning_system',
    'charset': 'utf8mb4'
}

def connect_db():
    try:
        conn = mysql.connector.connect(**DB_CONFIG)
        print("✓ 连接数据库成功")
        return conn
    except Exception as e:
        print(f"✗ 连接失败: {e}")
        sys.exit(1)

def generate_education_health_data(conn, year=2023):
    """生成教育健康数据"""
    cursor = conn.cursor()
    
    cursor.execute("SELECT county_code FROM county_basic")
    all_counties = [row[0] for row in cursor.fetchall()]
    
    cursor.execute(f"SELECT DISTINCT county_code FROM education_health WHERE year = {year}")
    existing = {row[0] for row in cursor.fetchall()}
    
    missing = [c for c in all_counties if c not in existing]
    print(f"\n生成教育健康数据: {len(missing)} 个县")
    
    insert_query = """
    INSERT INTO education_health 
    (county_code, year, education_investment_万元, health_investment_万元)
    VALUES (%s, %s, %s, %s)
    """
    
    count = 0
    for county_code in missing:
        # 生成不同投资水平（会影响治理风险）
        # 某些县投资低（高风险），某些投资高（低风险）
        edu_investment = random.uniform(5000, 50000)
        health_investment = random.uniform(3000, 30000)
        
        try:
            cursor.execute(insert_query, (
                county_code, year,
                round(edu_investment, 2),
                round(health_investment, 2)
            ))
            count += 1
        except Exception as e:
            pass
    
    conn.commit()
    print(f"✓ 成功生成 {count} 条教育健康记录")

def generate_investment_data(conn, year=2023):
    """生成投资消费数据"""
    cursor = conn.cursor()
    
    cursor.execute("SELECT county_code FROM county_basic")
    all_counties = [row[0] for row in cursor.fetchall()]
    
    cursor.execute(f"SELECT DISTINCT county_code FROM investment_consumption WHERE year = {year}")
    existing = {row[0] for row in cursor.fetchall()}
    
    missing = [c for c in all_counties if c not in existing]
    print(f"\n生成投资消费数据: {len(missing)} 个县")
    
    insert_query = """
    INSERT INTO investment_consumption 
    (county_code, year, urban_fixed_investment_万元, total_fixed_investment_万元, 
     retail_sales_万元, real_estate_investment_亿元, investment_efficiency, consumption_rate)
    VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
    """
    
    count = 0
    for county_code in missing:
        # 生成有差异的投资和消费数据
        total_inv = random.uniform(50000, 500000)
        urban_inv = total_inv * random.uniform(0.4, 0.8)
        retail = random.uniform(30000, 300000)
        realestate = random.uniform(1, 50)
        
        # 投资效率和消费率会影响发展风险
        inv_eff = random.uniform(0.5, 2.5)
        cons_rate = random.uniform(30, 70)
        
        try:
            cursor.execute(insert_query, (
                county_code, year,
                round(urban_inv, 2),
                round(total_inv, 2),
                round(retail, 2),
                round(realestate, 2),
                round(inv_eff, 3),
                round(cons_rate, 2)
            ))
            count += 1
        except Exception as e:
            pass
    
    conn.commit()
    print(f"✓ 成功生成 {count} 条投资消费记录")

def main():
    print("="*60)
    print("生成完整补充数据 - 教育健康 & 投资消费")
    print("="*60)
    
    conn = connect_db()
    
    try:
        generate_education_health_data(conn, 2023)
        generate_investment_data(conn, 2023)
        
        print("\n" + "="*60)
        print("✓ 数据生成完成！")
        print("="*60)
        print("\n请在前端点击\"立即重新计算风险\"")
        
    except Exception as e:
        print(f"\n✗ 失败: {e}")
        import traceback
        traceback.print_exc()
    finally:
        if conn.is_connected():
            conn.close()

if __name__ == "__main__":
    main()
