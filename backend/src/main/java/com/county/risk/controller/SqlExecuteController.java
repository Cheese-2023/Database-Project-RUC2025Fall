package com.county.risk.controller;

import com.county.risk.service.SqlExecuteService;
import com.county.risk.util.RolePermissionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Map<String, Object>> executeQuery(
            @RequestParam String sql,
            @RequestHeader(value = "role", required = false) String role) {
        
        Map<String, Object> response = new HashMap<>();
        
        // 权限检查
        if (!RolePermissionUtil.canExecuteSQL(role)) {
            response.put("success", false);
            response.put("message", "您没有权限执行SQL操作");
            return ResponseEntity.status(403).body(response);
        }
        
        try {
            List<Map<String, Object>> results = sqlExecuteService.executeQuery(sql);
            response.put("success", true);
            response.put("data", results);
            response.put("count", results.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("执行查询SQL失败", e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 执行更新SQL
     */
    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> executeUpdate(
            @RequestParam String sql,
            @RequestHeader(value = "role", required = false) String role) {
        
        Map<String, Object> response = new HashMap<>();
        
        // 权限检查
        if (!RolePermissionUtil.canExecuteSQL(role)) {
            response.put("success", false);
            response.put("message", "您没有权限执行SQL操作");
            return ResponseEntity.status(403).body(response);
        }
        
        try {
            int affectedRows = sqlExecuteService.executeUpdate(sql);
            response.put("success", true);
            response.put("affectedRows", affectedRows);
            response.put("message", "执行成功，影响 " + affectedRows + " 行");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("执行更新SQL失败", e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取数据库表列表
     */
    @GetMapping("/tables")
    public ResponseEntity<Map<String, Object>> getTableList(
            @RequestHeader(value = "role", required = false) String role) {
        
        Map<String, Object> response = new HashMap<>();
        
        // 权限检查
        if (!RolePermissionUtil.canExecuteSQL(role)) {
            response.put("success", false);
            response.put("message", "您没有权限访问此功能");
            return ResponseEntity.status(403).body(response);
        }
        
        try {
            List<String> tables = sqlExecuteService.getTableList();
            response.put("success", true);
            response.put("data", tables);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取表列表失败", e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取表结构
     */
    @GetMapping("/table-structure")
    public ResponseEntity<Map<String, Object>> getTableStructure(
            @RequestParam String tableName,
            @RequestHeader(value = "role", required = false) String role) {
        
        Map<String, Object> response = new HashMap<>();
        
        // 权限检查
        if (!RolePermissionUtil.canExecuteSQL(role)) {
            response.put("success", false);
            response.put("message", "您没有权限访问此功能");
            return ResponseEntity.status(403).body(response);
        }
        
        try {
            List<Map<String, Object>> structure = sqlExecuteService.getTableStructure(tableName);
            response.put("success", true);
            response.put("data", structure);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取表结构失败", e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}

