import request from '../utils/request';

/**
 * 脱贫攻坚成果展示相关API
 */

// 获取贫困县统计概览
export const getPovertyOverview = () => {
  return request.get('/poverty-achievement/overview');
};

// 按省份统计
export const getStatisticsByProvince = () => {
  return request.get('/poverty-achievement/statistics/by-province');
};

// 按摘帽年份统计
export const getStatisticsByDelistingYear = () => {
  return request.get('/poverty-achievement/statistics/by-delisting-year');
};

// 获取摘帽前后对比
export const getDelistingComparison = (delistingYear?: number) => {
  return request.get('/poverty-achievement/comparison', { 
    params: { delistingYear } 
  });
};

// 获取贫困县列表
export const getPovertyCountyList = (province?: string, delistingYear?: number) => {
  return request.get('/poverty-achievement/list', { 
    params: { province, delistingYear } 
  });
};

// 获取贫困县详情
export const getCountyDetail = (countyCode: string) => {
  return request.get(`/poverty-achievement/county/${countyCode}`);
};

// 获取经济指标趋势
export const getEconomicTrend = (indicatorType: string = 'gdp') => {
  return request.get('/poverty-achievement/trend', { 
    params: { indicatorType } 
  });
};


