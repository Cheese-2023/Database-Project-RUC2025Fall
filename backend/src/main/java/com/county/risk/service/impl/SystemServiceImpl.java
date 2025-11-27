package com.county.risk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.county.risk.dto.SystemOverviewDTO;
import com.county.risk.entity.Alert;
import com.county.risk.entity.SystemConfig;
import com.county.risk.mapper.AlertMapper;
import com.county.risk.mapper.DataQualityCheckMapper;
import com.county.risk.mapper.RiskAssessmentMapper;
import com.county.risk.mapper.SystemConfigMapper;
import com.county.risk.service.SystemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SystemServiceImpl implements SystemService {

    private final RiskAssessmentMapper riskAssessmentMapper;
    private final AlertMapper alertMapper;
    private final DataQualityCheckMapper dataQualityCheckMapper;
    private final SystemConfigMapper systemConfigMapper;

    @Override
    public SystemOverviewDTO getSystemOverview() {
        SystemOverviewDTO dto = new SystemOverviewDTO();
        dto.setServiceInfo(buildServiceInfo());
        dto.setMetrics(buildMetrics());
        dto.setRecentAlerts(buildAlertSummaries());
        dto.setPendingIssues(buildIssueSummaries());
        return dto;
    }

    private SystemOverviewDTO.ServiceInfo buildServiceInfo() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        SystemOverviewDTO.ServiceInfo info = new SystemOverviewDTO.ServiceInfo();
        info.setName("县域风险预警系统");
        info.setVersion(resolveSystemVersion());
        info.setStartTime(LocalDateTime.ofInstant(
                Instant.ofEpochMilli(runtimeMXBean.getStartTime()), ZoneId.systemDefault()));
        info.setUptimeSeconds(runtimeMXBean.getUptime() / 1000);
        info.setJavaVersion(System.getProperty("java.version"));
        info.setEnvironment(System.getProperty("os.name"));
        return info;
    }

    private String resolveSystemVersion() {
        SystemConfig versionConfig = systemConfigMapper.selectOne(new LambdaQueryWrapper<SystemConfig>()
                .eq(SystemConfig::getConfigKey, "SYSTEM_VERSION")
                .last("LIMIT 1"));
        return versionConfig != null ? versionConfig.getConfigValue() : "1.0.0";
    }

    private SystemOverviewDTO.Metrics buildMetrics() {
        SystemOverviewDTO.Metrics metrics = new SystemOverviewDTO.Metrics();
        metrics.setTotalCounties(riskAssessmentMapper.countDistinctCounties());
        metrics.setRiskRecords(riskAssessmentMapper.selectCount(null));
        metrics.setAlertCount(alertMapper.selectCount(null));
        metrics.setPendingAlerts(alertMapper.selectCount(new LambdaQueryWrapper<Alert>()
                .in(Alert::getStatus, List.of("新建", "处理中"))));
        metrics.setResolvedAlerts(alertMapper.selectCount(new LambdaQueryWrapper<Alert>()
                .in(Alert::getStatus, List.of("已确认", "已处理", "已关闭"))));
        metrics.setDataIssues(dataQualityCheckMapper.selectCount(null));
        metrics.setUnresolvedIssues(dataQualityCheckMapper.countUnresolvedIssues());
        return metrics;
    }

    private List<SystemOverviewDTO.AlertSummary> buildAlertSummaries() {
        List<Map<String, Object>> rows = alertMapper.selectRecentAlerts(5);
        if (CollectionUtils.isEmpty(rows)) {
            return List.of();
        }
        return rows.stream().map(row -> {
            SystemOverviewDTO.AlertSummary summary = new SystemOverviewDTO.AlertSummary();
            summary.setAlertId(asInteger(row.get("alert_id")));
            summary.setCountyName(asString(row.get("county_name")));
            summary.setProvinceName(asString(row.get("province_name")));
            summary.setRiskLevel(asString(row.get("risk_level")));
            summary.setAlertType(asString(row.get("alert_type")));
            summary.setStatus(asString(row.get("status")));
            summary.setTitle(asString(row.get("title")));
            summary.setCreatedAt(toLocalDateTime(row.get("created_at")));
            return summary;
        }).collect(Collectors.toList());
    }

    private List<SystemOverviewDTO.DataIssueSummary> buildIssueSummaries() {
        List<Map<String, Object>> rows = dataQualityCheckMapper.selectPendingIssues(5);
        if (CollectionUtils.isEmpty(rows)) {
            return List.of();
        }
        return rows.stream().map(row -> {
            SystemOverviewDTO.DataIssueSummary summary = new SystemOverviewDTO.DataIssueSummary();
            summary.setId(asInteger(row.get("id")));
            summary.setTableName(asString(row.get("table_name")));
            summary.setCountyName(asString(row.get("county_name")));
            summary.setSeverity(asString(row.get("severity")));
            summary.setStatus(asString(row.get("status")));
            summary.setIssueDescription(asString(row.get("issue_description")));
            summary.setDetectedAt(toLocalDateTime(row.get("detected_at")));
            return summary;
        }).collect(Collectors.toList());
    }

    private Integer asInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return null;
    }

    private String asString(Object value) {
        return value != null ? String.valueOf(value) : null;
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        if (value instanceof java.util.Date date) {
            return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        }
        return null;
    }
}
