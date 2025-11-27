package com.county.risk.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.county.risk.entity.RiskIndicator;

import java.util.List;
import java.util.Map;

/**
 * 风险指标服务接口
 */
public interface RiskIndicatorService extends IService<RiskIndicator> {

    /**
     * 获取按类别分组的指标
     */
    Map<String, List<RiskIndicator>> getIndicatorsByCategory();

    /**
     * 更新指标权重和阈值
     */
    boolean updateIndicatorConfig(Integer indicatorId, RiskIndicator indicator);

    /**
     * 恢复默认指标配置
     */
    void restoreDefaultIndicators();
}
