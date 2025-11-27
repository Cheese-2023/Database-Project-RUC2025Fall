import request from '../utils/request';

/**
 * 风险评估相关API
 */

// 按风险等级查询
export const getRiskByLevel = (level: string) => {
  return request.get(`/risk/level/${level}`);
};

// 查询县域风险
export const getCountyRisk = (countyCode: string, year?: number) => {
  return request.get(`/risk/county/${countyCode}`, { params: { year } });
};

// 获取风险统计信息
export const getRiskStatistics = () => {
  return request.get('/risk/statistics');
};

// 获取县域风险趋势
export const getRiskTrend = (countyCode: string) => {
  return request.get(`/risk/trend/${countyCode}`);
};

// 获取平均风险趋势
export const getAverageRiskTrend = () => {
  return request.get('/risk/trend/average');
};

// 获取高风险县域TOP榜
export const getTopRiskCounties = (limit: number = 10) => {
  return request.get('/risk/top', { params: { limit } });
};

// 获取风险列表（含县域信息）
export const getRiskList = (params?: {
  level?: string;
  provinceName?: string;
  year?: number | string;
}) => {
  return request.get('/risk/list', { params });
};

// 获取按类别分组的风险指标
export const getGroupedIndicators = () => {
  return request.get('/risk/indicators/grouped');
};

// 更新风险指标配置
export const updateIndicator = (id: number, data: any) => {
  return request.put(`/risk/indicators/${id}`, data);
};

// 触发风险重新计算
export const calculateRisk = (year?: number) => {
  return request.post('/risk/indicators/calculate', null, { params: { year } });
};

// 恢复默认配置
export const restoreDefaults = () => {
  return request.post('/risk/indicators/restore-defaults');
};
