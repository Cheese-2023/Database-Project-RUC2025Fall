package com.county.risk.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 综合风险评估实体类
 * 对应表: comprehensive_risk_assessment
 */
@Data
@TableName("comprehensive_risk_assessment")
public class ComprehensiveRiskAssessment {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 县域代码
     */
    private String countyCode;
    
    /**
     * 年份
     */
    private Integer year;
    
    /**
     * 经济风险得分
     */
    private BigDecimal economicRiskScore;
    
    /**
     * 社会风险得分
     */
    private BigDecimal socialRiskScore;
    
    /**
     * 环境风险得分
     */
    private BigDecimal environmentRiskScore;
    
    /**
     * 治理风险得分
     */
    private BigDecimal governanceRiskScore;
    
    /**
     * 发展风险得分
     */
    private BigDecimal developmentRiskScore;
    
    /**
     * 综合风险得分
     */
    private BigDecimal comprehensiveRiskScore;
    
    /**
     * 风险等级: 低风险、中低风险、中风险、中高风险、高风险
     */
    private String riskLevel;
    
    /**
     * 风险趋势: 改善、稳定、恶化
     */
    private String riskTrend;
    
    /**
     * 主要风险因素
     */
    private String majorRiskFactors;
    
    /**
     * 评估日期
     */
    private LocalDateTime assessmentDate;
}
