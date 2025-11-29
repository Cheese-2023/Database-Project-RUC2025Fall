<template>
  <div class="dashboard-container">
    <!-- 主内容区 -->
    <div class="dashboard-main" v-loading="loading" element-loading-text="加载中...">
        <!-- 关键指标卡片 -->
        <el-row :gutter="20" class="stats-row">
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-icon" style="background: #409EFF;">
                  <el-icon :size="32"><Location /></el-icon>
                </div>
                <div class="stat-info">
                  <p class="stat-label">监测县域数</p>
                  <h2 class="stat-value">{{ stats.totalCounties }}</h2>
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
                  <p class="stat-label">低风险县域</p>
                  <h2 class="stat-value">{{ stats.lowRisk }}</h2>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-icon" style="background: #E6A23C;">
                  <el-icon :size="32"><Warning /></el-icon>
                </div>
                <div class="stat-info">
                  <p class="stat-label">中风险县域</p>
                  <h2 class="stat-value">{{ stats.mediumRisk }}</h2>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-icon" style="background: #F56C6C;">
                  <el-icon :size="32"><CircleClose /></el-icon>
                </div>
                <div class="stat-info">
                  <p class="stat-label">高风险县域</p>
                  <h2 class="stat-value">{{ stats.highRisk }}</h2>
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
                <span class="card-title">风险等级分布</span>
              </template>
              <div id="riskDistChart" class="chart"></div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card class="chart-card">
              <template #header>
                <span class="card-title">风险趋势分析</span>
              </template>
              <div id="riskTrendChart" class="chart"></div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 预警列表 -->
        <el-row :gutter="20">
          <el-col :span="24">
            <el-card class="alert-card">
              <template #header>
                <span class="card-title">最新预警信息</span>
              </template>
                <el-table :data="alertList" stripe style="width: 100%" class="custom-table">
                  <el-table-column prop="countyName" label="县域" width="150">
                    <template #default="scope">
                      <span class="county-name">{{ scope.row.countyName }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="riskType" label="风险类型" width="120">
                    <template #default="scope">
                      <el-tag effect="plain" :type="getRiskTypeTag(scope.row.riskType)">
                        {{ scope.row.riskType }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="riskLevel" label="风险等级" width="120">
                    <template #default="scope">
                      <el-tag :type="getRiskLevelType(scope.row.riskLevel)" effect="dark">
                        {{ scope.row.riskLevel }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="description" label="描述">
                    <template #default="scope">
                      <span class="alert-desc">{{ scope.row.description }}</span>
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
import { ref, onMounted, onActivated } from 'vue'
import * as echarts from 'echarts'
import { getRiskStatistics, getAverageRiskTrend, getTopRiskCounties } from '../api/risk'
import { ElMessage } from 'element-plus'

const loading = ref(true)
const isDataLoaded = ref(false) // 标记数据是否已加载

const stats = ref({
  totalCounties: 0,
  lowRisk: 0,
  mediumRisk: 0,
  highRisk: 0
})

const alertList = ref<any[]>([])


const getRiskLevelType = (level: string) => {
  switch (level) {
    case '高风险': return 'danger'
    case '中高风险': return 'warning'
    case '中风险': return 'warning' // Element Plus warning is orange
    case '中低风险': return 'info'
    case '低风险': return 'success'
    default: return 'info'
  }
}

const getRiskTypeTag = (type: string) => {
  if (type.includes('经济')) return 'primary'
  if (type.includes('环境')) return 'success'
  if (type.includes('社会')) return 'warning'
  if (type.includes('综合')) return 'danger'
  return 'info'
}

// 加载统计数据
const loadStatistics = async () => {
  try {
    const res = await getRiskStatistics()
    if (res.code === 200) {
      const data = res.data
      const medium = Number(data.mediumRiskCount || 0)
      const mediumLow = Number(data.mediumLowRiskCount || 0)
      const high = Number(data.highRiskCount || 0)
      const mediumHigh = Number(data.mediumHighRiskCount || 0)
      stats.value = {
        totalCounties: Number(data.totalCount || 0),
        lowRisk: Number(data.lowRiskCount || 0) + mediumLow, // 将中低风险归入低风险统计
        mediumRisk: medium, // 中风险仅包含中风险
        highRisk: high + mediumHigh
      }
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
    ElMessage.error('加载统计数据失败')
  }
}

// 加载高风险县域列表
const loadTopRiskCounties = async () => {
  try {
    const res = await getTopRiskCounties(5)
    if (res.code === 200) {
      alertList.value = res.data.map((item: any) => ({
        countyName: item.county_name || item.countyName,
        provinceName: item.province_name || item.provinceName,
        riskType: '综合风险',
        riskLevel: item.risk_level || item.riskLevel,
        description: `综合风险分: ${item.comprehensive_risk_score || item.comprehensiveRiskScore}`,
        createTime: item.assessment_date || item.assessmentDate || new Date().toLocaleString()
      }))
    }
  } catch (error) {
    console.error('加载预警列表失败:', error)
  }
}

const initCharts = async () => {
  try {
    // 加载风险等级分布数据
    const statsRes = await getRiskStatistics()
    if (statsRes.code === 200) {
      const data = statsRes.data
      
      // 风险等级分布饼图
      const riskDistChart = echarts.init(document.getElementById('riskDistChart')!)
      riskDistChart.setOption({
        tooltip: {
          trigger: 'item'
        },
        legend: {
          bottom: '5%',
          left: 'center'
        },
        series: [
          {
            name: '风险等级',
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 10,
              borderColor: '#fff',
              borderWidth: 2
            },
            label: {
              show: false,
              position: 'center'
            },
            emphasis: {
              label: {
                show: true,
                fontSize: 20,
                fontWeight: 'bold'
              }
            },
            data: [
              { value: parseInt(data.lowRiskCount) || 0, name: '低风险', itemStyle: { color: '#67C23A' } },
              { value: parseInt(data.mediumLowRiskCount) || 0, name: '中低风险', itemStyle: { color: '#95D475' } },
              { value: parseInt(data.mediumRiskCount) || 0, name: '中风险', itemStyle: { color: '#E6A23C' } },
              { value: parseInt(data.mediumHighRiskCount) || 0, name: '中高风险', itemStyle: { color: '#F78989' } },
              { value: parseInt(data.highRiskCount) || 0, name: '高风险', itemStyle: { color: '#F56C6C' } }
            ]
          }
        ]
      })
    }
    
    // 加载风险趋势数据
    const trendRes = await getAverageRiskTrend()
    if (trendRes.code === 200) {
      const trendData = trendRes.data
      
      // 风险趋势折线图
      const riskTrendChart = echarts.init(document.getElementById('riskTrendChart')!)
      riskTrendChart.setOption({
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: trendData.map((item: any) => formatYear(item.year)),
          axisLabel: {
            rotate: 45
          }
        },
        yAxis: {
          type: 'value',
          name: '平均风险分',
          min: 0,
          max: 100
        },
        series: [
          {
            name: '平均风险分',
            type: 'line',
            data: trendData.map((item: any) => parseFloat(item.avgScore).toFixed(2)),
            smooth: true,
            itemStyle: {
              color: '#409EFF'
            },
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
          }
        ],
        grid: {
          left: '3%',
          right: '4%',
          bottom: '15%',
          containLabel: true
        }
      })
    }
  } catch (error) {
    console.error('初始化图表失败:', error)
    ElMessage.error('加载图表数据失败')
  } finally {
    loading.value = false
  }
}

const formatYear = (value: string | number) => {
  if (!value) return value
  if (typeof value === 'number') return value.toString()
  if (value.includes('-')) {
    return value.split('-')[0]
  }
  return value
}

onMounted(async () => {
  // 如果数据已加载（通过 keep-alive 缓存），则不再重新加载
  if (isDataLoaded.value) {
    return
  }
  
  loading.value = true
  try {
    await loadStatistics()
    await loadTopRiskCounties()
    await initCharts()
    isDataLoaded.value = true
  } finally {
    loading.value = false
  }
})

// 当组件被激活时（从其他页面返回），如果数据未加载则加载
onActivated(async () => {
  if (!isDataLoaded.value && !loading.value) {
    loading.value = true
    try {
      await loadStatistics()
      await loadTopRiskCounties()
      await initCharts()
      isDataLoaded.value = true
    } finally {
      loading.value = false
    }
  }
})
</script>

<style scoped lang="scss">
.dashboard-container {
  width: 100%;
  height: 100vh;
  background: #f0f2f5;
}

.dashboard-header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 0 20px;
  display: flex;
  flex-direction: column;
  height: auto !important;
}

.header-title {
  padding: 20px 0 10px;
  text-align: center;
  
  h1 {
    font-size: 28px;
    color: #303133;
    margin: 0;
  }
  
  .subtitle {
    font-size: 14px;
    color: #909399;
    margin: 5px 0 0;
  }
}

.header-nav {
  :deep(.el-menu) {
    border-bottom: none;
  }
}

.dashboard-main {
  padding: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  .stat-content {
    display: flex;
    align-items: center;
    
    .stat-icon {
      width: 60px;
      height: 60px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      margin-right: 15px;
    }
    
    .stat-info {
      flex: 1;
      
      .stat-label {
        font-size: 14px;
        color: #909399;
        margin: 0 0 5px;
      }
      
      .stat-value {
        font-size: 28px;
        font-weight: bold;
        color: #303133;
        margin: 0;
      }
    }
  }
}

.charts-row {
  margin-bottom: 20px;
}

.chart-card, .alert-card {
  .card-title {
    font-size: 16px;
    font-weight: bold;
    color: #303133;
  }
  
  .chart {
    width: 100%;
    height: 350px;
  }
}

.custom-table {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  
  :deep(.el-table__header) {
    background-color: #f5f7fa;
    color: #606266;
    font-weight: 600;
  }
  
  .county-name {
    font-weight: bold;
    color: #303133;
  }
  
  .alert-desc {
    color: #606266;
  }
}
</style>
