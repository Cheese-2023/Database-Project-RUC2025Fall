package com.county.risk.service;

import java.util.List;
import java.util.Map;

/**
 * SQL执行服务接口
 */
public interface SqlExecuteService {
    
    /**
     * 执行查询SQL（SELECT语句）
     * @param sql SQL语句
     * @return 查询结果
     */
    List<Map<String, Object>> executeQuery(String sql);
    
    /**
     * 执行更新SQL（INSERT、UPDATE、DELETE语句）
     * @param sql SQL语句
     * @return 影响的行数
     */
    int executeUpdate(String sql);
    
    /**
     * 验证SQL语句是否安全（防止危险操作）
     * @param sql SQL语句
     * @return 是否安全
     */
    boolean isSqlSafe(String sql);
    
    /**
     * 获取数据库表列表
     * @return 表名列表
     */
    List<String> getTableList();
    
    /**
     * 获取表结构信息
     * @param tableName 表名
     * @return 表结构信息
     */
    List<Map<String, Object>> getTableStructure(String tableName);
}

