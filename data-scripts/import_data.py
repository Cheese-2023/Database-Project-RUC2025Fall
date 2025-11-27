#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
县域数据导入脚本
功能：将CSV格式的县域统计数据导入到MySQL数据库
"""

import pandas as pd
import mysql.connector
from mysql.connector import Error
import sys
from datetime import datetime

# 数据库配置
DB_CONFIG = {
    'host': 'localhost',
    'user': 'root',
    'password': 'xxx',  # 请修改为实际密码
    'database': 'county_risk_warning_system',
    'charset': 'utf8mb4'
}

def connect_db():
    """连接数据库"""
    try:
        connection = mysql.connector.connect(**DB_CONFIG)
        if connection.is_connected():
            print("✓ 成功连接到MySQL数据库")
            return connection
    except Error as e:
        print(f"✗ 数据库连接失败: {e}")
        sys.exit(1)

def load_csv_data(csv_file):
    """加载CSV数据"""
    try:
        print(f"正在读取CSV文件: {csv_file}")
        # 尝试不同的编码格式
        encodings = ['utf-8', 'gbk', 'gb2312', 'gb18030', 'latin1']
        df = None
        
        for encoding in encodings:
            try:
                df = pd.read_csv(csv_file, encoding=encoding)
                print(f"✓ 成功读取 {len(df)} 条记录 (编码: {encoding})")
                return df
            except UnicodeDecodeError:
                continue
        
        if df is None:
            raise Exception("无法识别文件编码，请确保CSV文件格式正确")
            
    except Exception as e:
        print(f"✗ CSV文件读取失败: {e}")
        print("提示: 请检查CSV文件是否存在，或尝试转换为UTF-8编码")
        sys.exit(1)

def load_excel_data(excel_file):
    """加载Excel数据"""
    try:
        print(f"正在读取Excel文件: {excel_file}")
        print("提示: Excel文件较大，读取需要一些时间...")
        
        # 读取Excel文件
        df = pd.read_excel(excel_file, engine='openpyxl')
        print(f"✓ 成功读取 {len(df)} 条记录")
        print(f"✓ 包含 {len(df.columns)} 个字段")
        
        return df
    except Exception as e:
        print(f"✗ Excel文件读取失败: {e}")
        print("提示: 请确保已安装 openpyxl: pip3 install openpyxl")
        sys.exit(1)

def clean_data(df):
    """数据清洗"""
    print("正在进行数据清洗...")
    
    # 处理缺失值
    df = df.fillna(0)
    
    # 去除异常值（这里可以根据实际情况添加更多规则）
    # 例如：人均GDP不应为负数
    if '人均地区生产总值_元人' in df.columns:
        df = df[df['人均地区生产总值_元人'] >= 0]
    
    print(f"✓ 数据清洗完成，剩余 {len(df)} 条有效记录")
    return df

def import_county_basic(connection, df):
    """导入县域基础信息"""
    print("\n开始导入县域基础信息...")
    cursor = connection.cursor()
    
    # 获取唯一县域列表
    counties = df[['区县代码', '区县', '城市', '省份', '行政区域土地面积_平方公里']].drop_duplicates()
    
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
        except Error as e:
            print(f"  警告: 县域 {row['区县']} 导入失败: {e}")
    
    connection.commit()
    print(f"✓ 成功导入 {count} 个县域基础信息")

def import_population_statistics(connection, df):
    """导入人口统计数据"""
    print("\n开始导入人口统计数据...")
    cursor = connection.cursor()
    
    insert_query = """
    INSERT INTO population_statistics 
    (county_code, year, total_households, rural_households, 
     total_population_万, rural_population_万, registered_population_万,
     population_density, urbanization_rate)
    VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
    ON DUPLICATE KEY UPDATE 
    total_households = VALUES(total_households),
    rural_households = VALUES(rural_households),
    total_population_万 = VALUES(total_population_万),
    rural_population_万 = VALUES(rural_population_万),
    registered_population_万 = VALUES(registered_population_万),
    population_density = VALUES(population_density),
    urbanization_rate = VALUES(urbanization_rate)
    """
    
    count = 0
    for _, row in df.iterrows():
        try:
            # 计算人口密度和城镇化率
            population_density = None
            urbanization_rate = None
            
            # 从县域基础信息获取土地面积
            cursor.execute("SELECT land_area_km2 FROM county_basic WHERE county_code = %s", (row['区县代码'],))
            land_result = cursor.fetchone()
            if land_result and land_result[0] and land_result[0] > 0:
                # 转换为float避免类型不兼容
                land_area = float(land_result[0])
                total_pop = float(row['年末总人口_万人']) if pd.notna(row['年末总人口_万人']) else 0
                if total_pop > 0:
                    population_density = (total_pop * 10000) / land_area
            
            # 计算城镇化率
            total_pop = float(row['年末总人口_万人']) if pd.notna(row['年末总人口_万人']) else 0
            rural_pop = float(row['乡村人口_万人']) if pd.notna(row['乡村人口_万人']) else 0
            if total_pop > 0:
                urbanization_rate = ((total_pop - rural_pop) / total_pop) * 100
            
            cursor.execute(insert_query, (
                row['区县代码'],
                row['年份'],
                row['年末总户数_户'],
                row['乡村户数_户'],
                row['年末总人口_万人'],
                row['乡村人口_万人'],
                row['户籍人口数_万人'],
                population_density,
                urbanization_rate
            ))
            count += 1
        except Error as e:
            print(f"  警告: 记录导入失败: {e}")
    
    connection.commit()
    print(f"✓ 成功导入 {count} 条人口统计记录")

def import_economic_aggregate(connection, df):
    """导入经济总量数据"""
    print("\n开始导入经济总量数据...")
    cursor = connection.cursor()
    
    insert_query = """
    INSERT INTO economic_aggregate 
    (county_code, year, gdp_万元, primary_industry_万元, secondary_industry_万元,
     industrial_value_万元, tertiary_industry_万元, agriculture_value_万元,
     livestock_value_万元, gdp_per_capita, industry_structure_1, 
     industry_structure_2, industry_structure_3)
    VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
    ON DUPLICATE KEY UPDATE 
    gdp_万元 = VALUES(gdp_万元),
    primary_industry_万元 = VALUES(primary_industry_万元),
    secondary_industry_万元 = VALUES(secondary_industry_万元),
    industrial_value_万元 = VALUES(industrial_value_万元),
    tertiary_industry_万元 = VALUES(tertiary_industry_万元),
    agriculture_value_万元 = VALUES(agriculture_value_万元),
    livestock_value_万元 = VALUES(livestock_value_万元),
    gdp_per_capita = VALUES(gdp_per_capita),
    industry_structure_1 = VALUES(industry_structure_1),
    industry_structure_2 = VALUES(industry_structure_2),
    industry_structure_3 = VALUES(industry_structure_3)
    """
    
    count = 0
    for _, row in df.iterrows():
        try:
            # 计算产业结构比重
            industry_1 = None
            industry_2 = None
            industry_3 = None
            
            gdp = float(row['地区生产总值_万元']) if pd.notna(row['地区生产总值_万元']) else 0
            if gdp > 0:
                primary = float(row['第一产业增加值_万元']) if pd.notna(row['第一产业增加值_万元']) else 0
                secondary = float(row['第二产业增加值_万元']) if pd.notna(row['第二产业增加值_万元']) else 0
                tertiary = float(row['第三产业增加值_万元']) if pd.notna(row['第三产业增加值_万元']) else 0
                
                industry_1 = (primary / gdp) * 100
                industry_2 = (secondary / gdp) * 100
                industry_3 = (tertiary / gdp) * 100
            
            cursor.execute(insert_query, (
                row['区县代码'],
                row['年份'],
                row['地区生产总值_万元'],
                row['第一产业增加值_万元'],
                row['第二产业增加值_万元'],
                row['工业增加值_万元'],
                row['第三产业增加值_万元'],
                row['农业增加值_万元'],
                row['牧业增加值_万元'],
                row['人均地区生产总值_元/人'],
                industry_1,
                industry_2,
                industry_3
            ))
            count += 1
        except Error as e:
            print(f"  警告: 记录导入失败: {e}")
    
    connection.commit()
    print(f"✓ 成功导入 {count} 条经济总量记录")

def main():
    """主函数"""
    print("="*60)
    print("县域风险预警系统 - 数据导入工具")
    print("="*60)
    
    # Excel文件路径（真正的县域数据）
    excel_file = '../data/中国县域数据库6.0版.xlsx'
    
    # 连接数据库
    connection = connect_db()
    
    try:
        # 加载并清洗数据
        df = load_excel_data(excel_file)
        df = clean_data(df)
        
        # 导入各类数据
        import_county_basic(connection, df)
        import_population_statistics(connection, df)
        import_economic_aggregate(connection, df)
        
        print("\n" + "="*60)
        print("✓ 数据导入完成！")
        print("="*60)
        
    except Exception as e:
        print(f"\n✗ 数据导入过程中发生错误: {e}")
        connection.rollback()
    finally:
        if connection.is_connected():
            connection.close()
            print("数据库连接已关闭")

if __name__ == "__main__":
    main()
