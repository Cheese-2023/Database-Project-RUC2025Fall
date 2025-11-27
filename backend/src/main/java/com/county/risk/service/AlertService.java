package com.county.risk.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.county.risk.entity.Alert;
import com.county.risk.entity.AlertRule;

import java.util.Map;

public interface AlertService extends IService<Alert> {

    // 预警规则相关
    IPage<AlertRule> getAlertRules(Page<AlertRule> page);

    boolean saveAlertRule(AlertRule rule);

    boolean deleteAlertRule(Integer id);

    // 预警记录相关
    IPage<Map<String, Object>> getAlerts(Page<Alert> page);

    boolean confirmAlert(Integer id, Integer userId);

    // 预警检查
    int checkAndGenerateAlerts(Integer year);
}
