package com.county.risk.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预警规则实体类
 */
@Data
@TableName("alert_rules")
public class AlertRule implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "rule_id", type = IdType.AUTO)
    private Integer ruleId;

    private String ruleName;

    private String ruleCategory; // 经济预警, 社会预警, 环境预警, 综合预警

    private String ruleExpression;

    private String conditionsJson;

    private BigDecimal thresholdHigh;

    private BigDecimal thresholdMedium;

    private BigDecimal thresholdLow;

    private BigDecimal weight;

    private Integer priority;

    private String status; // 启用, 停用

    private String description;

    private Integer createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
