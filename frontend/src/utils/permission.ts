/**
 * 权限工具函数
 */

/**
 * 检查用户是否可以访问风险监控大屏
 */
export const canAccessDashboard = (role: string | null): boolean => {
  return true; // 所有用户都可以访问
};

/**
 * 检查用户是否可以使用AI助手
 */
export const canUseAI = (role: string | null): boolean => {
  if (!role) return false;
  return ['VIP', 'RISK_ANALYST', 'DATA_MAINTAINER', 'ADMIN'].includes(role);
};

/**
 * 检查用户是否可以访问风险分析页面
 */
export const canAccessRiskAnalysis = (role: string | null): boolean => {
  if (!role) return false;
  return ['RISK_ANALYST', 'DATA_MAINTAINER', 'ADMIN'].includes(role);
};

/**
 * 检查用户是否可以访问预警管理页面
 */
export const canAccessAlertManage = (role: string | null): boolean => {
  if (!role) return false;
  return ['RISK_ANALYST', 'DATA_MAINTAINER', 'ADMIN'].includes(role);
};

/**
 * 检查用户是否可以访问数据管理页面
 */
export const canAccessDataManage = (role: string | null): boolean => {
  if (!role) return false;
  return ['RISK_ANALYST', 'DATA_MAINTAINER', 'ADMIN'].includes(role);
};

/**
 * 检查用户是否可以访问成果展示页面
 */
export const canAccessPovertyAchievement = (role: string | null): boolean => {
  if (!role) return false;
  return ['RISK_ANALYST', 'DATA_MAINTAINER', 'ADMIN'].includes(role);
};

/**
 * 检查用户是否可以执行SQL操作
 */
export const canExecuteSQL = (role: string | null): boolean => {
  if (!role) return false;
  return ['RISK_ANALYST', 'DATA_MAINTAINER', 'ADMIN'].includes(role);
};

/**
 * 检查用户是否可以调整风险参数
 */
export const canAdjustRiskParams = (role: string | null): boolean => {
  if (!role) return false;
  return ['RISK_ANALYST', 'ADMIN'].includes(role);
};

