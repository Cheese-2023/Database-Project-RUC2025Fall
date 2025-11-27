import request from '@/utils/request'

export const countyApi = {
  // 获取县域列表
  getCountyList() {
    return request.get('/county/list')
  },
  
  // 根据县代码获取县域信息
  getCountyByCode(countyCode: string) {
    return request.get(`/county/${countyCode}`)
  },
  
  // 根据省份查询县域
  getCountyByProvince(provinceName: string) {
    return request.get(`/county/province/${provinceName}`)
  }
}
