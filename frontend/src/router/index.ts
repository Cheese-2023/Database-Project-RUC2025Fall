import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue'),
    meta: { title: '风险监控大屏', requiresAuth: true }
  },
  {
    path: '/risk-analysis',
    name: 'RiskAnalysis',
    component: () => import('@/views/RiskAnalysis.vue'),
    meta: { title: '风险分析', requiresAuth: true, roles: ['ADMIN', 'RISK_ANALYST', 'DATA_MAINTAINER'] }
  },
  {
    path: '/alert-manage',
    name: 'AlertManage',
    component: () => import('@/views/AlertManage.vue'),
    meta: { title: '预警管理', requiresAuth: true, roles: ['ADMIN', 'RISK_ANALYST', 'DATA_MAINTAINER'] }
  },
  {
    path: '/data-manage',
    name: 'DataManage',
    component: () => import('@/views/DataManage.vue'),
    meta: { title: '数据管理', requiresAuth: true, roles: ['ADMIN', 'RISK_ANALYST', 'DATA_MAINTAINER'] }
  },
  {
    path: '/sql-execute',
    name: 'SqlExecute',
    component: () => import('@/views/SqlExecute.vue'),
    meta: { title: 'SQL操作', requiresAuth: true, roles: ['ADMIN', 'RISK_ANALYST', 'DATA_MAINTAINER'] }
  },
  {
    path: '/poverty-achievement',
    name: 'PovertyAchievement',
    component: () => import('@/views/PovertyAchievement.vue'),
    meta: { title: '成果展示', requiresAuth: true, roles: ['ADMIN', 'RISK_ANALYST', 'DATA_MAINTAINER'] }
  },
  {
    path: '/deepseek-chat',
    name: 'DeepSeekChat',
    component: () => import('@/views/DeepSeekChat.vue'),
    meta: { title: 'AI助手', requiresAuth: true, roles: ['ADMIN', 'VIP', 'RISK_ANALYST', 'DATA_MAINTAINER'] }
  },

]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：未登录禁止访问需要权限的页面
router.beforeEach((to, _from, next) => {
  const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true'
  const role = localStorage.getItem('userRole') || ''

  // 已登录访问登录页，直接跳到大屏
  if (to.path === '/login' && isLoggedIn) {
    return next('/dashboard')
  }

  // 需要登录的路由，但当前未登录
  if (to.meta.requiresAuth && !isLoggedIn) {
    return next({
      path: '/login',
      query: { redirect: to.fullPath }
    })
  }

  // 有角色要求的路由，且当前角色不在允许列表中
  if (to.meta.roles && Array.isArray(to.meta.roles) && to.meta.roles.length > 0) {
    if (!role || !to.meta.roles.includes(role)) {
      // 普通用户访问管理页面时，强制回到大屏
      return next('/dashboard')
    }
  }

  next()
})

export default router
