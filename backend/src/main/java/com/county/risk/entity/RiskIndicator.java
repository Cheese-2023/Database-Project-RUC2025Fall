package com.county.risk.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 风险指标定义实体
 */
@Data
@TableName("risk_indicators")
public class RiskIndicator implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer indicatorId;
    private String indicatorCode;
    private String indicatorName;
    private String category;
    private String subcategory;
    private String calculationMethod;
    private String dataSource;
    private BigDecimal weight;
    private BigDecimal thresholdHigh;
    private BigDecimal thresholdMedium;
    private BigDecimal thresholdLow;
    private String unit; // 单位，如 %, 万元, 指数
    private String comparisonOperator; // 比较操作符: GT (大于阈值风险高), LT (小于阈值风险高)
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
