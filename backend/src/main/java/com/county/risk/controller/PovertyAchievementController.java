package com.county.risk.controller;

import com.county.risk.common.Result;
import com.county.risk.service.PovertyAchievementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 脱贫攻坚成果展示控制器
 */
@Tag(name = "脱贫攻坚成果展示", description = "832个贫困县摘帽后经济变化展示")
@RestController
@RequestMapping("/poverty-achievement")
@RequiredArgsConstructor
public class PovertyAchievementController {
    
    private final PovertyAchievementService povertyAchievementService;
    
    /**
     * 获取贫困县统计概览
     */
    @Operation(summary = "获取贫困县统计概览", description = "获取贫困县总数、摘帽年份分布等统计信息")
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        Map<String, Object> overview = povertyAchievementService.getOverview();
        return Result.success(overview);
    }
    
    /**
     * 获取按省份分布的贫困县统计
     */
    @Operation(summary = "按省份统计", description = "获取各省份贫困县数量统计")
    @GetMapping("/statistics/by-province")
    public Result<List<Map<String, Object>>> getStatisticsByProvince() {
        List<Map<String, Object>> statistics = povertyAchievementService.getStatisticsByProvince();
        return Result.success(statistics);
    }
    
    /**
     * 获取按摘帽年份统计
     */
    @Operation(summary = "按摘帽年份统计", description = "获取各年份摘帽的贫困县数量")
    @GetMapping("/statistics/by-delisting-year")
    public Result<List<Map<String, Object>>> getStatisticsByDelistingYear() {
        List<Map<String, Object>> statistics = povertyAchievementService.getStatisticsByDelistingYear();
        return Result.success(statistics);
    }
    
    /**
     * 获取摘帽前后经济指标对比
     */
    @Operation(summary = "摘帽前后对比", description = "获取贫困县摘帽前后主要经济指标对比数据")
    @GetMapping("/comparison")
    public Result<Map<String, Object>> getDelistingComparison(
            @Parameter(description = "摘帽年份（可选）") @RequestParam(required = false) Integer delistingYear) {
        Map<String, Object> comparison = povertyAchievementService.getDelistingComparison(delistingYear);
        return Result.success(comparison);
    }
    
    /**
     * 获取贫困县列表
     */
    @Operation(summary = "获取贫困县列表", description = "获取所有贫困县信息列表")
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> getPovertyCountyList(
            @Parameter(description = "省份（可选）") @RequestParam(required = false) String province,
            @Parameter(description = "摘帽年份（可选）") @RequestParam(required = false) Integer delistingYear) {
        List<Map<String, Object>> list = povertyAchievementService.getPovertyCountyList(province, delistingYear);
        return Result.success(list);
    }
    
    /**
     * 获取单个贫困县的详细数据
     */
    @Operation(summary = "获取贫困县详情", description = "获取单个贫困县的历年数据和摘帽前后对比")
    @GetMapping("/county/{countyCode}")
    public Result<Map<String, Object>> getCountyDetail(
            @Parameter(description = "县域代码") @PathVariable String countyCode) {
        Map<String, Object> detail = povertyAchievementService.getCountyDetail(countyCode);
        return Result.success(detail);
    }
    
    /**
     * 获取经济指标趋势数据
     */
    @Operation(summary = "获取经济指标趋势", description = "获取贫困县主要经济指标的历史趋势数据")
    @GetMapping("/trend")
    public Result<Map<String, Object>> getEconomicTrend(
            @Parameter(description = "指标类型：gdp, income, fiscal") @RequestParam(required = false, defaultValue = "gdp") String indicatorType) {
        Map<String, Object> trend = povertyAchievementService.getEconomicTrend(indicatorType);
        return Result.success(trend);
    }
}


