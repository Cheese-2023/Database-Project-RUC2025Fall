#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
生成补充测试数据 - 为缺少财政数据的县生成合理的模拟数据
目标：让Dashboard能显示多样化的风险等级分布
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

def generate_fiscal_data(conn, year=2023):
    """为缺少财政数据的县生成模拟数据"""
    cursor = conn.cursor()
    
    # 1. 获取所有县
    cursor.execute("SELECT county_code FROM county_basic")
    all_counties = [row[0] for row in cursor.fetchall()]
    print(f"总县数: {len(all_counties)}")
    
    # 2. 获取已有财政数据的县
    cursor.execute(f"SELECT DISTINCT county_code FROM fiscal_finance WHERE year = {year}")
    existing_counties = {row[0] for row in cursor.fetchall()}
    print(f"已有财政数据的县: {len(existing_counties)}")
    
    # 3. 找出缺少数据的县
    missing_counties = [c for c in all_counties if c not in existing_counties]
    print(f"缺少财政数据的县: {len(missing_counties)}")
    
    if not missing_counties:
        print("所有县都已有数据！")
        return
    
    # 4. 获取已有数据的统计信息（用于生成合理的范围）
    cursor.execute(f"""
        SELECT 
            AVG(fiscal_revenue_万元) as avg_revenue,
            AVG(tax_revenue_万元) as avg_tax,
            AVG(fiscal_expenditure_万元) as avg_exp,
            AVG(savings_balance_万元) as avg_savings,
            AVG(loan_balance_万元) as avg_loan
        FROM fiscal_finance WHERE year = {year}
    """)
    stats = cursor.fetchone()
    
    # 设置合理的生成范围（使用现有数据的平均值作为基准）
    # 转换为float以避免Decimal类型问题
    base_revenue = float(stats[0] or 50000)
    base_tax = float(stats[1] or 30000)
    base_exp = float(stats[2] or 60000)
    base_savings = float(stats[3] or 200000)
    base_loan = float(stats[4] or 150000)
    
    print(f"\n基准值 (基于现有数据):")
    print(f"  财政收入: {base_revenue:.0f} 万元")
    print(f"  税收收入: {base_tax:.0f} 万元")
    print(f"  财政支出: {base_exp:.0f} 万元")
    
    # 5. 生成数据
    insert_query = """
    INSERT INTO fiscal_finance 
    (county_code, year, fiscal_revenue_万元, tax_revenue_万元, fiscal_expenditure_万元,
     savings_balance_万元, loan_balance_万元, fiscal_self_sufficiency, debt_to_revenue_ratio)
    VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
    """
    
    print(f"\n开始生成 {len(missing_counties)} 个县的测试数据...")
    
    success_count = 0
    for county_code in missing_counties:
        # 生成有变化的数据（0.3x - 3x 的范围）
        variation = random.uniform(0.3, 3.0)
        
        revenue = base_revenue * variation + random.gauss(0, base_revenue * 0.2)
        revenue = max(10000, revenue)  # 最小1万
        
        tax = base_tax * variation + random.gauss(0, base_tax * 0.2)
        tax = max(5000, min(tax, revenue * 0.9))  # 税收不超过总收入的90%
        
        # 支出通常大于或接近收入
        exp_ratio = random.uniform(0.9, 2.5)  # 支出可能是收入的0.9-2.5倍
        expenditure = revenue * exp_ratio + random.gauss(0, revenue * 0.1)
        expenditure = max(8000, expenditure)
        
        savings = base_savings * variation + random.gauss(0, base_savings * 0.3)
        savings = max(20000, savings)
        
        loan = base_loan * variation + random.gauss(0, base_loan * 0.4)
        loan = max(10000, loan)
        
        # 计算财政指标
        fiscal_self_sufficiency = (revenue / expenditure) * 100 if expenditure > 0 else 100
        # 限制在合理范围内
        fiscal_self_sufficiency = min(fiscal_self_sufficiency, 200)
        
        debt_to_revenue_ratio = (loan / revenue) * 100 if revenue > 0 else 0
        # 限制在合理范围内（避免数据库溢出）
        debt_to_revenue_ratio = min(debt_to_revenue_ratio, 999.99)
        
        try:
            cursor.execute(insert_query, (
                county_code, year,
                round(revenue, 2),
                round(tax, 2),
                round(expenditure, 2),
                round(savings, 2),
                round(loan, 2),
                round(fiscal_self_sufficiency, 2),
                round(debt_to_revenue_ratio, 2)
            ))
            success_count += 1
        except Exception as e:
            print(f"  警告: 县域 {county_code} 数据插入失败: {e}")
    
    conn.commit()
    print(f"✓ 成功生成 {success_count} 条测试数据")
    
    # 6. 验证结果
    cursor.execute(f"SELECT COUNT(*) FROM fiscal_finance WHERE year = {year}")
    total = cursor.fetchone()[0]
    print(f"\n当前{year}年财政数据总数: {total}")

def main():
    print("="*60)
    print("生成补充测试数据 - 财政金融")
    print("="*60)
    
    conn = connect_db()
    
    try:
        generate_fiscal_data(conn, year=2023)
        
        print("\n" + "="*60)
        print("✓ 数据生成完成！")
        print("="*60)
        print("\n下一步操作:")
        print("1. 在前端点击\"立即重新计算风险\"")
        print("2. 等待几秒后刷新Dashboard")
        print("3. 应该能看到不同的风险等级分布了！")
        
    except Exception as e:
        print(f"\n✗ 生成失败: {e}")
        import traceback
        traceback.print_exc()
        conn.rollback()
    finally:
        if conn.is_connected():
            conn.close()

if __name__ == "__main__":
    main()
