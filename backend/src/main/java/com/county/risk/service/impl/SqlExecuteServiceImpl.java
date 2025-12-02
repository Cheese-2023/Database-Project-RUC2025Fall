package com.county.risk.service.impl;

import com.county.risk.service.SqlExecuteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;

/**
 * SQL执行服务实现类
 */
@Slf4j
@Service
public class SqlExecuteServiceImpl implements SqlExecuteService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    // 禁止的危险SQL关键字
    private static final Set<String> DANGEROUS_KEYWORDS = new HashSet<>(Arrays.asList(
        "DROP", "TRUNCATE", "ALTER", "CREATE", "GRANT", "REVOKE",
        "FLUSH", "LOCK", "UNLOCK", "SHUTDOWN", "KILL"
    ));

    @Override
    public List<Map<String, Object>> executeQuery(String sql) {
        if (!isSqlSafe(sql)) {
            throw new IllegalArgumentException("SQL语句包含危险操作，已被拒绝执行");
        }
        
        try {
            // 确保是SELECT语句
            String trimmedSql = sql.trim().toUpperCase();
            if (!trimmedSql.startsWith("SELECT")) {
                throw new IllegalArgumentException("查询操作只能执行SELECT语句");
            }
            
            log.info("执行查询SQL: {}", sql);
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
            log.info("查询返回 {} 条记录", results.size());
            return results;
        } catch (Exception e) {
            log.error("执行查询SQL失败: {}", e.getMessage(), e);
            throw new RuntimeException("执行SQL失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public int executeUpdate(String sql) {
        if (!isSqlSafe(sql)) {
            throw new IllegalArgumentException("SQL语句包含危险操作，已被拒绝执行");
        }
        
        try {
            String trimmedSql = sql.trim().toUpperCase();
            // 只允许INSERT、UPDATE、DELETE语句
            if (!trimmedSql.startsWith("INSERT") 
                && !trimmedSql.startsWith("UPDATE") 
                && !trimmedSql.startsWith("DELETE")) {
                throw new IllegalArgumentException("更新操作只能执行INSERT、UPDATE、DELETE语句");
            }
            
            log.info("执行更新SQL: {}", sql);
            int affectedRows = jdbcTemplate.update(sql);
            log.info("更新影响 {} 行", affectedRows);
            return affectedRows;
        } catch (Exception e) {
            log.error("执行更新SQL失败: {}", e.getMessage(), e);
            throw new RuntimeException("执行SQL失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isSqlSafe(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return false;
        }
        
        String upperSql = sql.toUpperCase().trim();
        
        // 检查是否包含危险关键字
        for (String keyword : DANGEROUS_KEYWORDS) {
            if (upperSql.contains(keyword)) {
                log.warn("检测到危险SQL关键字: {}", keyword);
                return false;
            }
        }
        
        // 检查是否包含多个语句（防止SQL注入）
        if (upperSql.contains(";") && upperSql.split(";").length > 1) {
            log.warn("检测到多个SQL语句");
            return false;
        }
        
        return true;
    }

    @Override
    public List<String> getTableList() {
        try {
            List<String> tables = new ArrayList<>();
            DatabaseMetaData metaData = dataSource.getConnection().getMetaData();
            ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE"});
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                tables.add(tableName);
            }
            rs.close();
            return tables;
        } catch (Exception e) {
            log.error("获取表列表失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取表列表失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, Object>> getTableStructure(String tableName) {
        try {
            List<Map<String, Object>> columns = new ArrayList<>();
            DatabaseMetaData metaData = dataSource.getConnection().getMetaData();
            ResultSet rs = metaData.getColumns(null, null, tableName, null);
            while (rs.next()) {
                Map<String, Object> column = new HashMap<>();
                column.put("columnName", rs.getString("COLUMN_NAME"));
                column.put("dataType", rs.getString("TYPE_NAME"));
                column.put("columnSize", rs.getInt("COLUMN_SIZE"));
                column.put("nullable", rs.getInt("NULLABLE") == 1);
                column.put("remarks", rs.getString("REMARKS"));
                columns.add(column);
            }
            rs.close();
            return columns;
        } catch (Exception e) {
            log.error("获取表结构失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取表结构失败: " + e.getMessage(), e);
        }
    }
}

