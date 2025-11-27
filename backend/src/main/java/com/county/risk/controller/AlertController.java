package com.county.risk.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.county.risk.common.Result;
import com.county.risk.entity.AlertRule;
import com.county.risk.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "预警管理接口")
@RestController
@RequestMapping("/alert")
@CrossOrigin
public class AlertController {

    @Autowired
    private AlertService alertService;

    @Operation(summary = "获取预警规则列表")
    @GetMapping("/rule/list")
    public Result<IPage<AlertRule>> getAlertRules(@RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(alertService.getAlertRules(new Page<>(page, size)));
    }

    @Operation(summary = "保存预警规则")
    @PostMapping("/rule")
    public Result<Boolean> saveAlertRule(@RequestBody AlertRule rule) {
        return Result.success(alertService.saveAlertRule(rule));
    }

    @Operation(summary = "删除预警规则")
    @DeleteMapping("/rule/{id}")
    public Result<Boolean> deleteAlertRule(@PathVariable Integer id) {
        return Result.success(alertService.deleteAlertRule(id));
    }

    @Operation(summary = "获取预警记录列表")
    @GetMapping("/list")
    public Result<IPage<Map<String, Object>>> getAlerts(@RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(alertService.getAlerts(new Page<>(page, size)));
    }

    @Operation(summary = "确认预警")
    @PostMapping("/confirm/{id}")
    public Result<Boolean> confirmAlert(@PathVariable Integer id) {
        // TODO: 获取当前登录用户ID
        Integer userId = 1; // 暂时硬编码
        return Result.success(alertService.confirmAlert(id, userId));
    }

    @Operation(summary = "检查并生成预警")
    @PostMapping("/check")
    public Result<Integer> checkAlerts() {
        int count = alertService.checkAndGenerateAlerts(2023);
        return Result.success(count);
    }
}
