#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
重新生成合理的财政数据 - 债务率控制在合理范围
"""

import mysql.connector
import random
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
        conn = mysql.connector.connect(**DB_CONFIG)
        print("✓ 连接数据库成功")
        return conn
    except Exception as e:
        print(f"✗ 连接失败: {e}")
        sys.exit(1)

def regenerate_fiscal_data(conn, year=2023):
    """重新生成更合理的财政数据"""
    cursor = conn.cursor()
    
    # 1. 获取所有县
    cursor.execute("SELECT county_code FROM county_basic")
    all_counties = [row[0] for row in cursor.fetchall()]
    print(f"总县数: {len(all_counties)}")
    
    # 2. 获取已有真实数据的县（从Excel导入的）
    cursor.execute(f"""
        SELECT county_code 
        FROM fiscal_finance 
        WHERE year = {year} 
        AND (fiscal_revenue_万元 > 100000 OR tax_revenue_万元 > 50000)
    """)
    real_data_counties = {row[0] for row in cursor.fetchall()}
    print(f"有真实数据的县: {len(real_data_counties)}")
    
    # 3. 找出需要重新生成的县（删除模拟数据）
    missing_counties = [c for c in all_counties if c not in real_data_counties]
    print(f"需要重新生成数据的县: {len(missing_counties)}")
    
    # 4. 删除旧的模拟数据
    if missing_counties:
        placeholders = ','.join(['%s'] * len(missing_counties))
        cursor.execute(f"""
            DELETE FROM fiscal_finance 
            WHERE county_code IN ({placeholders}) AND year = {year}
        """, missing_counties)
        conn.commit()
        print(f"✓ 已删除 {cursor.rowcount} 条旧数据")
    
    # 5. 获取真实数据的统计信息
    cursor.execute(f"""
        SELECT 
            AVG(fiscal_revenue_万元) as avg_revenue,
            AVG(tax_revenue_万元) as avg_tax,
            AVG(fiscal_expenditure_万元) as avg_exp,
            AVG(savings_balance_万元) as avg_savings,
            AVG(loan_balance_万元) as avg_loan
        FROM fiscal_finance 
        WHERE year = {year}
        AND county_code IN ({','.join(['%s'] * len(real_data_counties))})
    """, list(real_data_counties))
    stats = cursor.fetchone()
    
    base_revenue = float(stats[0] or 76000)
    base_tax = float(stats[1] or 30000)
    base_exp = float(stats[2] or 148000)
    base_savings = float(stats[3] or 200000)
    base_loan = float(stats[4] or 100000)
    
    print(f"\n基准值 (基于真实数据):")
    print(f"  财政收入: {base_revenue:.0f} 万元")
    print(f"  财政支出: {base_exp:.0f} 万元")
    
    # 6. 生成新的合理数据
    insert_query = """
    INSERT INTO fiscal_finance 
    (county_code, year, fiscal_revenue_万元, tax_revenue_万元, fiscal_expenditure_万元,
     savings_balance_万元, loan_balance_万元, fiscal_self_sufficiency, debt_to_revenue_ratio)
    VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
    """
    
    print(f"\n开始生成 {len(missing_counties)} 个县的合理数据...")
    
    success_count = 0
    for county_code in missing_counties:
        # 使用更小的变化范围（0.5x - 2.0x）
        variation = random.uniform(0.5, 2.0)
        
        revenue = base_revenue * variation + random.gauss(0, base_revenue * 0.15)
        revenue = max(20000, revenue)
        
        tax = base_tax * variation + random.gauss(0, base_tax * 0.15)
        tax = max(10000, min(tax, revenue * 0.85))
        
        # 支出比例更合理: 0.95-1.5倍收入
        exp_ratio = random.uniform(0.95, 1.5)
        expenditure = revenue * exp_ratio + random.gauss(0, revenue * 0.05)
        expenditure = max(revenue * 0.9, expenditure)
        
        savings = base_savings * variation + random.gauss(0, base_savings * 0.2)
        savings = max(50000, savings)
        
        # 关键：控制贷款余额，让债务率合理
        # 债务率目标：50-150%（大部分在80-120%）
        target_debt_ratio = random.gauss(100, 25)  # 均值100%，标准差25%
        target_debt_ratio = max(50, min(target_debt_ratio, 200))  # 限制在50-200%
        
        loan = revenue * (target_debt_ratio / 100)
        loan = max(30000, loan)
        
        # 计算财政指标
        fiscal_self_sufficiency = (revenue / expenditure) * 100 if expenditure > 0 else 100
        fiscal_self_sufficiency = min(fiscal_self_sufficiency, 150)
        
        debt_to_revenue_ratio = (loan / revenue) * 100 if revenue > 0 else 0
        debt_to_revenue_ratio = min(debt_to_revenue_ratio, 250)  # 限制最大值
        
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
    print(f"✓ 成功生成 {success_count} 条合理数据")
    
    # 7. 验证结果
    cursor.execute(f"""
        SELECT 
            COUNT(*) as total,
            AVG(debt_to_revenue_ratio) as avg_debt,
            MIN(debt_to_revenue_ratio) as min_debt,
            MAX(debt_to_revenue_ratio) as max_debt,
            COUNT(CASE WHEN debt_to_revenue_ratio > 200 THEN 1 END) as high_debt_count
        FROM fiscal_finance 
        WHERE year = {year}
    """)
    result = cursor.fetchone()
    
    print(f"\n新数据统计:")
    print(f"  总记录数: {result[0]}")
    print(f"  债务率平均值: {result[1]:.1f}%")
    print(f"  债务率范围: {result[2]:.1f}% - {result[3]:.1f}%")
    print(f"  债务率>200%的县: {result[4]} 个")

def main():
    print("="*60)
    print("重新生成合理的财政数据")
    print("="*60)
    
    conn = connect_db()
    
    try:
        regenerate_fiscal_data(conn, year=2023)
        
        print("\n" + "="*60)
        print("✓ 数据重新生成完成！")
        print("="*60)
        print("\n下一步:")
        print("1. 后端会自动热重载")
        print("2. 刷新预警管理页面，重新检查预警")
        print("3. 应该看到更合理的预警分布")
        
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
