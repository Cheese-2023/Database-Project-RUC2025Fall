#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
查询风险评估统计信息
"""

import mysql.connector
from mysql.connector import Error

def connect_db():
    """连接数据库"""
    try:
        connection = mysql.connector.connect(
            host='localhost',
            database='county_risk_warning_system',
            user='root',
            password='xxx'
        )
        return connection
    except Error as e:
        print(f"✗ 数据库连接失败: {e}")
        return None

def main():
    print("=" * 60)
    print("风险评估统计信息")
    print("=" * 60)
    
    conn = connect_db()
    if not conn:
        return
    
    cursor = conn.cursor(dictionary=True)
    
    # 总体统计
    cursor.execute("""
        SELECT 
            COUNT(*) as total_count,
            ROUND(AVG(comprehensive_risk_score), 2) as avg_score,
            ROUND(MIN(comprehensive_risk_score), 2) as min_score,
            ROUND(MAX(comprehensive_risk_score), 2) as max_score
        FROM comprehensive_risk_assessment
    """)
    total_stats = cursor.fetchone()
    
    print(f"\n总记录数: {total_stats['total_count']}")
    print(f"平均综合风险分: {total_stats['avg_score']}")
    print(f"最低风险分: {total_stats['min_score']}")
    print(f"最高风险分: {total_stats['max_score']}")
    
    # 按风险等级统计
    print("\n" + "=" * 60)
    print("各风险等级分布:")
    print("=" * 60)
    
    cursor.execute("""
        SELECT 
            risk_level,
            COUNT(*) as count,
            ROUND(AVG(comprehensive_risk_score), 2) as avg_score,
            ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM comprehensive_risk_assessment), 2) as percentage
        FROM comprehensive_risk_assessment 
        GROUP BY risk_level 
        ORDER BY FIELD(risk_level, '低风险', '中低风险', '中风险', '中高风险', '高风险')
    """)
    
    risk_levels = cursor.fetchall()
    
    print(f"{'风险等级':<12} {'数量':>8} {'占比':>8} {'平均分':>10}")
    print("-" * 60)
    for row in risk_levels:
        print(f"{row['risk_level']:<12} {row['count']:>8} {row['percentage']:>7}% {row['avg_score']:>10}")
    
    # 查看几个样例
    print("\n" + "=" * 60)
    print("风险评估样例 (前5条):")
    print("=" * 60)
    
    cursor.execute("""
        SELECT 
            cb.county_name,
            cb.province_name,
            cra.year,
            cra.economic_risk_score,
            cra.social_risk_score,
            cra.environment_risk_score,
            cra.governance_risk_score,
            cra.development_risk_score,
            cra.comprehensive_risk_score,
            cra.risk_level
        FROM comprehensive_risk_assessment cra
        JOIN county_basic cb ON cra.county_code = cb.county_code
        ORDER BY cra.comprehensive_risk_score DESC
        LIMIT 5
    """)
    
    samples = cursor.fetchall()
    
    for i, row in enumerate(samples, 1):
        print(f"\n{i}. {row['province_name']} - {row['county_name']} ({row['year']}年)")
        print(f"   经济风险: {row['economic_risk_score']:.2f}, 社会风险: {row['social_risk_score']:.2f}")
        print(f"   环境风险: {row['environment_risk_score']:.2f}, 治理风险: {row['governance_risk_score']:.2f}")
        print(f"   发展风险: {row['development_risk_score']:.2f}")
        print(f"   综合风险: {row['comprehensive_risk_score']:.2f} ({row['risk_level']})")
    
    cursor.close()
    conn.close()
    
    print("\n" + "=" * 60)

if __name__ == "__main__":
    main()
