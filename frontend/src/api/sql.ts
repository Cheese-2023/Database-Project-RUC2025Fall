import request from '../utils/request';

/**
 * SQL执行相关接口
 */

// 执行查询SQL
export const executeQuery = (sql: string) => {
  return request.post(`/sql/query?sql=${encodeURIComponent(sql)}`);
};

// 执行更新SQL
export const executeUpdate = (sql: string) => {
  return request.post(`/sql/update?sql=${encodeURIComponent(sql)}`);
};

// 获取数据库表列表
export const getTableList = () => {
  return request.get('/sql/tables');
};

// 获取表结构
export const getTableStructure = (tableName: string) => {
  return request.get('/sql/table-structure', {
    params: { tableName }
  });
};

