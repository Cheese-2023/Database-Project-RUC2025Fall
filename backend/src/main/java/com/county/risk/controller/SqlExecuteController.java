package com.county.risk.controller;

import com.county.risk.common.Result;
import com.county.risk.service.SqlExecuteService;
import com.county.risk.util.RolePermissionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SQL执行控制器
 */
@Slf4j
@RestController
@RequestMapping("/sql")
public class SqlExecuteController {

    @Autowired
    private SqlExecuteService sqlExecuteService;

    /**
     * 执行查询SQL
     */
    @PostMapping("/query")
    public Result<Map<String, Object>> executeQuery(
            @RequestParam String sql,
            @RequestHeader(value = "role", required = false) String role) {
        
        // 权限检查
        if (!RolePermissionUtil.canExecuteSQL(role)) {
            return Result.error(403, "您没有权限执行SQL操作");
        }
        
        try {
            List<Map<String, Object>> results = sqlExecuteService.executeQuery(sql);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("data", results);
            responseData.put("count", results.size());
            return Result.success(responseData);
        } catch (Exception e) {
            log.error("执行查询SQL失败", e);
            return Result.error(500, e.getMessage());
        }
    }

    /**
     * 执行更新SQL
     */
    @PostMapping("/update")
    public Result<Map<String, Object>> executeUpdate(
            @RequestParam String sql,
            @RequestHeader(value = "role", required = false) String role) {
        
        // 权限检查
        if (!RolePermissionUtil.canExecuteSQL(role)) {
            return Result.error(403, "您没有权限执行SQL操作");
        }
        
        try {
            int affectedRows = sqlExecuteService.executeUpdate(sql);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("affectedRows", affectedRows);
            responseData.put("message", "执行成功，影响 " + affectedRows + " 行");
            return Result.success(responseData);
        } catch (Exception e) {
            log.error("执行更新SQL失败", e);
            return Result.error(500, e.getMessage());
        }
    }

    /**
     * 获取数据库表列表
     */
    @GetMapping("/tables")
    public Result<List<String>> getTableList(
            @RequestHeader(value = "role", required = false) String role) {
        
        // 权限检查
        if (!RolePermissionUtil.canExecuteSQL(role)) {
            return Result.error(403, "您没有权限访问此功能");
        }
        
        try {
            List<String> tables = sqlExecuteService.getTableList();
            return Result.success(tables);
        } catch (Exception e) {
            log.error("获取表列表失败", e);
            return Result.error(500, e.getMessage());
        }
    }

    /**
     * 获取表结构
     */
    @GetMapping("/table-structure")
    public Result<List<Map<String, Object>>> getTableStructure(
            @RequestParam String tableName,
            @RequestHeader(value = "role", required = false) String role) {
        
        // 权限检查
        if (!RolePermissionUtil.canExecuteSQL(role)) {
            return Result.error(403, "您没有权限访问此功能");
        }
        
        try {
            List<Map<String, Object>> structure = sqlExecuteService.getTableStructure(tableName);
            return Result.success(structure);
        } catch (Exception e) {
            log.error("获取表结构失败", e);
            return Result.error(500, e.getMessage());
        }
    }
}

