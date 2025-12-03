package com.county.risk.util;

import com.county.risk.service.RiskCalculationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 风险计算运行器
 * 可以通过命令行参数触发风险计算
 * 
 * 使用方法：
 * 1. 在启动参数中添加：--risk.calculate=true
 * 2. 或者直接运行：java -jar app.jar --risk.calculate=true
 */
@Slf4j
@Component
public class RiskCalculationRunner implements CommandLineRunner {

    @Autowired(required = false)
    private RiskCalculationService riskCalculationService;

    @Override
    public void run(String... args) {
        // 检查是否有 --risk.calculate 参数
        boolean shouldCalculate = false;
        for (String arg : args) {
            if (arg.contains("--risk.calculate=true")) {
                shouldCalculate = true;
                break;
            }
        }
        
        if (shouldCalculate && riskCalculationService != null) {
            log.info("检测到风险计算参数，开始计算所有年份的风险...");
            try {
                riskCalculationService.calculateAllYears();
                log.info("风险计算完成！");
            } catch (Exception e) {
                log.error("风险计算失败: {}", e.getMessage(), e);
            }
        }
    }
}

