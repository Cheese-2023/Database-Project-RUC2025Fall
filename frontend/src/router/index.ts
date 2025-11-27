import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue'),
    meta: { title: '风险监控大屏' }
  },
  {
    path: '/risk-analysis',
    name: 'RiskAnalysis',
    component: () => import('@/views/RiskAnalysis.vue'),
    meta: { title: '风险分析' }
  },
  {
    path: '/alert-manage',
    name: 'AlertManage',
    component: () => import('@/views/AlertManage.vue'),
    meta: { title: '预警管理' }
  },
  {
    path: '/data-manage',
    name: 'DataManage',
    component: () => import('@/views/DataManage.vue'),
    meta: { title: '数据管理' }
  },

]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
