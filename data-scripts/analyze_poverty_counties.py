#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
贫困县数据分析脚本

功能：
1. 从"ARIMA填补(慎用)"表中读取县域数据
2. 从"832个国家级贫困县摘帽情况分省分年统计.xlsx"中筛选贫困县
3. 对832个贫困县进行统计分析
4. 生成分析报告

使用方法：
python3 analyze_poverty_counties.py
"""

import pandas as pd
import numpy as np
import os
import sys
from datetime import datetime

# 数据文件路径
ARIMA_DATA_FILE = '../data/中国县域数据库6.0版.xlsx'
POVERTY_DATA_FILE = '../data/832个国家级贫困县摘帽情况分省分年统计.xlsx'
OUTPUT_DIR = '../data/poverty_analysis_output'

def create_output_dir():
    """创建输出目录"""
    if not os.path.exists(OUTPUT_DIR):
        os.makedirs(OUTPUT_DIR)
        print(f"✓ 创建输出目录: {OUTPUT_DIR}")

def load_arima_data():
    """加载ARIMA填补的数据表"""
    print("\n[1] 正在加载ARIMA填补数据表...")
    try:
        df = pd.read_excel(ARIMA_DATA_FILE, engine='openpyxl', sheet_name='ARIMA填补(慎用)')
        print(f"✓ 成功加载数据，共 {len(df)} 行，{len(df.columns)} 列")
        print(f"  列名: {list(df.columns[:10])}...")  # 显示前10列
        return df
    except Exception as e:
        print(f"✗ 加载ARIMA数据失败: {e}")
        sys.exit(1)

def load_poverty_data():
    """加载贫困县摘帽情况数据"""
    print("\n[2] 正在加载贫困县摘帽情况数据...")
    try:
        # 先查看所有sheet名称
        excel_file = pd.ExcelFile(POVERTY_DATA_FILE, engine='openpyxl')
        print(f"  可用sheet: {excel_file.sheet_names}")
        
        # 尝试读取第一个sheet
        df = pd.read_excel(POVERTY_DATA_FILE, engine='openpyxl', sheet_name=0)
        print(f"✓ 成功加载数据，共 {len(df)} 行，{len(df.columns)} 列")
        print(f"  列名: {list(df.columns)}")
        return df
    except Exception as e:
        print(f"✗ 加载贫困县数据失败: {e}")
        sys.exit(1)

def normalize_county_name(name):
    """标准化县名，用于匹配"""
    if pd.isna(name):
        return ""
    name = str(name).strip()
    # 移除常见的后缀差异
    name = name.replace("县", "").replace("区", "").replace("市", "")
    return name

def extract_poverty_county_codes(poverty_df, arima_df):
    """从贫困县数据中提取县代码列表"""
    print("\n[3] 正在提取贫困县代码...")
    
    # 显示贫困县数据的前几行，帮助理解数据结构
    print("\n贫困县数据前5行:")
    print(poverty_df.head())
    
    poverty_counties = set()
    poverty_county_names = set()
    
    # 确定贫困县表中的县名列
    poverty_name_col = None
    for col in ['贫困县', '县名', '区县', '县', '县域名称', '名称']:
        if col in poverty_df.columns:
            poverty_name_col = col
            break
    
    if not poverty_name_col:
        print("✗ 无法找到贫困县名称列")
        return poverty_counties
    
    # 获取所有贫困县名称
    poverty_names = set(poverty_df[poverty_name_col].dropna().astype(str).unique())
    print(f"  找到 {len(poverty_names)} 个贫困县名称")
    print(f"  示例: {list(poverty_names)[:5]}")
    
    # 确定ARIMA表中的县名列和代码列
    arima_name_col = None
    arima_code_col = None
    
    for col in ['区县', '县名', '县', '县域名称']:
        if col in arima_df.columns:
            arima_name_col = col
            break
    
    for col in ['区县代码', '县代码', '代码']:
        if col in arima_df.columns:
            arima_code_col = col
            break
    
    if not arima_name_col or not arima_code_col:
        print(f"✗ ARIMA表中缺少必要列: 县名列={arima_name_col}, 代码列={arima_code_col}")
        return poverty_counties
    
    print(f"  使用ARIMA表的列: 县名={arima_name_col}, 代码={arima_code_col}")
    
    # 方法1: 直接匹配（完全匹配）
    matched_direct = arima_df[arima_df[arima_name_col].isin(poverty_names)]
    if len(matched_direct) > 0:
        matched_codes = set(matched_direct[arima_code_col].dropna().astype(str).unique())
        matched_names = set(matched_direct[arima_name_col].dropna().unique())
        poverty_counties.update(matched_codes)
        poverty_county_names.update(matched_names)
        print(f"  直接匹配成功: {len(matched_codes)} 个县")
    
    # 方法2: 模糊匹配（处理名称差异）
    unmatched_poverty = poverty_names - poverty_county_names
    if len(unmatched_poverty) > 0:
        print(f"  尝试模糊匹配剩余的 {len(unmatched_poverty)} 个县...")
        
        # 创建标准化名称映射
        arima_normalized = {}
        for idx, row in arima_df.iterrows():
            name = str(row[arima_name_col]) if pd.notna(row[arima_name_col]) else ""
            code = str(row[arima_code_col]) if pd.notna(row[arima_code_col]) else ""
            normalized = normalize_county_name(name)
            if normalized and code:
                arima_normalized[normalized] = (name, code)
        
        # 尝试匹配
        fuzzy_matched = 0
        for poverty_name in unmatched_poverty:
            normalized_poverty = normalize_county_name(poverty_name)
            if normalized_poverty in arima_normalized:
                matched_name, matched_code = arima_normalized[normalized_poverty]
                poverty_counties.add(matched_code)
                poverty_county_names.add(matched_name)
                fuzzy_matched += 1
        
        if fuzzy_matched > 0:
            print(f"  模糊匹配成功: {fuzzy_matched} 个县")
    
    # 方法3: 部分匹配（包含关系）
    still_unmatched = poverty_names - poverty_county_names
    if len(still_unmatched) > 0:
        print(f"  尝试部分匹配剩余的 {len(still_unmatched)} 个县...")
        
        partial_matched = 0
        for poverty_name in still_unmatched:
            # 移除"县"、"自治县"等后缀
            base_name = str(poverty_name).replace("县", "").replace("自治县", "").replace("区", "").replace("市", "")
            
            # 在ARIMA表中查找包含该名称的县
            for idx, row in arima_df.iterrows():
                arima_name = str(row[arima_name_col]) if pd.notna(row[arima_name_col]) else ""
                if base_name in arima_name or arima_name in base_name:
                    code = str(row[arima_code_col]) if pd.notna(row[arima_code_col]) else ""
                    if code:
                        poverty_counties.add(code)
                        poverty_county_names.add(arima_name)
                        partial_matched += 1
                        break
        
        if partial_matched > 0:
            print(f"  部分匹配成功: {partial_matched} 个县")
    
    print(f"\n✓ 总共匹配到 {len(poverty_counties)} 个贫困县代码")
    print(f"  匹配到的县名数量: {len(poverty_county_names)}")
    
    if len(poverty_counties) > 0:
        print(f"  示例代码: {list(poverty_counties)[:5]}")
    
    # 显示未匹配的县
    unmatched = poverty_names - poverty_county_names
    if len(unmatched) > 0:
        print(f"\n⚠️  未匹配的贫困县 ({len(unmatched)} 个):")
        print(f"  示例: {list(unmatched)[:10]}")
    
    return poverty_counties

def create_county_name_mapping(arima_df, poverty_df):
    """创建县名到县代码的映射"""
    print("\n[3.5] 创建县名映射...")
    
    # 确定列名
    arima_name_col = None
    arima_code_col = None
    poverty_name_col = None
    
    for col in ['区县', '县名', '县']:
        if col in arima_df.columns:
            arima_name_col = col
            break
    
    for col in ['区县代码', '县代码', '代码']:
        if col in arima_df.columns:
            arima_code_col = col
            break
    
    for col in ['贫困县', '县名', '区县', '县']:
        if col in poverty_df.columns:
            poverty_name_col = col
            break
    
    if not all([arima_name_col, arima_code_col, poverty_name_col]):
        return {}
    
    # 创建映射
    name_to_code = {}
    for idx, row in arima_df.iterrows():
        name = str(row[arima_name_col]) if pd.notna(row[arima_name_col]) else ""
        code = str(row[arima_code_col]) if pd.notna(row[arima_code_col]) else ""
        if name and code:
            name_to_code[name] = code
    
    def extract_year(value):
        """从摘帽时间中提取年份"""
        if pd.isna(value):
            return None
        value_str = str(value)
        # 尝试提取4位数字年份
        import re
        year_match = re.search(r'20\d{2}', value_str)
        if year_match:
            try:
                return int(year_match.group())
            except:
                return None
        # 尝试直接转换为整数
        try:
            return int(float(value_str))
        except:
            return None
    
    # 创建贫困县名到代码的映射（包括摘帽年份）
    poverty_info = {}
    for idx, row in poverty_df.iterrows():
        poverty_name = str(row[poverty_name_col]) if pd.notna(row[poverty_name_col]) else ""
        delisting_year = extract_year(row.get('摘帽时间', None))
        
        if poverty_name:
            # 尝试直接匹配
            if poverty_name in name_to_code:
                code = name_to_code[poverty_name]
                poverty_info[code] = {
                    'name': poverty_name,
                    'delisting_year': delisting_year
                }
            else:
                # 尝试模糊匹配
                normalized_poverty = normalize_county_name(poverty_name)
                for name, code in name_to_code.items():
                    if normalize_county_name(name) == normalized_poverty:
                        poverty_info[code] = {
                            'name': name,
                            'delisting_year': delisting_year
                        }
                        break
    
    print(f"✓ 创建了 {len(poverty_info)} 个贫困县的信息映射")
    return poverty_info

def filter_poverty_counties(arima_df, poverty_counties, poverty_info=None):
    """筛选出贫困县的数据"""
    print("\n[4] 正在筛选贫困县数据...")
    
    # 确定县代码列名
    code_col = None
    for col in ['区县代码', '县代码', '代码']:
        if col in arima_df.columns:
            code_col = col
            break
    
    if not code_col:
        print("✗ 无法找到县代码列")
        return None, None
    
    # 筛选贫困县
    poverty_df = arima_df[arima_df[code_col].astype(str).isin(poverty_counties)].copy()
    
    # 如果有贫困县信息，添加摘帽年份
    if poverty_info:
        poverty_df['摘帽年份'] = poverty_df[code_col].astype(str).map(
            lambda x: poverty_info.get(x, {}).get('delisting_year', None)
        )
    
    print(f"✓ 筛选出 {len(poverty_df)} 条贫困县数据记录")
    
    return poverty_df, code_col

def analyze_poverty_counties(poverty_df, code_col):
    """对贫困县数据进行统计分析"""
    print("\n[5] 正在进行统计分析...")
    
    analysis_results = {}
    
    # 1. 基本信息统计
    analysis_results['基本信息'] = {
        '贫困县总数': len(poverty_df[code_col].unique()) if code_col else 0,
        '数据记录总数': len(poverty_df),
        '数据年份范围': f"{poverty_df['年份'].min()}-{poverty_df['年份'].max()}" if '年份' in poverty_df.columns else "未知"
    }
    
    # 2. 按年份统计
    if '年份' in poverty_df.columns:
        year_stats = poverty_df.groupby('年份').agg({
            code_col: 'nunique'  # 每年有多少个贫困县有数据
        }).rename(columns={code_col: '贫困县数量'})
        analysis_results['按年份统计'] = year_stats
    
    # 3. 按省份统计
    if '省份' in poverty_df.columns:
        province_stats = poverty_df.groupby('省份').agg({
            code_col: 'nunique'
        }).rename(columns={code_col: '贫困县数量'}).sort_values('贫困县数量', ascending=False)
        analysis_results['按省份统计'] = province_stats
    
    # 3.5 按摘帽年份统计
    if '摘帽年份' in poverty_df.columns:
        delisting_stats = poverty_df.groupby('摘帽年份').agg({
            code_col: 'nunique'
        }).rename(columns={code_col: '摘帽县数量'}).sort_index()
        analysis_results['按摘帽年份统计'] = delisting_stats
        print(f"  找到摘帽年份信息: {delisting_stats.sum().values[0]} 个县有摘帽年份")
    
    # 3.6 摘帽前后对比分析
    if '摘帽年份' in poverty_df.columns and '年份' in poverty_df.columns:
        print("  正在进行摘帽前后对比分析...")
        delisting_comparison = analyze_delisting_comparison(poverty_df, code_col)
        if delisting_comparison:
            analysis_results['摘帽前后对比'] = delisting_comparison
    
    # 4. 经济指标分析（如果存在）
    economic_cols = [col for col in poverty_df.columns if any(keyword in col for keyword in ['GDP', 'gdp', '生产总值', '收入', '财政'])]
    if economic_cols:
        print(f"  找到经济相关指标: {economic_cols[:5]}...")
        analysis_results['经济指标'] = {}
        for col in economic_cols[:10]:  # 只分析前10个
            if poverty_df[col].dtype in [np.float64, np.int64]:
                analysis_results['经济指标'][col] = {
                    '平均值': float(poverty_df[col].mean()),
                    '中位数': float(poverty_df[col].median()),
                    '最大值': float(poverty_df[col].max()),
                    '最小值': float(poverty_df[col].min()),
                    '标准差': float(poverty_df[col].std())
                }
    
    # 5. 人口指标分析
    population_cols = [col for col in poverty_df.columns if any(keyword in col for keyword in ['人口', 'population'])]
    if population_cols:
        print(f"  找到人口相关指标: {population_cols[:5]}...")
        analysis_results['人口指标'] = {}
        for col in population_cols[:10]:
            if poverty_df[col].dtype in [np.float64, np.int64]:
                analysis_results['人口指标'][col] = {
                    '平均值': float(poverty_df[col].mean()),
                    '中位数': float(poverty_df[col].median()),
                    '最大值': float(poverty_df[col].max()),
                    '最小值': float(poverty_df[col].min())
                }
    
    print("✓ 统计分析完成")
    return analysis_results

def analyze_delisting_comparison(poverty_df, code_col):
    """分析摘帽前后的经济指标变化"""
    comparison_results = {}
    
    # 筛选有摘帽年份的县
    counties_with_delisting = poverty_df[poverty_df['摘帽年份'].notna()][code_col].unique()
    
    if len(counties_with_delisting) == 0:
        return None
    
    # 主要经济指标
    key_indicators = [
        '地区生产总值_万元',
        '人均地区生产总值_元/人',
        '地方财政一般预算收入_万元',
        '城镇居民人均可支配收入_元',
        '农村居民人均可支配收入_元'
    ]
    
    # 只分析存在的指标
    available_indicators = [col for col in key_indicators if col in poverty_df.columns]
    
    if len(available_indicators) == 0:
        return None
    
    comparison_data = []
    
    for county_code in counties_with_delisting[:100]:  # 限制分析前100个县以提高速度
        county_data = poverty_df[poverty_df[code_col] == county_code].copy()
        delisting_year = county_data['摘帽年份'].iloc[0]
        
        if pd.isna(delisting_year):
            continue
        
        delisting_year = int(delisting_year)
        
        # 摘帽前3年平均值
        before_data = county_data[
            (county_data['年份'] >= delisting_year - 3) & 
            (county_data['年份'] < delisting_year)
        ]
        
        # 摘帽后3年平均值
        after_data = county_data[
            (county_data['年份'] > delisting_year) & 
            (county_data['年份'] <= delisting_year + 3)
        ]
        
        if len(before_data) == 0 or len(after_data) == 0:
            continue
        
        row = {'县代码': county_code, '摘帽年份': delisting_year}
        
        for indicator in available_indicators:
            before_mean = before_data[indicator].mean()
            after_mean = after_data[indicator].mean()
            
            if pd.notna(before_mean) and pd.notna(after_mean) and before_mean > 0:
                growth_rate = ((after_mean - before_mean) / before_mean) * 100
                row[f'{indicator}_摘帽前均值'] = before_mean
                row[f'{indicator}_摘帽后均值'] = after_mean
                row[f'{indicator}_增长率%'] = growth_rate
        
        comparison_data.append(row)
    
    if len(comparison_data) > 0:
        comparison_df = pd.DataFrame(comparison_data)
        
        # 计算平均增长率
        summary = {}
        for indicator in available_indicators:
            growth_col = f'{indicator}_增长率%'
            if growth_col in comparison_df.columns:
                summary[indicator] = {
                    '平均增长率(%)': float(comparison_df[growth_col].mean()),
                    '中位数增长率(%)': float(comparison_df[growth_col].median()),
                    '摘帽前平均': float(comparison_df[f'{indicator}_摘帽前均值'].mean()),
                    '摘帽后平均': float(comparison_df[f'{indicator}_摘帽后均值'].mean())
                }
        
        comparison_results['摘要'] = summary
        comparison_results['详细数据'] = comparison_df
    
    return comparison_results

def save_results(poverty_df, code_col, analysis_results, poverty_counties):
    """保存分析结果"""
    print("\n[6] 正在保存分析结果...")
    
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    
    # 1. 保存筛选后的贫困县数据
    output_file = os.path.join(OUTPUT_DIR, f'poverty_counties_data_{timestamp}.xlsx')
    poverty_df.to_excel(output_file, index=False, engine='openpyxl')
    print(f"✓ 贫困县数据已保存: {output_file}")
    
    # 2. 保存贫困县代码列表
    codes_file = os.path.join(OUTPUT_DIR, f'poverty_county_codes_{timestamp}.txt')
    with open(codes_file, 'w', encoding='utf-8') as f:
        for code in sorted(poverty_counties):
            f.write(f"{code}\n")
    print(f"✓ 贫困县代码列表已保存: {codes_file}")
    
    # 3. 保存分析报告
    report_file = os.path.join(OUTPUT_DIR, f'poverty_analysis_report_{timestamp}.txt')
    with open(report_file, 'w', encoding='utf-8') as f:
        f.write("="*60 + "\n")
        f.write("贫困县数据分析报告\n")
        f.write("="*60 + "\n")
        f.write(f"生成时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n\n")
        
        # 基本信息
        if '基本信息' in analysis_results:
            f.write("【基本信息】\n")
            for key, value in analysis_results['基本信息'].items():
                f.write(f"  {key}: {value}\n")
            f.write("\n")
        
        # 按年份统计
        if '按年份统计' in analysis_results:
            f.write("【按年份统计】\n")
            f.write(str(analysis_results['按年份统计']))
            f.write("\n\n")
        
        # 按省份统计
        if '按省份统计' in analysis_results:
            f.write("【按省份统计】\n")
            f.write(str(analysis_results['按省份统计']))
            f.write("\n\n")
        
        # 按摘帽年份统计
        if '按摘帽年份统计' in analysis_results:
            f.write("【按摘帽年份统计】\n")
            f.write(str(analysis_results['按摘帽年份统计']))
            f.write("\n\n")
        
        # 摘帽前后对比
        if '摘帽前后对比' in analysis_results:
            f.write("【摘帽前后对比分析】\n")
            if '摘要' in analysis_results['摘帽前后对比']:
                for indicator, stats in analysis_results['摘帽前后对比']['摘要'].items():
                    f.write(f"\n  {indicator}:\n")
                    for stat_name, stat_value in stats.items():
                        f.write(f"    {stat_name}: {stat_value}\n")
            f.write("\n")
        
        # 经济指标
        if '经济指标' in analysis_results:
            f.write("【经济指标分析】\n")
            for col, stats in analysis_results['经济指标'].items():
                f.write(f"\n  {col}:\n")
                for stat_name, stat_value in stats.items():
                    f.write(f"    {stat_name}: {stat_value}\n")
            f.write("\n")
        
        # 人口指标
        if '人口指标' in analysis_results:
            f.write("【人口指标分析】\n")
            for col, stats in analysis_results['人口指标'].items():
                f.write(f"\n  {col}:\n")
                for stat_name, stat_value in stats.items():
                    f.write(f"    {stat_name}: {stat_value}\n")
            f.write("\n")
    
    print(f"✓ 分析报告已保存: {report_file}")
    
    # 4. 保存Excel格式的分析报告
    excel_report_file = os.path.join(OUTPUT_DIR, f'poverty_analysis_report_{timestamp}.xlsx')
    with pd.ExcelWriter(excel_report_file, engine='openpyxl') as writer:
        # 基本信息
        if '基本信息' in analysis_results:
            pd.DataFrame([analysis_results['基本信息']]).to_excel(writer, sheet_name='基本信息', index=False)
        
        # 按年份统计
        if '按年份统计' in analysis_results:
            analysis_results['按年份统计'].to_excel(writer, sheet_name='按年份统计')
        
        # 按省份统计
        if '按省份统计' in analysis_results:
            analysis_results['按省份统计'].to_excel(writer, sheet_name='按省份统计')
        
        # 按摘帽年份统计
        if '按摘帽年份统计' in analysis_results:
            analysis_results['按摘帽年份统计'].to_excel(writer, sheet_name='按摘帽年份统计')
        
        # 摘帽前后对比
        if '摘帽前后对比' in analysis_results:
            if '摘要' in analysis_results['摘帽前后对比']:
                delisting_summary = []
                for indicator, stats in analysis_results['摘帽前后对比']['摘要'].items():
                    row = {'指标名称': indicator}
                    row.update(stats)
                    delisting_summary.append(row)
                pd.DataFrame(delisting_summary).to_excel(writer, sheet_name='摘帽前后对比摘要', index=False)
            
            if '详细数据' in analysis_results['摘帽前后对比']:
                analysis_results['摘帽前后对比']['详细数据'].to_excel(writer, sheet_name='摘帽前后对比详细', index=False)
        
        # 经济指标汇总
        if '经济指标' in analysis_results:
            econ_summary = []
            for col, stats in analysis_results['经济指标'].items():
                row = {'指标名称': col}
                row.update(stats)
                econ_summary.append(row)
            pd.DataFrame(econ_summary).to_excel(writer, sheet_name='经济指标', index=False)
        
        # 人口指标汇总
        if '人口指标' in analysis_results:
            pop_summary = []
            for col, stats in analysis_results['人口指标'].items():
                row = {'指标名称': col}
                row.update(stats)
                pop_summary.append(row)
            pd.DataFrame(pop_summary).to_excel(writer, sheet_name='人口指标', index=False)
    
    print(f"✓ Excel分析报告已保存: {excel_report_file}")

def main():
    print("="*60)
    print("贫困县数据分析脚本")
    print("="*60)
    
    # 创建输出目录
    create_output_dir()
    
    # 加载数据
    arima_df = load_arima_data()
    poverty_df = load_poverty_data()
    
    # 提取贫困县代码
    poverty_counties = extract_poverty_county_codes(poverty_df, arima_df)
    
    if len(poverty_counties) == 0:
        print("\n⚠️  警告: 未能提取到贫困县代码，请检查数据文件格式")
        print("请手动检查贫困县数据文件的结构，并修改脚本中的匹配逻辑")
        return
    
    # 创建县名映射（包含摘帽年份信息）
    poverty_info = create_county_name_mapping(arima_df, poverty_df)
    
    # 筛选贫困县数据
    filtered_data, code_col = filter_poverty_counties(arima_df, poverty_counties, poverty_info)
    
    if filtered_data is None or len(filtered_data) == 0:
        print("\n⚠️  警告: 未能筛选出贫困县数据")
        return
    
    # 统计分析
    analysis_results = analyze_poverty_counties(filtered_data, code_col)
    
    # 保存结果
    save_results(filtered_data, code_col, analysis_results, poverty_counties)
    
    print("\n" + "="*60)
    print("✓ 分析完成！")
    print(f"结果已保存到: {OUTPUT_DIR}")
    print("="*60)

if __name__ == '__main__':
    main()

