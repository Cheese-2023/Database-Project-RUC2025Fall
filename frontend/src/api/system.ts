import request from '@/utils/request'

export const systemApi = {
  getOverview() {
    return request.get('/system/overview')
  },
  getConfigs(params?: { category?: string }) {
    return request.get('/system/configs', { params })
  },
  updateConfig(id: number, data: { configValue: string }) {
    return request.put(`/system/configs/${id}`, data)
  }
}
