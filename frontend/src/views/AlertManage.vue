<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>预警记录</span>
          <el-button type="primary" @click="handleCheck" :loading="checking">
            <el-icon><Refresh /></el-icon>
            立即检查预警
          </el-button>
        </div>
      </template>

      <el-alert
        title="系统预警规则说明"
        type="warning"
        show-icon
        :closable="false"
        class="block-alert"
      >
        <template #default>
          <div class="rules-explanation">
            <p><strong>固定预警规则（自动检查2023年数据）：</strong></p>
            <ul>
              <!-- <li><strong>综合预警：</strong>综合风险分数 ≥ 14分</li> -->
              <li><strong>经济预警：</strong>
                <ul>
                  <li>经济风险分数 ≥ 40分</li>
                  <li>财政自给率 < 20%</li>
                </ul>
              </li>
              <li><strong>环境预警：</strong>
                <ul>
                  <li>环境风险分数 ≥ 30分</li>
                  <li>空气质量指数 > 150</li>
                </ul>
              </li>
            </ul>
            <p style="margin-top: 10px; color: #E6A23C;"><strong>提示：</strong>点击"立即检查预警"按钮可重新扫描所有县域数据，刷新预警记录。</p>
          </div>
        </template>
      </el-alert>

      <el-table 
        :data="alertList" 
        stripe 
        style="width: 100%" 
        v-loading="loading" 
        :empty-text="loading ? '加载中...' : '暂无预警记录'"
      >
        <el-table-column prop="countyName" label="县域" width="150" />
        <el-table-column prop="provinceName" label="省份" width="120" />
        <el-table-column prop="alertType" label="预警类型" width="120">
          <template #default="scope">
            <el-tag :type="getAlertTypeTag(scope.row.alertType)">
              {{ scope.row.alertType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === '已确认' ? 'success' : 'warning'">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button 
              v-if="scope.row.status === '新建'"
              type="primary" 
              size="small" 
              @click="handleConfirm(scope.row.alertId)">
              确认
            </el-button>
            <span v-else style="color: #67C23A;">✓ 已确认</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="alertPage.current"
          v-model:page-size="alertPage.size"
          :total="alertPage.total"
          layout="total, prev, pager, next"
          @current-change="loadAlerts"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onActivated, reactive } from 'vue'
import { getAlerts, confirmAlert, checkAlerts } from '../api/alert'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'

const loading = ref(false)
const checking = ref(false)

interface AlertItem {
  alertId: number
  countyName: string
  provinceName: string
  alertType: string
  title: string
  status: string
}

const alertList = ref<AlertItem[]>([])
const alertPage = reactive({
  current: 1,
  size: 10,
  total: 0
})

const normalizeAlert = (item: any): AlertItem => ({
  alertId: item.alert_id || item.alertId,
  countyName: item.county_name || item.countyName || '-',
  provinceName: item.province_name || item.provinceName || '-',
  alertType: item.alert_type || item.alertType || '综合预警',
  title: item.title || '-',
  status: item.status || '新建'
})

const getAlertTypeTag = (type: string) => {
  const typeMap: Record<string, any> = {
    '经济预警': 'danger',
    '社会预警': 'warning',
    '环境预警': 'success',
    '综合预警': 'primary'
  }
  return typeMap[type] || 'info'
}

// 加载预警记录
const loadAlerts = async () => {
  loading.value = true
  try {
    const res = await getAlerts({ page: alertPage.current, size: alertPage.size })
    if (res.code === 200) {
      const records = res.data.records || []
      alertList.value = records.map(normalizeAlert)
      alertPage.total = res.data.total || alertList.value.length
    }
  } catch (error) {
    ElMessage.error('加载预警记录失败')
  } finally {
    loading.value = false
  }
}

// 确认预警
const handleConfirm = async (id: number) => {
  try {
    await confirmAlert(id)
    ElMessage.success('确认成功')
    loadAlerts()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 检查预警
const handleCheck = async () => {
  checking.value = true
  try {
    const res = await checkAlerts()
    if (res.code === 200) {
      const count = res.data || 0
      ElMessage.success(`检查完成，生成${count}条预警记录`)
      loadAlerts()
    }
  } catch (error) {
    ElMessage.error('检查失败')
  } finally {
    checking.value = false
  }
}

const isDataLoaded = ref(false) // 标记数据是否已加载

onMounted(() => {
  if (!isDataLoaded.value) {
    loadAlerts()
    // 自动触发一次检查
    handleCheck()
    isDataLoaded.value = true
  }
})

// 当组件被激活时（从其他页面返回），如果数据未加载则加载
onActivated(() => {
  if (!isDataLoaded.value) {
    loadAlerts()
    handleCheck()
    isDataLoaded.value = true
  }
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.block-alert {
  margin-bottom: 16px;
}
.rules-explanation {
  font-size: 14px;
  line-height: 1.6;
}
.rules-explanation ul {
  margin: 8px 0;
  padding-left: 20px;
}
.rules-explanation li {
  margin: 4px 0;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
