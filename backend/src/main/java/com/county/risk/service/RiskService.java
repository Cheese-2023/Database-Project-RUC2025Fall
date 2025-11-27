package com.county.risk.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.county.risk.entity.ComprehensiveRiskAssessment;

import java.util.List;
import java.util.Map;

/**
 * 风险评估服务接口
 */
public interface RiskService extends IService<ComprehensiveRiskAssessment> {
    
    /**
     * 按风险等级查询
     * @param level 风险等级
     * @return 风险评估列表
     */
    List<ComprehensiveRiskAssessment> getRiskByLevel(String level);
    
    /**
     * 查询单个县域的风险评估
     * @param countyCode 县域代码
     * @param year 年份（可选）
     * @return 风险评估对象
     */
    ComprehensiveRiskAssessment getCountyRisk(String countyCode, Integer year);
    
    /**
     * 获取风险统计信息
     * @return 统计结果
     */
    Map<String, Object> getRiskStatistics();
    
    /**
     * 获取县域风险趋势
     * @param countyCode 县域代码
     * @return 历年风险数据
     */
    List<Map<String, Object>> getRiskTrend(String countyCode);
    
    /**
     * 获取平均风险趋势
     * @return 历年平均风险数据
     */
    List<Map<String, Object>> getAverageRiskTrend();
    
    /**
     * 获取高风险县域TOP N
     * @param limit 返回数量
     * @return 高风险县域列表
     */
    List<Map<String, Object>> getTopRiskCounties(int limit);
    
    /**
     * 获取带县域信息的风险评估列表
     * @param level 风险等级（可选）
     * @param provinceCode 省份代码（可选）
     * @param year 年份（可选）
     * @return 风险评估列表（含县域名称等信息）
     */
    List<Map<String, Object>> getRiskListWithCountyInfo(String level, String provinceName, Integer year);
}
