import request from '../utils/request'

// 获取预警规则列表
export const getAlertRules = (params: any) => {
  return request.get('/alert/rule/list', { params })
}

// 保存预警规则
export const saveAlertRule = (data: any) => {
  return request.post('/alert/rule', data)
}

// 删除预警规则
export const deleteAlertRule = (id: number) => {
  return request.delete(`/alert/rule/${id}`)
}

// 获取预警记录列表
export const getAlerts = (params: any) => {
  return request.get('/alert/list', { params })
}

// 确认预警
export const confirmAlert = (id: number) => {
  return request.post(`/alert/confirm/${id}`)
}

// 检查并生成预警
export const checkAlerts = () => {
  return request.post('/alert/check')
}
