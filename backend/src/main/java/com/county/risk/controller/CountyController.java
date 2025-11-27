package com.county.risk.controller;

import com.county.risk.common.Result;
import com.county.risk.entity.CountyBasic;
import com.county.risk.service.CountyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 县域信息控制器
 */
@Tag(name = "县域信息管理")
@RestController
@RequestMapping("/county")
@RequiredArgsConstructor
public class CountyController {
    
    private final CountyService countyService;
    
    @Operation(summary = "获取所有县域列表")
    @GetMapping("/list")
    public Result<List<CountyBasic>> getCountyList() {
        return Result.success(countyService.list());
    }
    
    @Operation(summary = "根据县代码获取县域信息")
    @GetMapping("/{countyCode}")
    public Result<CountyBasic> getCountyByCode(@PathVariable String countyCode) {
        return Result.success(countyService.getById(countyCode));
    }
    
    @Operation(summary = "根据省份查询县域")
    @GetMapping("/province/{provinceName}")
    public Result<List<CountyBasic>> getCountyByProvince(@PathVariable String provinceName) {
        return Result.success(countyService.getByProvince(provinceName));
    }
}
