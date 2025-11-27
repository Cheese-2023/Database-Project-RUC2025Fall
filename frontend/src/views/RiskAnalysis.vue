<template>
  <div class="page-container">
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card" shadow="never">
          <p class="stat-label">监测县域数</p>
          <p class="stat-value">{{ stats.totalCounties }}</p>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="never">
          <p class="stat-label">低风险县域</p>
          <p class="stat-value text-success">{{ stats.lowRisk }}</p>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="never">
          <p class="stat-label">中风险县域</p>
          <p class="stat-value text-warning">{{ stats.mediumRisk }}</p>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="never">
          <p class="stat-label">高风险县域</p>
          <p class="stat-value text-danger">{{ stats.highRisk }}</p>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="filters">
        <el-form-item label="风险等级">
          <el-select v-model="filters.level" placeholder="全部等级" clearable>
            <el-option v-for="item in riskLevelOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="省份">
          <el-select v-model="filters.provinceName" placeholder="全部省份" filterable clearable>
            <el-option v-for="item in provinceOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="年份">
          <el-select v-model="filters.year" placeholder="最新年份" clearable>
            <el-option v-for="item in yearOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="20" class="content-row">
      <el-col :span="14">
        <el-card shadow="never" class="table-card">
          <template #header>
            <div class="card-header">
              <span>县域风险列表</span>
              <span class="sub-text">双击行可查看风险趋势</span>
            </div>
          </template>
          <el-table
            :data="riskList"
            border
            height="520"
            v-loading="listLoading"
            :empty-text="listLoading ? '数据加载中...' : '暂无数据，调整筛选条件后重试'"
            @row-dblclick="handleRowDblClick"
          >
            <el-table-column prop="county_name" label="县域" width="160" />
            <el-table-column prop="province_name" label="省份" width="140" />
            <el-table-column prop="risk_level" label="风险等级" width="120">
              <template #default="scope">
                <el-tag :type="getRiskLevelType(scope.row.risk_level)">
                  {{ scope.row.risk_level }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="comprehensive_risk_score" label="综合得分" width="120">
              <template #default="scope">
                {{ Number(scope.row.comprehensive_risk_score).toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="economic_risk_score" label="经济" width="100" />
            <el-table-column prop="social_risk_score" label="社会" width="100" />
            <el-table-column prop="environment_risk_score" label="环境" width="100" />
            <el-table-column prop="development_risk_score" label="发展" width="100" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>{{ selectedCounty.name ? `${selectedCounty.name} 风险趋势` : '风险趋势' }}</span>
              <el-tag v-if="selectedCounty.level" size="small" :type="getRiskLevelType(selectedCounty.level)">
                当前 {{ selectedCounty.level }}
              </el-tag>
            </div>
          </template>
          <div v-if="trendData.length === 0 && !trendLoading" class="chart-placeholder">
            <el-empty description="请选择县域查看风险趋势" />
          </div>
          <div v-else ref="trendChartRef" class="chart" v-loading="trendLoading" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import * as echarts from 'echarts'
import { getRiskStatistics, getRiskList, getRiskTrend } from '@/api/risk'
import { countyApi } from '@/api/county'
import { ElMessage } from 'element-plus'

interface Filters {
  level: string
  provinceName: string
  year: string | number | null
}

const stats = ref({
  totalCounties: 0,
  lowRisk: 0,
  mediumRisk: 0,
  highRisk: 0
})

const filters = reactive<Filters>({
  level: '',
  provinceName: '',
  year: null
})

const riskLevelOptions = ['低风险', '中低风险', '中风险', '中高风险', '高风险']
const yearOptions = ref<number[]>(Array.from({ length: 15 }, (_, idx) => new Date().getFullYear() - idx))
const provinceOptions = ref<string[]>([])

const riskList = ref<any[]>([])
const listLoading = ref(false)

const selectedCounty = ref({ code: '', name: '', level: '' })
const trendData = ref<any[]>([])
const trendChartRef = ref<HTMLDivElement | null>(null)
let trendChart: echarts.ECharts | null = null
const trendLoading = ref(false)

const loadStatistics = async () => {
  try {
    const res = await getRiskStatistics()
    if (res.code === 200 && res.data) {
      // 使用和Dashboard完全相同的逻辑
      stats.value.totalCounties = Number(res.data.totalCount || 0)
      stats.value.lowRisk = Number(res.data.lowRiskCount || 0) + Number(res.data.mediumLowRiskCount || 0)
      stats.value.mediumRisk = Number(res.data.mediumRiskCount || 0)  // 仅中风险
      stats.value.highRisk = Number(res.data.mediumHighRiskCount || 0) + Number(res.data.highRiskCount || 0)  // 中高+高风险
    }
  } catch (error) {
    ElMessage.error('加载统计数据失败')
  }
}

const loadProvinces = async () => {
  try {
    const res = await countyApi.getCountyList()
    if (res.code === 200 && Array.isArray(res.data)) {
      const set = new Set<string>()
      res.data.forEach((item: any) => {
        if (item.provinceName) {
          set.add(item.provinceName)
        }
      })
      provinceOptions.value = Array.from(set)
    }
  } catch (error) {
    console.error(error)
  }
}

const loadRiskList = async () => {
  listLoading.value = true
  try {
    const params: Record<string, any> = {}
    if (filters.level) params.level = filters.level
    if (filters.provinceName) params.provinceName = filters.provinceName
    if (filters.year) params.year = Number(filters.year)
    const res = await getRiskList(params)
    if (res.code === 200) {
      const list = res.data || []
      riskList.value = list
      refreshYearOptions(list)
      if (list.length) {
        autoSelectCounty(list[0])
      } else {
        selectedCounty.value = { code: '', name: '', level: '' }
        trendData.value = []
        trendChart?.clear()
      }
    }
  } catch (error) {
    ElMessage.error('加载风险列表失败')
  } finally {
    listLoading.value = false
  }
}

const handleSearch = () => {
  loadRiskList()
}

const handleReset = () => {
  filters.level = ''
  filters.provinceName = ''
  filters.year = null
  loadRiskList()
}

const handleRowDblClick = (row: any) => {
  autoSelectCounty(row)
}

const autoSelectCounty = (row: any) => {
  if (!row) return
  selectedCounty.value = {
    code: row.county_code,
    name: row.county_name,
    level: row.risk_level
  }
  loadRiskTrend(row.county_code)
}

const loadRiskTrend = async (countyCode: string) => {
  if (!countyCode) return
  console.log('[RiskAnalysis] Loading trend for county:', countyCode)
  trendLoading.value = true
  try {
    const res = await getRiskTrend(countyCode)
    console.log('[RiskAnalysis] Trend API response:', res)
    if (res.code === 200) {
      trendData.value = res.data || []
      console.log('[RiskAnalysis] Trend data:', trendData.value)
      if (!trendData.value.length) {
        ElMessage.info('该县暂无历史风险记录')
      } else {
        console.log('[RiskAnalysis] Rendering chart with', trendData.value.length, 'data points')
        // 等待DOM更新后再渲染图表
        setTimeout(() => renderTrendChart(), 100)
      }
    }
  } catch (error) {
    console.error('[RiskAnalysis] Load trend error:', error)
    ElMessage.error('加载风险趋势失败')
  } finally {
    trendLoading.value = false
  }
}

const renderTrendChart = () => {
  if (!trendChartRef.value) {
    console.error('[RiskAnalysis] Chart container ref not found!')
    return
  }
  if (!trendData.value.length) {
    console.warn('[RiskAnalysis] No trend data to render')
    return
  }
  
  console.log('[RiskAnalysis] Initializing/updating chart')
  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
    console.log('[RiskAnalysis] Chart instance created')
  } else {
    // 清空现有图表数据
    trendChart.clear()
    console.log('[RiskAnalysis] Chart cleared for new data')
  }
  
  const years = trendData.value.map((item: any) => formatYear(item.year))
  const scores = trendData.value.map((item: any) => Number(item.comprehensive_risk_score || item.avgScore || 0).toFixed(2))
  
  console.log('[RiskAnalysis] Chart data - Years:', years)
  console.log('[RiskAnalysis] Chart data - Scores:', scores)
  
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '8%', containLabel: true },
    xAxis: {
      type: 'category',
      data: years,
      axisLabel: { interval: years.length > 8 ? 1 : 0 }
    },
    yAxis: {
      type: 'value',
      name: '综合风险分'
    },
    series: [
      {
        name: '综合风险分',
        type: 'line',
        data: scores,
        smooth: true,
        areaStyle: {
          color: 'rgba(64,158,255,0.2)'
        },
        lineStyle: {
          color: '#409EFF'
        }
      }
    ]
  })
  
  // 触发resize确保图表正确显示
  trendChart.resize()
  console.log('[RiskAnalysis] Chart rendered successfully')
}

const formatYear = (value: string | number) => {
  if (!value) return value
  if (typeof value === 'number') return value
  return value.split('-')[0]
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

const refreshYearOptions = (list: any[]) => {
  const set = new Set<number>()
  list.forEach((item) => {
    const year = Number(formatYear(item.year))
    if (!Number.isNaN(year)) {
      set.add(year)
    }
  })
  if (set.size) {
    yearOptions.value = Array.from(set).sort((a, b) => b - a)
  }
}

onMounted(() => {
  loadStatistics()
  loadProvinces()
  loadRiskList()
})

onBeforeUnmount(() => {
  trendChart?.dispose()
  trendChart = null
})
</script>

<style scoped lang="scss">
.page-container {
  padding: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  .stat-label {
    color: #909399;
    margin-bottom: 6px;
  }
  .stat-value {
    font-size: 26px;
    font-weight: bold;
    margin: 0;
  }
  .text-success {
    color: #67c23a;
  }
  .text-warning {
    color: #e6a23c;
  }
  .text-danger {
    color: #f56c6c;
  }
}

.filter-card {
  margin-bottom: 20px;
}

.content-row {
  margin-top: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  .sub-text {
    font-size: 12px;
    color: #909399;
  }
}

.chart-card {
  height: 600px;
}

.chart {
  width: 100%;
  height: 520px;
}

.chart-placeholder {
  height: 520px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  border-radius: 4px;
}
</style>
