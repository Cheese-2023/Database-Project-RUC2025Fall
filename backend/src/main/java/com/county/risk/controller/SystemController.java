package com.county.risk.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.county.risk.common.Result;
import com.county.risk.dto.SystemOverviewDTO;
import com.county.risk.entity.SystemConfig;
import com.county.risk.service.SystemConfigService;
import com.county.risk.service.SystemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "系统管理接口")
@RestController
@RequestMapping("/system")
@RequiredArgsConstructor
public class SystemController {

    private final SystemService systemService;
    private final SystemConfigService systemConfigService;

    @Operation(summary = "获取系统概览信息")
    @GetMapping("/overview")
    public Result<SystemOverviewDTO> getSystemOverview() {
        return Result.success(systemService.getSystemOverview());
    }

    @Operation(summary = "获取系统配置列表")
    @GetMapping("/configs")
    public Result<List<SystemConfig>> getConfigs(@RequestParam(required = false) String category) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(category)) {
            wrapper.eq(SystemConfig::getCategory, category);
        }
        wrapper.orderByAsc(SystemConfig::getCategory).orderByAsc(SystemConfig::getConfigKey);
        return Result.success(systemConfigService.list(wrapper));
    }

    @Operation(summary = "更新系统配置")
    @PutMapping("/configs/{id}")
    public Result<Boolean> updateConfig(@PathVariable Integer id, @RequestBody SystemConfig request) {
        SystemConfig existing = systemConfigService.getById(id);
        if (existing == null) {
            return Result.error("配置不存在");
        }
        if (Boolean.FALSE.equals(existing.getIsEditable())) {
            return Result.error("该配置不允许修改");
        }
        existing.setConfigValue(request.getConfigValue());
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setUpdatedBy(1); // TODO: 替换为当前登录用户
        boolean updated = systemConfigService.updateById(existing);
        return Result.success(updated);
    }
}
