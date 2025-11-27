package com.county.risk.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 预警记录实体类
 */
@Data
@TableName("alerts")
public class Alert implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "alert_id", type = IdType.AUTO)
    private Integer alertId;

    private String countyCode;

    private Integer year;

    private Integer riskScoreId;

    private String riskLevel; // GREEN, YELLOW, ORANGE, RED

    private String alertType; // 经济预警, 社会预警, 环境预警, 综合预警

    private String triggeredRules;

    private String title;

    private String description;

    private String recommendation;

    private String severity; // 严重, 中等, 轻微

    private String status; // 新建, 已确认, 处理中, 已处理, 已关闭

    private LocalDateTime createdAt;

    private Integer confirmedBy;

    private LocalDateTime confirmedAt;

    private Integer handledBy;

    private LocalDateTime handledAt;

    private Integer closedBy;

    private LocalDateTime closedAt;

    private String reviewComment;
}
