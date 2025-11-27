package com.county.risk.controller;

import com.county.risk.common.Result;
import com.county.risk.entity.RiskIndicator;
import com.county.risk.service.RiskCalculationService;
import com.county.risk.service.RiskIndicatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 风险指标管理控制器
 */
@Tag(name = "风险指标管理")
@RestController
@RequestMapping("/risk/indicators")
@RequiredArgsConstructor
public class RiskIndicatorController {

    private final RiskIndicatorService riskIndicatorService;
    private final RiskCalculationService riskCalculationService;

    @Operation(summary = "获取按类别分组的指标")
    @GetMapping("/grouped")
    public Result<Map<String, List<RiskIndicator>>> getGroupedIndicators() {
        return Result.success(riskIndicatorService.getIndicatorsByCategory());
    }

    @Operation(summary = "更新指标配置")
    @PutMapping("/{id}")
    public Result<Boolean> updateIndicator(@PathVariable Integer id, @RequestBody RiskIndicator indicator) {
        return Result.success(riskIndicatorService.updateIndicatorConfig(id, indicator));
    }

    @Operation(summary = "恢复默认指标配置")
    @PostMapping("/restore-defaults")
    public Result<Void> restoreDefaults() {
        riskIndicatorService.restoreDefaultIndicators();
        return Result.success();
    }

    @Operation(summary = "触发风险重新计算")
    @PostMapping("/calculate")
    public Result<Void> calculateRisk(@RequestParam(required = false) Integer year) {
        // 异步执行计算，避免前端超时
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                if (year == null) {
                    riskCalculationService.calculateAllYears();
                } else {
                    riskCalculationService.calculateAll(year);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return Result.success();
    }
}
