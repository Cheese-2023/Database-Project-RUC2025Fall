<template>
  <el-container class="layout-container">
    <!-- 顶部导航栏 -->
    <el-header class="layout-header">
      <div class="header-left">
        <el-button 
          v-if="showBackButton" 
          type="text" 
          @click="handleBack"
          class="back-button"
        >
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <div class="header-title">
          <h1>县域风险预警与可视化决策系统</h1>
          <p class="subtitle">County Risk Warning and Visualization Decision System</p>
        </div>
      </div>
      <div class="header-nav">
        <el-menu 
          mode="horizontal" 
          :default-active="activeMenu" 
          @select="handleMenuSelect"
          class="header-menu"
        >
          <el-menu-item index="dashboard">风险监控大屏</el-menu-item>
          <el-menu-item 
            v-if="userRole === 'ADMIN'" 
            index="risk-analysis"
          >
            风险分析
          </el-menu-item>
          <el-menu-item 
            v-if="userRole === 'ADMIN'" 
            index="alert-manage"
          >
            预警管理
          </el-menu-item>
          <el-menu-item 
            v-if="userRole === 'ADMIN'" 
            index="data-manage"
          >
            数据管理
          </el-menu-item>
          <el-menu-item index="poverty-achievement">
            成果展示
          </el-menu-item>
          <el-menu-item index="deepseek-chat">
            AI助手
          </el-menu-item>
        </el-menu>
      </div>
      <div class="header-actions">
        <div class="user-info">
          <span class="user-name">{{ username || '访客' }}</span>
          <el-tag size="small" effect="dark" type="info">{{ userRole || '未登录' }}</el-tag>
        </div>
        <el-button type="primary" size="small" @click="handleLogout">
          退出登录
        </el-button>
      </div>
    </el-header>
    
    <!-- 主内容区 -->
    <el-main class="layout-main">
      <router-view v-slot="{ Component }">
        <keep-alive :include="cachedViews">
          <component :is="Component" :key="$route.fullPath" />
        </keep-alive>
      </router-view>
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

// 当前用户角色
const userRole = computed(() => {
  return localStorage.getItem('userRole') || ''
})

const username = computed(() => {
  return localStorage.getItem('username') || ''
})

// 需要缓存的视图（避免重复加载数据）
const cachedViews = ['Dashboard', 'RiskAnalysis', 'AlertManage', 'DataManage', 'PovertyAchievement', 'DeepSeekChat']

// 当前激活的菜单
const activeMenu = computed(() => {
  const name = route.name as string
  if (name === 'Dashboard') return 'dashboard'
  if (name === 'RiskAnalysis') return 'risk-analysis'
  if (name === 'AlertManage') return 'alert-manage'
  if (name === 'DataManage') return 'data-manage'
  if (name === 'PovertyAchievement') return 'poverty-achievement'
  if (name === 'DeepSeekChat') return 'deepseek-chat'
  return 'dashboard'
})

// 是否显示返回按钮（不在首页时显示）
const showBackButton = computed(() => {
  return route.name !== 'Dashboard'
})

// 菜单选择处理
const handleMenuSelect = (index: string) => {
  router.push(`/${index}`)
}

// 返回按钮处理
const handleBack = () => {
  router.push('/dashboard')
}

const handleLogout = () => {
  localStorage.removeItem('isLoggedIn')
  localStorage.removeItem('userRole')
  localStorage.removeItem('username')
  router.push('/login')
}
</script>

<style scoped lang="scss">
.layout-container {
  height: 100vh;
  overflow: hidden;
}

.layout-header {
  height: 80px !important;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  z-index: 1000;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.back-button {
  color: white !important;
  font-size: 16px;
  padding: 8px 16px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 4px;
  transition: all 0.3s;
  
  &:hover {
    background: rgba(255, 255, 255, 0.1);
    border-color: rgba(255, 255, 255, 0.5);
  }
  
  .el-icon {
    margin-right: 4px;
  }
}

.header-title {
  h1 {
    font-size: 24px;
    font-weight: 600;
    margin: 0;
    line-height: 1.2;
  }
  
  .subtitle {
    font-size: 12px;
    opacity: 0.9;
    margin: 4px 0 0 0;
  }
}

.header-nav {
  flex: 1;
  display: flex;
  justify-content: flex-end;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-left: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgba(255, 255, 255, 0.9);
  
  .user-name {
    font-weight: 500;
  }
}

.header-menu {
  background: transparent !important;
  border: none !important;
  
  :deep(.el-menu-item) {
    color: rgba(255, 255, 255, 0.9) !important;
    border-bottom: 2px solid transparent !important;
    margin: 0 10px;
    
    &:hover {
      background: rgba(255, 255, 255, 0.1) !important;
      color: white !important;
    }
    
    &.is-active {
      color: white !important;
      border-bottom-color: white !important;
      background: rgba(255, 255, 255, 0.1) !important;
    }
  }
}

.layout-main {
  padding: 0;
  background: #f5f7fa;
  overflow-y: auto;
  height: calc(100vh - 80px);
}
</style>

