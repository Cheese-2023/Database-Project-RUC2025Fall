package com.county.risk.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据质量检查实体类
 */
@Data
@TableName("data_quality_checks")
public class DataQualityCheck implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String tableName;

    private String countyCode;

    private Integer year;

    private String checkType; // 完整性, 一致性, 准确性, 时效性, 有效性

    private String fieldName;

    private String issueDescription;

    private String expectedValue;

    private String actualValue;

    private String severity; // 严重, 中等, 轻微

    private String status; // 待处理, 处理中, 已解决, 已忽略

    private LocalDateTime detectedAt;

    private Integer detectedBy;

    private LocalDateTime resolvedAt;

    private Integer resolvedBy;

    private String resolutionComment;
}
