package com.county.risk.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.county.risk.common.Result;
import com.county.risk.dto.DataQualityIssueDTO;
import com.county.risk.service.DataQualityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "数据质量管理接口")
@RestController
@RequestMapping("/data/quality")
@CrossOrigin
public class DataQualityController {

    @Autowired
    private DataQualityService dataQualityService;

    @Operation(summary = "获取数据质量检查列表")
    @GetMapping("/list")
    public Result<IPage<DataQualityIssueDTO>> getCheckList(@RequestParam(defaultValue = "1") Integer page,
                                                          @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(dataQualityService.getCheckList(new Page<>(page, size)));
    }

    @Operation(summary = "解决数据问题")
    @PostMapping("/resolve/{id}")
    public Result<Boolean> resolveIssue(@PathVariable Integer id, @RequestParam String comment) {
        // TODO: 获取当前登录用户ID
        Integer userId = 1;
        return Result.success(dataQualityService.resolveIssue(id, comment, userId));
    }

    @Operation(summary = "忽略数据问题")
    @PostMapping("/ignore/{id}")
    public Result<Boolean> ignoreIssue(@PathVariable Integer id) {
        // TODO: 获取当前登录用户ID
        Integer userId = 1;
        return Result.success(dataQualityService.ignoreIssue(id, userId));
    }
}
