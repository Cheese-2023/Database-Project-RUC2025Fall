<template>
  <div class="poverty-achievement-container">
    <div class="page-header">
      <h1>脱贫攻坚成果展示</h1>
      <p class="subtitle">832个国家级贫困县摘帽后经济变化分析</p>
    </div>

    <div v-loading="loading" element-loading-text="加载中...">
      <!-- 统计概览卡片 -->
      <el-row :gutter="20" class="stats-row">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background: #409EFF;">
                <el-icon :size="32"><Location /></el-icon>
              </div>
              <div class="stat-info">
                <p class="stat-label">贫困县总数</p>
                <h2 class="stat-value">{{ overview.totalCount || 0 }}</h2>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background: #67C23A;">
                <el-icon :size="32"><SuccessFilled /></el-icon>
              </div>
              <div class="stat-info">
                <p class="stat-label">已摘帽县数</p>
                <h2 class="stat-value">{{ overview.totalCount || 0 }}</h2>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background: #E6A23C;">
                <el-icon :size="32"><TrendCharts /></el-icon>
              </div>
              <div class="stat-info">
                <p class="stat-label">GDP平均增长率</p>
                <h2 class="stat-value">{{ comparison.growth?.gdpGrowth?.toFixed(1) || '0' }}%</h2>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background: #F56C6C;">
                <el-icon :size="32"><Money /></el-icon>
              </div>
              <div class="stat-info">
                <p class="stat-label">农村收入增长率</p>
                <h2 class="stat-value">{{ comparison.growth?.ruralIncomeGrowth?.toFixed(1) || '0' }}%</h2>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 图表区域 -->
      <el-row :gutter="20" class="charts-row">
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header>
              <span class="card-title">按省份分布</span>
            </template>
            <div id="provinceChart" class="chart"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header>
              <span class="card-title">按摘帽年份分布</span>
            </template>
            <div id="delistingYearChart" class="chart"></div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 摘帽前后对比 -->
      <el-row :gutter="20" class="charts-row">
        <el-col :span="24">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span class="card-title">摘帽前后经济指标对比</span>
                <el-select v-model="selectedIndicator" @change="loadComparison" style="width: 200px;">
                  <el-option label="GDP" value="gdp" />
                  <el-option label="人均GDP" value="gdpPerCapita" />
                  <el-option label="财政收入" value="fiscalRevenue" />
                  <el-option label="城镇居民收入" value="urbanIncome" />
                  <el-option label="农村居民收入" value="ruralIncome" />
                </el-select>
              </div>
            </template>
            <div id="comparisonChart" class="chart"></div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 经济指标趋势 -->
      <el-row :gutter="20" class="charts-row">
        <el-col :span="24">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span class="card-title">贫困县经济指标趋势</span>
                <el-select v-model="trendIndicator" @change="loadTrend" style="width: 200px;">
                  <el-option label="GDP" value="gdp" />
                  <el-option label="人均GDP" value="gdpPerCapita" />
                  <el-option label="财政收入" value="fiscalRevenue" />
                </el-select>
              </div>
            </template>
            <div id="trendChart" class="chart"></div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 贫困县列表 -->
      <el-row :gutter="20">
        <el-col :span="24">
          <el-card class="list-card">
            <template #header>
              <div class="card-header">
                <span class="card-title">贫困县列表</span>
                <div class="filter-group">
                  <el-select v-model="filterProvince" placeholder="选择省份" clearable @change="loadCountyList" style="width: 150px; margin-right: 10px;">
                    <el-option v-for="province in provinceOptions" :key="province" :label="province" :value="province" />
                  </el-select>
                  <el-button type="primary" @click="loadCountyList">查询</el-button>
                </div>
              </div>
            </template>
            <el-table :data="countyList" stripe style="width: 100%" v-loading="listLoading">
              <el-table-column prop="countyName" label="县名" width="150" />
              <el-table-column prop="provinceName" label="省份" width="120" />
              <el-table-column prop="cityName" label="地级市" width="120" />
              <el-table-column label="操作" width="120">
                <template #default="scope">
                  <el-button type="primary" size="small" @click="viewDetail(scope.row.countyCode)">查看详情</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { Location, SuccessFilled, TrendCharts, Money } from '@element-plus/icons-vue'
import {
  getPovertyOverview,
  getStatisticsByProvince,
  getStatisticsByDelistingYear,
  getDelistingComparison,
  getPovertyCountyList,
  getEconomicTrend
} from '../api/poverty'

const loading = ref(false)
const listLoading = ref(false)

const overview = ref<any>({})
const comparison = ref<any>({})
const provinceStats = ref<any[]>([])
const delistingYearStats = ref<any[]>([])
const countyList = ref<any[]>([])
const provinceOptions = ref<string[]>([])

const selectedIndicator = ref('gdp')
const trendIndicator = ref('gdp')
const filterProvince = ref('')

let provinceChart: echarts.ECharts | null = null
let delistingYearChart: echarts.ECharts | null = null
let comparisonChart: echarts.ECharts | null = null
let trendChart: echarts.ECharts | null = null

const loadOverview = async () => {
  try {
    const res = await getPovertyOverview()
    if (res.code === 200) {
      overview.value = res.data || {}
    }
  } catch (error) {
    ElMessage.error('加载概览数据失败')
  }
}

const loadProvinceStats = async () => {
  try {
    const res = await getStatisticsByProvince()
    if (res.code === 200) {
      provinceStats.value = res.data || []
      renderProvinceChart()
    }
  } catch (error) {
    ElMessage.error('加载省份统计数据失败')
  }
}

const loadDelistingYearStats = async () => {
  try {
    const res = await getStatisticsByDelistingYear()
    if (res.code === 200) {
      delistingYearStats.value = res.data || []
      renderDelistingYearChart()
    }
  } catch (error) {
    ElMessage.error('加载摘帽年份统计数据失败')
  }
}

const loadComparison = async () => {
  try {
    const res = await getDelistingComparison()
    if (res.code === 200) {
      comparison.value = res.data || {}
      renderComparisonChart()
    }
  } catch (error) {
    ElMessage.error('加载对比数据失败')
  }
}

const loadTrend = async () => {
  try {
    const res = await getEconomicTrend(trendIndicator.value)
    if (res.code === 200) {
      const trendData = res.data?.trend || []
      renderTrendChart(trendData)
    }
  } catch (error) {
    ElMessage.error('加载趋势数据失败')
  }
}

const loadCountyList = async () => {
  listLoading.value = true
  try {
    const res = await getPovertyCountyList(filterProvince.value || undefined)
    if (res.code === 200) {
      countyList.value = res.data || []
      // 提取省份选项
      const provinces = new Set<string>()
      countyList.value.forEach((item: any) => {
        if (item.provinceName) {
          provinces.add(item.provinceName)
        }
      })
      provinceOptions.value = Array.from(provinces).sort()
    }
  } catch (error) {
    ElMessage.error('加载贫困县列表失败')
  } finally {
    listLoading.value = false
  }
}

const viewDetail = (countyCode: string) => {
  ElMessage.info(`查看 ${countyCode} 的详细信息（功能开发中）`)
}

const renderProvinceChart = () => {
  if (!provinceChart) {
    provinceChart = echarts.init(document.getElementById('provinceChart')!)
  }
  
  const data = provinceStats.value.slice(0, 10) // 只显示前10个省份
  provinceChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    xAxis: {
      type: 'category',
      data: data.map((item: any) => item.province || item.provinceName),
      axisLabel: { rotate: 45 }
    },
    yAxis: {
      type: 'value',
      name: '贫困县数量'
    },
    series: [{
      name: '贫困县数量',
      type: 'bar',
      data: data.map((item: any) => item.count || item.count),
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#83bff6' },
          { offset: 0.5, color: '#188df0' },
          { offset: 1, color: '#188df0' }
        ])
      }
    }]
  })
}

const renderDelistingYearChart = () => {
  if (!delistingYearChart) {
    delistingYearChart = echarts.init(document.getElementById('delistingYearChart')!)
  }
  
  delistingYearChart.setOption({
    tooltip: {
      trigger: 'item'
    },
    legend: {
      bottom: '5%',
      left: 'center'
    },
    series: [{
      name: '摘帽年份',
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: false
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 16,
          fontWeight: 'bold'
        }
      },
      data: delistingYearStats.value.map((item: any) => ({
        value: item.count,
        name: `${extractYear(item.year)}年`
      }))
    }]
  })
}

const renderComparisonChart = () => {
  if (!comparisonChart) {
    comparisonChart = echarts.init(document.getElementById('comparisonChart')!)
  }
  
  const before = comparison.value.before || {}
  const after = comparison.value.after || {}
  
  let indicatorKey = 'avgGdp'
  let indicatorName = 'GDP (万元)'
  
  switch (selectedIndicator.value) {
    case 'gdp':
      indicatorKey = 'avgGdp'
      indicatorName = 'GDP (万元)'
      break
    case 'gdpPerCapita':
      indicatorKey = 'avgGdpPerCapita'
      indicatorName = '人均GDP (元)'
      break
    case 'fiscalRevenue':
      indicatorKey = 'avgFiscalRevenue'
      indicatorName = '财政收入 (万元)'
      break
    case 'urbanIncome':
      indicatorKey = 'avgUrbanIncome'
      indicatorName = '城镇居民收入 (元)'
      break
    case 'ruralIncome':
      indicatorKey = 'avgRuralIncome'
      indicatorName = '农村居民收入 (元)'
      break
  }
  
  const beforeValue = before[indicatorKey] || 0
  const afterValue = after[indicatorKey] || 0
  
  comparisonChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    xAxis: {
      type: 'category',
      data: ['摘帽前', '摘帽后']
    },
    yAxis: {
      type: 'value',
      name: indicatorName
    },
    series: [{
      name: indicatorName,
      type: 'bar',
      data: [beforeValue, afterValue],
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#83bff6' },
          { offset: 0.5, color: '#188df0' },
          { offset: 1, color: '#188df0' }
        ])
      },
      label: {
        show: true,
        position: 'top',
        formatter: (params: any) => {
          const value = params.value
          if (value >= 10000) {
            return (value / 10000).toFixed(2) + '万'
          }
          return value.toFixed(2)
        }
      }
    }]
  })
}

// 辅助函数：从year字段中提取年份（支持整数、日期字符串等）
const extractYear = (year: any): string => {
  if (!year) return ''
  if (typeof year === 'number') return year.toString()
  if (typeof year === 'string') {
    // 如果是日期字符串（如 "2000-01-01"），提取年份
    const match = year.match(/^(\d{4})/)
    if (match) return match[1]
    return year
  }
  return String(year)
}

const renderTrendChart = (trendData: any[]) => {
  if (!trendChart) {
    trendChart = echarts.init(document.getElementById('trendChart')!)
  }
  
  let dataKey = 'avgGdp'
  let name = 'GDP (万元)'
  
  switch (trendIndicator.value) {
    case 'gdp':
      dataKey = 'avgGdp'
      name = 'GDP (万元)'
      break
    case 'gdpPerCapita':
      dataKey = 'avgGdpPerCapita'
      name = '人均GDP (元)'
      break
    case 'fiscalRevenue':
      dataKey = 'avgFiscalRevenue'
      name = '财政收入 (万元)'
      break
  }
  
  trendChart.setOption({
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: trendData.map((item: any) => extractYear(item.year))
    },
    yAxis: {
      type: 'value',
      name: name
    },
    series: [{
      name: name,
      type: 'line',
      data: trendData.map((item: any) => item[dataKey] || 0),
      smooth: true,
      itemStyle: { color: '#409EFF' },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(64, 158, 255, 0.5)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
          ]
        }
      }
    }]
  })
}

onMounted(async () => {
  loading.value = true
  try {
    await Promise.all([
      loadOverview(),
      loadProvinceStats(),
      loadDelistingYearStats(),
      loadComparison(),
      loadTrend(),
      loadCountyList()
    ])
  } finally {
    loading.value = false
  }
  
  // 响应式调整图表大小
  window.addEventListener('resize', () => {
    provinceChart?.resize()
    delistingYearChart?.resize()
    comparisonChart?.resize()
    trendChart?.resize()
  })
})
</script>

<style scoped lang="scss">
.poverty-achievement-container {
  padding: 20px;
  background: #f5f5f5;
  min-height: calc(100vh - 80px);
}

.page-header {
  margin-bottom: 20px;
  h1 {
    font-size: 28px;
    color: #303133;
    margin-bottom: 8px;
  }
  .subtitle {
    color: #909399;
    font-size: 14px;
  }
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  .stat-content {
    display: flex;
    align-items: center;
    gap: 15px;
  }
  .stat-icon {
    width: 60px;
    height: 60px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
  }
  .stat-info {
    flex: 1;
    .stat-label {
      color: #909399;
      font-size: 14px;
      margin-bottom: 8px;
    }
    .stat-value {
      font-size: 28px;
      font-weight: bold;
      color: #303133;
      margin: 0;
    }
  }
}

.charts-row {
  margin-bottom: 20px;
}

.chart-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  .card-title {
    font-size: 16px;
    font-weight: bold;
    color: #303133;
  }
  .chart {
    width: 100%;
    height: 400px;
  }
}

.list-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  .filter-group {
    display: flex;
    align-items: center;
  }
}
</style>


