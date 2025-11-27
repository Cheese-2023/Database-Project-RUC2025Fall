package com.county.risk.controller;

import com.county.risk.common.Result;
import com.county.risk.entity.ComprehensiveRiskAssessment;
import com.county.risk.service.RiskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 风险评估控制器
 */
@Tag(name = "风险评估管理", description = "县域风险评估相关接口")
@RestController
@RequestMapping("/risk")
public class RiskController {
    
    @Autowired
    private RiskService riskService;
    
    /**
     * 按风险等级查询
     */
    @Operation(summary = "按风险等级查询", description = "根据风险等级查询县域列表")
    @GetMapping("/level/{level}")
    public Result<List<ComprehensiveRiskAssessment>> getRiskByLevel(
            @Parameter(description = "风险等级：低风险、中低风险、中风险、中高风险、高风险")
            @PathVariable String level) {
        List<ComprehensiveRiskAssessment> list = riskService.getRiskByLevel(level);
        return Result.success(list);
    }
    
    /**
     * 查询县域风险
     */
    @Operation(summary = "查询县域风险", description = "根据县域代码查询风险评估信息")
    @GetMapping("/county/{countyCode}")
    public Result<ComprehensiveRiskAssessment> getCountyRisk(
            @Parameter(description = "县域代码") @PathVariable String countyCode,
            @Parameter(description = "年份（可选）") @RequestParam(required = false) Integer year) {
        ComprehensiveRiskAssessment risk = riskService.getCountyRisk(countyCode, year);
        return Result.success(risk);
    }
    
    /**
     * 获取风险统计信息
     */
    @Operation(summary = "获取风险统计", description = "获取各风险等级数量、平均分等统计信息")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getRiskStatistics() {
        Map<String, Object> statistics = riskService.getRiskStatistics();
        return Result.success(statistics);
    }
    
    /**
     * 获取县域风险趋势
     */
    @Operation(summary = "获取县域风险趋势", description = "获取单个县域历年风险变化趋势")
    @GetMapping("/trend/{countyCode}")
    public Result<List<Map<String, Object>>> getRiskTrend(
            @Parameter(description = "县域代码") @PathVariable String countyCode) {
        List<Map<String, Object>> trend = riskService.getRiskTrend(countyCode);
        return Result.success(trend);
    }
    
    /**
     * 获取平均风险趋势
     */
    @Operation(summary = "获取平均风险趋势", description = "获取全国县域平均风险历年变化趋势")
    @GetMapping("/trend/average")
    public Result<List<Map<String, Object>>> getAverageRiskTrend() {
        List<Map<String, Object>> trend = riskService.getAverageRiskTrend();
        return Result.success(trend);
    }
    
    /**
     * 获取高风险县域TOP榜
     */
    @Operation(summary = "获取高风险TOP榜", description = "获取综合风险分最高的县域列表")
    @GetMapping("/top")
    public Result<List<Map<String, Object>>> getTopRiskCounties(
            @Parameter(description = "返回数量") @RequestParam(defaultValue = "10") int limit) {
        List<Map<String, Object>> topList = riskService.getTopRiskCounties(limit);
        return Result.success(topList);
    }
    
    /**
     * 获取风险列表（含县域信息）
     */
    @Operation(summary = "获取风险列表", description = "获取风险评估列表（支持多条件筛选）")
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> getRiskList(
            @Parameter(description = "风险等级") @RequestParam(required = false) String level,
            @Parameter(description = "省份名称") @RequestParam(required = false) String provinceName,
            @Parameter(description = "年份") @RequestParam(required = false) Integer year) {
        List<Map<String, Object>> list = riskService.getRiskListWithCountyInfo(level, provinceName, year);
        return Result.success(list);
    }
}
