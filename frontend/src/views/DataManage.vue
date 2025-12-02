<template>
  <div class="data-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>风险计算配置</span>
          <div>
            <el-button 
              v-if="canAdjust"
              type="success" 
              @click="handleRecalculate" 
              :loading="calculating"
            >
              立即重新计算风险
            </el-button>
            <el-button 
              v-if="canAdjust"
              type="primary" 
              @click="handleRestoreDefaults"
            >
              恢复默认配置
            </el-button>
            <el-tag v-else type="warning">您没有权限调整风险参数</el-tag>
          </div>
        </div>
      </template>

      <el-alert
        class="info-alert"
        type="warning"
        show-icon
        :closable="false"
        title="在此修改风险计算公式的权重和阈值。修改后，请点击“立即重新计算风险”以更新全系统的风险评估结果。"
      >
        <template #default>
          <div class="formula-explanation">
            <p><strong>计算公式说明：</strong></p>
            <p>1. <strong>综合风险得分</strong> = Σ (维度得分 × 维度权重)</p>
            <p>2. <strong>维度得分</strong> = Σ (指标得分 × 指标权重)</p>
            <p>3. <strong>指标得分计算规则：</strong></p>
            <ul>
              <li>如果 指标值 > 高风险阈值，得分 = 100</li>
              <li>如果 指标值 > 中风险阈值，得分 = 80</li>
              <li>如果 指标值 > 低风险阈值，得分 = 60</li>
              <li>否则，得分 = 20</li>
            </ul>
          </div>
        </template>
      </el-alert>

      <el-tabs v-loading="loading" :key="refreshKey">
        <el-tab-pane v-for="(indicators, category) in groupedIndicators" :key="category" :label="category">
          <el-table :data="indicators" border style="width: 100%" :key="'table-' + category + '-' + refreshKey">
            <el-table-column prop="indicatorName" label="指标名称" width="180">
              <template #default="scope">
                <span>{{ scope.row.indicatorName }}</span>
                <el-tooltip placement="top">
                  <template #content>
                    <div style="max-width: 300px">
                      <p><strong>指标代码:</strong> {{ scope.row.indicatorCode }}</p>
                      <p><strong>单位:</strong> {{ scope.row.unit || '无' }}</p>
                      <p><strong>风险判定:</strong> {{ scope.row.comparisonOperator === 'LT' ? '数值越低，风险越高' : '数值越高，风险越高' }}</p>
                      <p><strong>计算说明:</strong> {{ scope.row.calculationMethod || '暂无说明' }}</p>
                    </div>
                  </template>
                  <el-icon class="help-icon"><QuestionFilled /></el-icon>
                </el-tooltip>
              </template>
            </el-table-column>
            <el-table-column prop="unit" label="单位" width="80" />
            <el-table-column prop="weight" label="权重 (0-1)" width="150">
              <template #default="scope">
                <el-input-number 
                  v-model="scope.row.weight" 
                  :key="`weight-${scope.row.id}-${refreshKey}`"
                  :precision="2" 
                  :step="0.01" 
                  :max="1" 
                  :min="0" 
                  size="small"
                  :disabled="!canAdjust"
                  @change="handleUpdateIndicator(scope.row)"
                />
              </template>
            </el-table-column>
            <el-table-column label="风险阈值配置 (高 / 中 / 低)" min-width="350">
              <template #default="scope">
                <div class="threshold-inputs">
                  <span v-if="scope.row.comparisonOperator === 'LT'" class="operator-text">&lt;</span>
                  <span v-else class="operator-text">&gt;</span>
                  
                  <el-input-number 
                    v-model="scope.row.thresholdHigh" 
                    :key="`high-${scope.row.id}-${refreshKey}`"
                    :precision="2" 
                    :step="0.1" 
                    size="small" 
                    placeholder="高"
                    :disabled="!canAdjust"
                    @change="handleUpdateIndicator(scope.row)" 
                  />
                  <span class="separator">/</span>
                  <el-input-number 
                    v-model="scope.row.thresholdMedium" 
                    :key="`med-${scope.row.id}-${refreshKey}`"
                    :precision="2" 
                    :step="0.1" 
                    size="small" 
                    placeholder="中"
                    :disabled="!canAdjust"
                    @change="handleUpdateIndicator(scope.row)" 
                  />
                  <span class="separator">/</span>
                  <el-input-number 
                    v-model="scope.row.thresholdLow" 
                    :key="`low-${scope.row.id}-${refreshKey}`"
                    :precision="2" 
                    :step="0.1" 
                    size="small" 
                    placeholder="低"
                    :disabled="!canAdjust"
                    @change="handleUpdateIndicator(scope.row)" 
                  />
                </div>
                <div class="threshold-hint">
                  {{ scope.row.comparisonOperator === 'LT' ? '低于此值触发风险' : '高于此值触发风险' }}
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { getGroupedIndicators, updateIndicator, calculateRisk, restoreDefaults } from '../api/risk'
import { ElMessage, ElMessageBox } from 'element-plus'
import { QuestionFilled } from '@element-plus/icons-vue'
import { canAdjustRiskParams } from '../utils/permission'

const loading = ref(false)
const calculating = ref(false)
const refreshKey = ref(0)
const groupedIndicators = ref<Record<string, any[]>>({})

// 当前用户角色
const userRole = computed(() => localStorage.getItem('userRole') || '')
// 是否可以调整风险参数
const canAdjust = computed(() => canAdjustRiskParams(userRole.value))

const loadIndicators = async () => {
  loading.value = true
  try {
    const res = await getGroupedIndicators()
    if (res.code === 200) {
      // 强制Vue响应式更新：创建新对象而不是直接赋值
      groupedIndicators.value = { ...res.data }
      console.log('指标已加载:', Object.keys(groupedIndicators.value))
    }
  } catch (error) {
    ElMessage.error('加载指标配置失败，请确保后端服务已更新并重启')
  } finally {
    loading.value = false
  }
}

const handleUpdateIndicator = async (row: any) => {
  try {
    await updateIndicator(row.indicatorId, row)
    ElMessage.success('配置已更新')
  } catch (error) {
    ElMessage.error('更新失败')
  }
}

const handleRecalculate = async () => {
  calculating.value = true
  try {
    await calculateRisk()
    ElMessage.success('风险重新计算已触发')
  } catch (error) {
    ElMessage.error('触发计算失败')
  } finally {
    calculating.value = false
  }
}

const handleRestoreDefaults = async () => {
  try {
    await ElMessageBox.confirm('确定要恢复所有指标的默认权重和阈值吗？此操作不可撤销。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    loading.value = true
    // 1. 调用恢复默认API
    await restoreDefaults()
    console.log('✓ 已调用恢复默认API')
    
    // 2. 等待一小段时间确保后端完成
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // 3. 重新加载数据 - 强制完全刷新
    const res = await getGroupedIndicators()
    if (res.code === 200) {
      // 使用JSON深拷贝确保Vue检测到变化
      groupedIndicators.value = JSON.parse(JSON.stringify(res.data))
      // 强制刷新整个组件
      refreshKey.value++
      console.log('✓ 指标已重新加载，刷新key:', refreshKey.value, '指标数:', Object.keys(groupedIndicators.value).length)
    }
    
    ElMessage.success('已恢复默认配置')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('恢复默认配置失败:', error)
      ElMessage.error('恢复默认配置失败')
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadIndicators()
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
.info-alert {
  margin-bottom: 24px;
}
.formula-explanation {
  line-height: 1.6;
}
.formula-explanation ul {
  margin-top: 5px;
  padding-left: 20px;
}
.category-section {
  margin-bottom: 30px;
}
.category-header {
  margin-bottom: 15px;
  border-left: 4px solid #409eff;
  padding-left: 12px;
}
.category-header h3 {
  margin: 0;
  color: #303133;
}
.threshold-inputs {
  display: flex;
  align-items: center;
  gap: 10px;
}
.separator {
  color: #909399;
  font-weight: bold;
}
.help-icon {
  margin-left: 5px;
  cursor: pointer;
  color: #909399;
}
.operator-text {
  font-weight: bold;
  margin-right: 5px;
}
.threshold-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
