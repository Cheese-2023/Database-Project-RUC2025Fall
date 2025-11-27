package com.county.risk.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SystemOverviewDTO {
    private ServiceInfo serviceInfo;
    private Metrics metrics;
    private List<AlertSummary> recentAlerts;
    private List<DataIssueSummary> pendingIssues;

    @Data
    public static class ServiceInfo {
        private String name;
        private String version;
        private LocalDateTime startTime;
        private Long uptimeSeconds;
        private String javaVersion;
        private String environment;
    }

    @Data
    public static class Metrics {
        private Long totalCounties;
        private Long riskRecords;
        private Long alertCount;
        private Long pendingAlerts;
        private Long resolvedAlerts;
        private Long dataIssues;
        private Long unresolvedIssues;
    }

    @Data
    public static class AlertSummary {
        private Integer alertId;
        private String countyName;
        private String provinceName;
        private String riskLevel;
        private String alertType;
        private String status;
        private LocalDateTime createdAt;
        private String title;
    }

    @Data
    public static class DataIssueSummary {
        private Integer id;
        private String tableName;
        private String countyName;
        private String severity;
        private String status;
        private LocalDateTime detectedAt;
        private String issueDescription;
    }
}
