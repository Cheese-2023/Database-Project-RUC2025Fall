package com.county.risk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.county.risk.entity.Alert;
import com.county.risk.entity.AlertRule;
import com.county.risk.entity.ComprehensiveRiskAssessment;
import com.county.risk.entity.FiscalFinance;
import com.county.risk.entity.EnvironmentCulture;
import com.county.risk.mapper.AlertMapper;
import com.county.risk.mapper.AlertRuleMapper;
import com.county.risk.mapper.RiskAssessmentMapper;
import com.county.risk.mapper.FiscalFinanceMapper;
import com.county.risk.mapper.EnvironmentCultureMapper;
import com.county.risk.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AlertServiceImpl extends ServiceImpl<AlertMapper, Alert> implements AlertService {

    @Autowired
    private AlertRuleMapper alertRuleMapper;

    @Autowired
    private AlertMapper alertMapper;

    @Autowired
    private RiskAssessmentMapper riskAssessmentMapper;

    @Autowired
    private FiscalFinanceMapper fiscalFinanceMapper;

    @Autowired
    private EnvironmentCultureMapper environmentCultureMapper;

    @Override
    public IPage<AlertRule> getAlertRules(Page<AlertRule> page) {
        return alertRuleMapper.selectPage(page, new QueryWrapper<AlertRule>().orderByDesc("created_at"));
    }

    @Override
    public boolean saveAlertRule(AlertRule rule) {
        if (rule.getRuleId() == null) {
            rule.setCreatedAt(LocalDateTime.now());
            rule.setUpdatedAt(LocalDateTime.now());
            return alertRuleMapper.insert(rule) > 0;
        } else {
            rule.setUpdatedAt(LocalDateTime.now());
            return alertRuleMapper.updateById(rule) > 0;
        }
    }

    @Override
    public boolean deleteAlertRule(Integer id) {
        return alertRuleMapper.deleteById(id) > 0;
    }

    @Override
    public IPage<Map<String, Object>> getAlerts(Page<Alert> page) {
        return alertMapper.selectAlertsWithCountyInfo(page);
    }

    @Override
    @Transactional
    public boolean confirmAlert(Integer id, Integer userId) {
        Alert alert = alertMapper.selectById(id);
        if (alert != null) {
            alert.setStatus("已确认");
            alert.setConfirmedBy(userId);
            alert.setConfirmedAt(LocalDateTime.now());
            return alertMapper.updateById(alert) > 0;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int checkAndGenerateAlerts(Integer year) {
        log.info("开始检查{}年预警...", year);

        // 1. 清空旧的新建状态预警
        QueryWrapper<Alert> deleteWrapper = new QueryWrapper<>();
        deleteWrapper.eq("year", year).eq("status", "新建");
        alertMapper.delete(deleteWrapper);
        log.info("已清空旧预警");

        // 2. 获取所有县域的风险评估数据
        QueryWrapper<ComprehensiveRiskAssessment> wrapper = new QueryWrapper<>();
        wrapper.eq("year", year);
        List<ComprehensiveRiskAssessment> assessments = riskAssessmentMapper.selectList(wrapper);
        log.info("找到{}条风险评估记录", assessments.size());

        int alertCount = 0;

        // 3. 对每个县域应用规则
        for (ComprehensiveRiskAssessment assessment : assessments) {
            // 综合预警规则已移除（应用户要求）
            /*
             * if (assessment.getComprehensiveRiskScore() != null &&
             * assessment.getComprehensiveRiskScore().doubleValue() >= 14.0) {
             * createAlert(assessment, "综合预警",
             * assessment.getCountyCode() + "综合风险分数达" +
             * assessment.getComprehensiveRiskScore().setScale(1, BigDecimal.ROUND_HALF_UP)
             * + "分");
             * alertCount++;
             * }
             */

            // 经济预警：经济风险分数高
            if (assessment.getEconomicRiskScore() != null &&
                    assessment.getEconomicRiskScore().doubleValue() >= 40.0) {
                createAlert(assessment, "经济预警",
                        "经济风险分数" + assessment.getEconomicRiskScore().setScale(0, BigDecimal.ROUND_HALF_UP)
                                + "分，存在经济下行压力");
                alertCount++;
            }

            // 环境预警：环境风险分数高（虽然当前环境分普遍较低）
            if (assessment.getEnvironmentRiskScore() != null &&
                    assessment.getEnvironmentRiskScore().doubleValue() >= 30.0) {
                createAlert(assessment, "环境预警",
                        "环境风险分数" + assessment.getEnvironmentRiskScore().setScale(0, BigDecimal.ROUND_HALF_UP)
                                + "分，环境压力较大");
                alertCount++;
            }

            // 检查财政数据
            QueryWrapper<FiscalFinance> fiscalWrapper = new QueryWrapper<>();
            fiscalWrapper.eq("county_code", assessment.getCountyCode()).eq("year", year);
            FiscalFinance fiscal = fiscalFinanceMapper.selectOne(fiscalWrapper);

            if (fiscal != null) {
                // 财政自给率低预警（调整为20%）
                if (fiscal.getFiscalSelfSufficiency() != null &&
                        fiscal.getFiscalSelfSufficiency().doubleValue() < 20.0) {
                    createAlert(assessment, "经济预警",
                            "财政自给率仅" + fiscal.getFiscalSelfSufficiency().setScale(1, BigDecimal.ROUND_HALF_UP)
                                    + "%，财政压力大");
                    alertCount++;
                }

                // 债务率预警已移除（因为使用模拟数据，不够准确）
                /*
                 * if (fiscal.getDebtToRevenueRatio() != null &&
                 * fiscal.getDebtToRevenueRatio().doubleValue() > 350.0) {
                 * createAlert(assessment, "经济预警",
                 * "债务率达" + fiscal.getDebtToRevenueRatio().setScale(0, BigDecimal.ROUND_HALF_UP)
                 * + "%，债务负担重");
                 * alertCount++;
                 * }
                 */
            }

            // 检查环境数据
            QueryWrapper<EnvironmentCulture> envWrapper = new QueryWrapper<>();
            envWrapper.eq("county_code", assessment.getCountyCode()).eq("year", year);
            EnvironmentCulture env = environmentCultureMapper.selectOne(envWrapper);

            if (env != null && env.getAirQualityIndex() != null) {
                // 空气质量差预警
                if (env.getAirQualityIndex().doubleValue() > 150.0) {
                    createAlert(assessment, "环境预警",
                            "空气质量指数" + env.getAirQualityIndex().setScale(0, BigDecimal.ROUND_HALF_UP) + "，空气质量差");
                    alertCount++;
                }
            }
        }

        log.info("检查完成，生成{}条预警", alertCount);
        return alertCount;
    }

    private void createAlert(ComprehensiveRiskAssessment assessment, String alertType, String title) {
        Alert alert = new Alert();
        alert.setCountyCode(assessment.getCountyCode());
        alert.setYear(assessment.getYear());
        alert.setRiskLevel(convertRiskLevel(assessment.getRiskLevel()));
        alert.setAlertType(alertType);
        alert.setTitle(title);
        alert.setStatus("新建");
        alert.setCreatedAt(LocalDateTime.now());
        alertMapper.insert(alert);
    }

    private String convertRiskLevel(String chineseLevel) {
        if (chineseLevel == null)
            return "YELLOW";
        switch (chineseLevel) {
            case "高风险":
                return "RED";
            case "中高风险":
                return "ORANGE";
            case "中风险":
            case "中低风险":
                return "YELLOW";
            case "低风险":
                return "GREEN";
            default:
                return "YELLOW";
        }
    }
}
