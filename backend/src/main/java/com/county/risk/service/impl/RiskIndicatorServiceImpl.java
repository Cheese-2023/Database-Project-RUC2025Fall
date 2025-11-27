package com.county.risk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.county.risk.entity.RiskIndicator;
import com.county.risk.mapper.RiskIndicatorMapper;
import com.county.risk.service.RiskIndicatorService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.math.BigDecimal;

/**
 * 风险指标服务实现类
 */
@Service
public class RiskIndicatorServiceImpl extends ServiceImpl<RiskIndicatorMapper, RiskIndicator>
        implements RiskIndicatorService {

    @Override
    public Map<String, List<RiskIndicator>> getIndicatorsByCategory() {
        List<RiskIndicator> list = list(new LambdaQueryWrapper<RiskIndicator>()
                .eq(RiskIndicator::getStatus, "启用")
                .orderByDesc(RiskIndicator::getWeight));

        // 移除强制更新逻辑，确保用户修改的配置能够持久化
        // 如果需要初始化，应通过专门的初始化接口或数据库迁移脚本完成

        return list.stream().collect(Collectors.groupingBy(RiskIndicator::getCategory));
    }

    private void setDefaultThresholds(RiskIndicator item, double high, double medium, double low) {
        // 仅当阈值不合理(如小于1且单位是%)或为空时重置，或者为了强制修复逻辑错误而重置
        // 这里为了响应用户"全部重构"的要求，强制重置所有阈值
        item.setThresholdHigh(BigDecimal.valueOf(high));
        item.setThresholdMedium(BigDecimal.valueOf(medium));
        item.setThresholdLow(BigDecimal.valueOf(low));
    }

    @Override
    public boolean updateIndicatorConfig(Integer id, RiskIndicator indicator) {
        RiskIndicator existing = getById(id);
        if (existing == null) {
            return false;
        }

        if (indicator.getWeight() != null)
            existing.setWeight(indicator.getWeight());
        if (indicator.getThresholdHigh() != null)
            existing.setThresholdHigh(indicator.getThresholdHigh());
        if (indicator.getThresholdMedium() != null)
            existing.setThresholdMedium(indicator.getThresholdMedium());
        if (indicator.getThresholdLow() != null)
            existing.setThresholdLow(indicator.getThresholdLow());
        if (indicator.getUnit() != null)
            existing.setUnit(indicator.getUnit());
        if (indicator.getComparisonOperator() != null)
            existing.setComparisonOperator(indicator.getComparisonOperator());

        return updateById(existing);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void restoreDefaultIndicators() {
        // Format: {Weight, High, Medium, Low}
        // 调整权重和阈值以实现金字塔分布：低风险最多(40-50%)，中风险部分(30-40%)，高风险最少(10-20%)
        Map<String, double[]> defaults = new java.util.HashMap<>();

        // 经济风险指标 - 降低权重，提高阈值使更多县域为低风险
        defaults.put("GDP_GROWTH", new double[] { 0.08, 1.0, 3.0, 5.0 }); // 降低权重从0.13→0.08
        defaults.put("FISCAL_SELF_SUFFICIENCY", new double[] { 0.08, 25.0, 40.0, 60.0 }); // 降低阈值
        defaults.put("DEBT_RATIO", new double[] { 0.10, 120.0, 100.0, 80.0 }); // 提高高风险阈值
        defaults.put("GDP_PER_CAPITA", new double[] { 0.06, 2.0, 4.0, 7.0 }); // 降低权重

        // 社会风险指标 - 降低权重
        defaults.put("POPULATION_DECLINE", new double[] { 0.20, 1.5, 1.0, 0.5 }); // 从0.30→0.20
        defaults.put("EMPLOYMENT_RATE", new double[] { 0.25, 80.0, 85.0, 92.0 }); // 从0.40→0.25，降低阈值
        defaults.put("INCOME_GAP", new double[] { 0.20, 3.5, 3.0, 2.5 }); // 从0.30→0.20

        // 环境风险指标 - 大幅降低权重（环境数据较少）
        defaults.put("AIR_QUALITY", new double[] { 0.30, 180.0, 120.0, 80.0 }); // 从0.50→0.30，提高阈值
        defaults.put("EMISSION_INTENSITY", new double[] { 0.30, 2.0, 1.5, 1.0 }); // 从0.50→0.30

        // 治理风险指标 - 降低权重
        defaults.put("EDUCATION_INVESTMENT", new double[] { 0.05, 10.0, 13.0, 16.0 }); // 从0.07→0.05
        defaults.put("HEALTH_INVESTMENT", new double[] { 0.02, 6.0, 8.0, 11.0 }); // 从0.03→0.02

        // 发展风险指标 - 降低权重
        defaults.put("INNOVATION_CAPACITY", new double[] { 0.03, 30.0, 50.0, 70.0 }); // 从0.04→0.03

        List<RiskIndicator> indicators = list();
        System.out.println("Found " + indicators.size() + " indicators to restore.");

        for (RiskIndicator indicator : indicators) {
            if (defaults.containsKey(indicator.getIndicatorCode())) {
                double[] values = defaults.get(indicator.getIndicatorCode());
                System.out.println(
                        "Restoring " + indicator.getIndicatorCode() + " - weight:" + values[0] + " thresholds:"
                                + java.util.Arrays.toString(java.util.Arrays.copyOfRange(values, 1, 4)));
                indicator.setWeight(BigDecimal.valueOf(values[0]));
                indicator.setThresholdHigh(BigDecimal.valueOf(values[1]));
                indicator.setThresholdMedium(BigDecimal.valueOf(values[2]));
                indicator.setThresholdLow(BigDecimal.valueOf(values[3]));
            } else {
                System.out.println("No defaults found for " + indicator.getIndicatorCode());
            }
        }
        boolean success = updateBatchById(indicators);
        System.out.println("Update batch result: " + success + ", updated " + indicators.size() + " indicators");
    }
}
