<template>
  <div class="page-container" v-loading="pageLoading">
    <el-row :gutter="20" class="info-row">
      <el-col :span="10">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>服务信息</span>
            </div>
          </template>
          <el-descriptions :column="1" v-if="overview.serviceInfo?.name">
            <el-descriptions-item label="名称">{{ overview.serviceInfo.name }}</el-descriptions-item>
            <el-descriptions-item label="版本">{{ overview.serviceInfo.version }}</el-descriptions-item>
            <el-descriptions-item label="启动时间">{{ formatDate(overview.serviceInfo.startTime) }}</el-descriptions-item>
            <el-descriptions-item label="运行时长">{{ uptimeLabel }}</el-descriptions-item>
            <el-descriptions-item label="Java 版本">{{ overview.serviceInfo.javaVersion }}</el-descriptions-item>
            <el-descriptions-item label="部署环境">{{ overview.serviceInfo.environment }}</el-descriptions-item>
          </el-descriptions>
          <div v-else class="empty-block">
            <el-empty description="暂无服务信息" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="14">
        <el-card shadow="never" class="metrics-card">
          <template #header>
            <div class="card-header">
              <span>系统指标</span>
            </div>
          </template>
          <el-row :gutter="15">
            <el-col v-for="item in metricItems" :key="item.key" :span="8">
              <div class="metric-item">
                <p class="metric-label">{{ item.label }}</p>
                <p class="metric-value">{{ item.value }}</p>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="data-row">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>最新预警</span>
            </div>
          </template>
          <el-table :data="overview.recentAlerts" height="320" stripe>
            <el-table-column prop="countyName" label="县域" width="140" />
            <el-table-column prop="alertType" label="类型" width="110" />
            <el-table-column prop="riskLevel" label="风险等级" width="120">
              <template #default="scope">
                <el-tag :type="getRiskLevelType(scope.row.riskLevel)">
                  {{ scope.row.riskLevel }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="title" label="标题" />
            <el-table-column prop="createdAt" label="时间" width="180">
              <template #default="scope">
                {{ formatDate(scope.row.createdAt) }}
              </template>
            </el-table-column>
          </el-table>
          <div v-if="!overview.recentAlerts?.length" class="empty-block">
            <el-empty description="暂无预警" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>数据质量待办</span>
            </div>
          </template>
          <el-table :data="overview.pendingIssues" height="320" stripe>
            <el-table-column prop="tableName" label="表名" width="140" />
            <el-table-column prop="countyName" label="县域" width="140" />
            <el-table-column prop="severity" label="严重程度" width="120">
              <template #default="scope">
                <el-tag :type="getSeverityType(scope.row.severity)">
                  {{ scope.row.severity }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="issueDescription" label="问题描述" />
            <el-table-column prop="detectedAt" label="发现时间" width="180">
              <template #default="scope">
                {{ formatDate(scope.row.detectedAt) }}
              </template>
            </el-table-column>
          </el-table>
          <div v-if="!overview.pendingIssues?.length" class="empty-block">
            <el-empty description="暂无数据质量问题" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <div>
            <span>系统配置</span>
            <span class="sub-text">配置修改后需刷新服务方可生效</span>
          </div>
          <el-select v-model="categoryFilter" placeholder="按分类过滤" clearable style="width: 200px">
            <el-option v-for="item in categoryOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </div>
      </template>
      <el-table :data="filteredConfigs" border v-loading="configLoading">
        <el-table-column prop="configKey" label="配置项" width="200" />
        <el-table-column prop="configValue" label="当前值" />
        <el-table-column prop="category" label="分类" width="160" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="updatedAt" label="更新时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.updatedAt) || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button type="primary" text size="small" @click="handleEdit(scope.row)">
              编辑
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="!filteredConfigs.length && !configLoading" class="empty-block">
        <el-empty description="暂无配置" />
      </div>
    </el-card>

    <el-dialog v-model="editDialogVisible" title="编辑配置" width="400px">
      <el-form :model="editForm" label-width="90px">
        <el-form-item label="配置项">
          <el-input v-model="editForm.configKey" disabled />
        </el-form-item>
        <el-form-item label="配置值">
          <el-input v-model="editForm.configValue" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="editForm.description" type="textarea" :rows="3" disabled />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="editSubmitting" @click="submitEdit">
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { systemApi } from '@/api/system'
import { ElMessage } from 'element-plus'

interface ServiceInfo {
  name: string
  version: string
  startTime: string
  uptimeSeconds: number
  javaVersion: string
  environment: string
}

interface Metrics {
  totalCounties: number
  riskRecords: number
  alertCount: number
  pendingAlerts: number
  resolvedAlerts: number
  dataIssues: number
  unresolvedIssues: number
}

interface AlertSummary {
  alertId: number
  countyName: string
  provinceName: string
  riskLevel: string
  alertType: string
  status: string
  createdAt: string
  title: string
}

interface IssueSummary {
  id: number
  tableName: string
  countyName: string
  severity: string
  status: string
  detectedAt: string
  issueDescription: string
}

interface SystemConfigItem {
  configId: number
  configKey: string
  configValue: string
  configType: string
  category: string
  description: string
  isEditable: boolean
  updatedAt: string
}

const pageLoading = ref(false)
const overview = reactive({
  serviceInfo: {} as Partial<ServiceInfo>,
  metrics: {} as Partial<Metrics>,
  recentAlerts: [] as AlertSummary[],
  pendingIssues: [] as IssueSummary[]
})

const allConfigs = ref<SystemConfigItem[]>([])
const configLoading = ref(false)
const categoryFilter = ref('')

const categoryOptions = computed(() => {
  const set = new Set<string>()
  allConfigs.value.forEach((item) => {
    if (item.category) set.add(item.category)
  })
  return Array.from(set)
})

const filteredConfigs = computed(() => {
  if (!categoryFilter.value) {
    return allConfigs.value
  }
  return allConfigs.value.filter((item) => item.category === categoryFilter.value)
})

const metricItems = computed(() => {
  const metrics = overview.metrics || {}
  return [
    { key: 'totalCounties', label: '监测县域', value: metrics.totalCounties ?? 0 },
    { key: 'riskRecords', label: '风险评估记录', value: metrics.riskRecords ?? 0 },
    { key: 'alertCount', label: '累计预警', value: metrics.alertCount ?? 0 },
    { key: 'pendingAlerts', label: '待处理预警', value: metrics.pendingAlerts ?? 0 },
    { key: 'resolvedAlerts', label: '已处理预警', value: metrics.resolvedAlerts ?? 0 },
    { key: 'dataIssues', label: '数据问题', value: metrics.dataIssues ?? 0 }
  ]
})

const uptimeLabel = computed(() => {
  const seconds = overview.serviceInfo?.uptimeSeconds || 0
  if (!seconds) return '-'
  const days = Math.floor(seconds / 86400)
  const hours = Math.floor((seconds % 86400) / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  return `${days}天 ${hours}小时 ${minutes}分钟`
})

const editDialogVisible = ref(false)
const editSubmitting = ref(false)
const editForm = reactive({
  configId: 0,
  configKey: '',
  configValue: '',
  description: ''
})

const loadOverview = async () => {
  pageLoading.value = true
  try {
    const res = await systemApi.getOverview()
    Object.assign(overview, res.data || {})
  } catch (error) {
    ElMessage.error('加载系统概览失败')
  } finally {
    pageLoading.value = false
  }
}

const loadConfigs = async () => {
  configLoading.value = true
  try {
    const res = await systemApi.getConfigs()
    allConfigs.value = res.data || []
  } catch (error) {
    ElMessage.error('加载系统配置失败')
  } finally {
    configLoading.value = false
  }
}

const handleEdit = (row: SystemConfigItem) => {
  if (!row.isEditable) {
    ElMessage.warning('该配置不可编辑')
    return
  }
  editForm.configId = row.configId
  editForm.configKey = row.configKey
  editForm.configValue = row.configValue
  editForm.description = row.description || ''
  editDialogVisible.value = true
}

const submitEdit = async () => {
  if (!editForm.configId) return
  editSubmitting.value = true
  try {
    await systemApi.updateConfig(editForm.configId, { configValue: editForm.configValue })
    ElMessage.success('配置已更新')
    editDialogVisible.value = false
    loadConfigs()
  } catch (error) {
    // 错误提示由请求拦截器处理
  } finally {
    editSubmitting.value = false
  }
}

const formatDate = (value?: string | number | null) => {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  return date.toLocaleString()
}

const getRiskLevelType = (level: string) => {
  const map: Record<string, any> = {
    '低风险': 'success',
    '中低风险': 'info',
    '中风险': 'warning',
    '中高风险': 'warning',
    '高风险': 'danger'
  }
  return map[level] || 'info'
}

const getSeverityType = (severity: string) => {
  const map: Record<string, any> = {
    high: 'danger',
    medium: 'warning',
    low: 'info',
    高: 'danger',
    中: 'warning',
    低: 'success'
  }
  if (!severity) return 'info'
  const key = typeof severity === 'string' ? severity.toLowerCase() : severity
  return map[key] || map[severity] || 'info'
}

onMounted(() => {
  loadOverview()
  loadConfigs()
})
</script>

<style scoped lang="scss">
.page-container {
  padding: 20px;
}

.info-row {
  margin-bottom: 20px;
}

.metrics-card {
  height: 100%;
}

.metric-item {
  background: #f5f7fa;
  border-radius: 6px;
  padding: 16px;
  margin-bottom: 15px;
  .metric-label {
    color: #909399;
    margin: 0 0 6px;
  }
  .metric-value {
    font-size: 24px;
    font-weight: 600;
    margin: 0;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  .sub-text {
    font-size: 12px;
    color: #909399;
    margin-left: 10px;
  }
}

.data-row {
  margin-bottom: 20px;
}

.empty-block {
  padding: 30px 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style>
