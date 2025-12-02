package com.county.risk.service;

import java.util.List;
import java.util.Map;

/**
 * 脱贫攻坚成果展示服务接口
 */
public interface PovertyAchievementService {
    
    /**
     * 获取贫困县统计概览
     */
    Map<String, Object> getOverview();
    
    /**
     * 按省份统计贫困县数量
     */
    List<Map<String, Object>> getStatisticsByProvince();
    
    /**
     * 按摘帽年份统计
     */
    List<Map<String, Object>> getStatisticsByDelistingYear();
    
    /**
     * 获取摘帽前后经济指标对比
     */
    Map<String, Object> getDelistingComparison(Integer delistingYear);
    
    /**
     * 获取贫困县列表
     */
    List<Map<String, Object>> getPovertyCountyList(String province, Integer delistingYear);
    
    /**
     * 获取单个贫困县详情
     */
    Map<String, Object> getCountyDetail(String countyCode);
    
    /**
     * 获取经济指标趋势
     */
    Map<String, Object> getEconomicTrend(String indicatorType);
}


