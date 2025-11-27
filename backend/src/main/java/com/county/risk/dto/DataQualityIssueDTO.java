package com.county.risk.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据质量问题视图
 */
@Data
public class DataQualityIssueDTO {
    private Integer id;
    private String tableName;
    private String countyCode;
    private Integer year;
    private String countyName;
    private String provinceName;
    private String checkType;
    private String fieldName;
    private String issueDescription;
    private String expectedValue;
    private String actualValue;
    private String severity;
    private String status;
    private LocalDateTime detectedAt;
    private LocalDateTime resolvedAt;
    private String resolutionComment;
}
