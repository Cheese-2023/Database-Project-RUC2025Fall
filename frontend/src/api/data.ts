import request from '../utils/request'

// 获取数据质量检查列表
export const getQualityChecks = (params: any) => {
  return request.get('/data/quality/list', { params })
}

// 解决数据问题
export const resolveIssue = (id: number, comment: string) => {
  return request.post(`/data/quality/resolve/${id}`, null, { params: { comment } })
}

// 忽略数据问题
export const ignoreIssue = (id: number) => {
  return request.post(`/data/quality/ignore/${id}`)
}
