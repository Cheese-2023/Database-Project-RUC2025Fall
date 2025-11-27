package com.county.risk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.county.risk.entity.ComprehensiveRiskAssessment;
import com.county.risk.mapper.RiskAssessmentMapper;
import com.county.risk.service.RiskService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 风险评估服务实现类
 */
@Service
public class RiskServiceImpl extends ServiceImpl<RiskAssessmentMapper, ComprehensiveRiskAssessment> 
        implements RiskService {
    
    @Override
    public List<ComprehensiveRiskAssessment> getRiskByLevel(String level) {
        LambdaQueryWrapper<ComprehensiveRiskAssessment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ComprehensiveRiskAssessment::getRiskLevel, level)
               .orderByDesc(ComprehensiveRiskAssessment::getComprehensiveRiskScore);
        return list(wrapper);
    }
    
    @Override
    public ComprehensiveRiskAssessment getCountyRisk(String countyCode, Integer year) {
        LambdaQueryWrapper<ComprehensiveRiskAssessment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ComprehensiveRiskAssessment::getCountyCode, countyCode);
        
        if (year != null) {
            wrapper.eq(ComprehensiveRiskAssessment::getYear, year);
        } else {
            // 如果不指定年份，获取最新年份的数据
            wrapper.orderByDesc(ComprehensiveRiskAssessment::getYear)
                   .last("LIMIT 1");
        }
        
        return getOne(wrapper);
    }
    
    @Override
    public Map<String, Object> getRiskStatistics() {
        return baseMapper.getRiskStatistics();
    }
    
    @Override
    public List<Map<String, Object>> getRiskTrend(String countyCode) {
        return baseMapper.getRiskTrendByCounty(countyCode);
    }
    
    @Override
    public List<Map<String, Object>> getAverageRiskTrend() {
        return baseMapper.getAverageRiskTrend();
    }
    
    @Override
    public List<Map<String, Object>> getTopRiskCounties(int limit) {
        return baseMapper.getTopRiskCounties(limit);
    }
    
    @Override
    public List<Map<String, Object>> getRiskListWithCountyInfo(String level, String provinceName, Integer year) {
        Integer targetYear = year;
        if (targetYear == null) {
            targetYear = baseMapper.getLatestAssessmentYear();
        }
        if (targetYear == null) {
            return List.of();
        }
        return baseMapper.getRiskListWithCountyInfo(level, provinceName, targetYear);
    }
}
