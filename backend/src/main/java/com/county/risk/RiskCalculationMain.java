package com.county.risk;

import com.county.risk.service.RiskCalculationService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import lombok.extern.slf4j.Slf4j;

/**
 * 风险计算主程序
 * 可以直接运行此程序来触发风险计算，无需启动完整的Web服务
 * 
 * 使用方法：
 * 1. 编译项目: mvn clean package
 * 2. 运行: java -cp target/classes:target/lib/* com.county.risk.RiskCalculationMain
 * 3. 或者: mvn exec:java -Dexec.mainClass="com.county.risk.RiskCalculationMain"
 */
@Slf4j
@SpringBootApplication
public class RiskCalculationMain {
    
    public static void main(String[] args) {
        log.info("========================================");
        log.info("启动风险计算程序");
        log.info("========================================");
        
        ConfigurableApplicationContext context = SpringApplication.run(CountyRiskWarningApplication.class, args);
        
        try {
            RiskCalculationService riskCalculationService = context.getBean(RiskCalculationService.class);
            
            log.info("开始计算所有年份的风险...");
            log.info("注意: 这可能需要较长时间（几分钟到几十分钟）");
            log.info("请查看日志了解计算进度");
            log.info("========================================");
            
            // 执行计算
            riskCalculationService.calculateAllYears();
            
            log.info("========================================");
            log.info("风险计算完成！");
            log.info("========================================");
            
        } catch (Exception e) {
            log.error("风险计算失败: {}", e.getMessage(), e);
        } finally {
            // 关闭应用上下文
            context.close();
        }
    }
}

