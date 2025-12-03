<template>
  <div style="padding: 20px; min-height: 100vh; background: #f5f7fa;">
    <el-card>
        <template #header>
          <div class="card-header">
            <span>SQL操作</span>
            <el-button type="primary" size="small" @click="loadTableList">刷新表列表</el-button>
          </div>
        </template>

        <el-row :gutter="20">
          <!-- 左侧：表列表和SQL编辑器 -->
          <el-col :span="12">
            <!-- 表列表 -->
            <el-card shadow="never" class="table-list-card" style="margin-bottom: 20px;">
              <template #header>
                <div style="display: flex; justify-content: space-between; align-items: center;">
                  <span>数据库表</span>
                  <el-tag type="info" size="small">{{ tableList.length }} 个表</el-tag>
                </div>
              </template>
              <div style="height: 300px; overflow-y: auto; padding: 10px; background: white;">
                <template v-if="tableList.length === 0">
                  <div style="text-align: center; padding: 40px; color: #909399;">
                    <p>暂无数据，请点击"刷新表列表"</p>
                  </div>
                </template>
                <template v-else>
                  <div 
                    v-for="(table, idx) in tableList" 
                    :key="`table-${idx}`"
                    @click="handleTableItemClick(table)"
                    style="padding: 12px; margin-bottom: 8px; background: #f5f7fa; border-radius: 4px; cursor: pointer; border: 1px solid #e4e7ed; display: block;"
                    @mouseenter="handleMouseEnter($event)"
                    @mouseleave="handleMouseLeave($event)"
                  >
                    <span style="font-size: 14px; color: #303133;">📄 {{ table }}</span>
                  </div>
                </template>
              </div>
            </el-card>

            <!-- SQL编辑器 -->
            <el-card shadow="never" class="sql-editor-card">
              <template #header>
                <div style="display: flex; justify-content: space-between; align-items: center;">
                  <span style="font-weight: 500;">SQL编辑器</span>
                  <div style="display: flex; gap: 8px;">
                    <el-button type="success" size="small" @click="executeQuery" :loading="queryLoading">
                      执行查询
                    </el-button>
                    <el-button type="warning" size="small" @click="executeUpdate" :loading="updateLoading">
                      执行更新
                    </el-button>
                    <el-button type="info" size="small" @click="clearSql">清空</el-button>
                  </div>
                </div>
              </template>
              <div style="padding: 15px;">
                <el-input
                  v-model="sqlText"
                  type="textarea"
                  :rows="12"
                  placeholder="请输入SQL语句，例如：&#10;SELECT * FROM county_basic LIMIT 10;&#10;&#10;或者点击左侧表名自动生成查询语句"
                  style="width: 100%;"
                  :style="{ fontFamily: 'Courier New, monospace' }"
                />
              </div>
            </el-card>
          </el-col>

          <!-- 右侧：结果展示 -->
          <el-col :span="12">
            <el-card shadow="never" class="result-card">
              <template #header>
                <div class="card-header">
                  <span>执行结果</span>
                  <el-tag v-if="resultCount !== null" type="info">
                    共 {{ resultCount }} 条记录
                  </el-tag>
                </div>
              </template>
              
              <div v-if="errorMessage" class="error-message">
                <el-alert :title="errorMessage" type="error" :closable="false" />
              </div>

              <el-scrollbar v-if="queryResults && queryResults.length > 0" height="500px">
                <el-table
                  :data="queryResults"
                  border
                  stripe
                  style="width: 100%"
                  max-height="500"
                >
                  <el-table-column
                    v-for="(value, key) in queryResults[0]"
                    :key="key"
                    :prop="key"
                    :label="key"
                    min-width="120"
                    show-overflow-tooltip
                  />
                </el-table>
              </el-scrollbar>

              <div v-else-if="updateResult !== null" class="update-result">
                <el-alert
                  :title="`执行成功，影响 ${updateResult} 行`"
                  type="success"
                  :closable="false"
                />
              </div>

              <el-empty v-else description="暂无执行结果" />
            </el-card>
          </el-col>
        </el-row>
      </el-card>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, Folder } from '@element-plus/icons-vue'
import { executeQuery, executeUpdate, getTableList, getTableStructure } from '../api/sql'

const sqlText = ref('')
const queryResults = ref<any[]>([])
const updateResult = ref<number | null>(null)
const errorMessage = ref('')
const resultCount = ref<number | null>(null)
const queryLoading = ref(false)
const updateLoading = ref(false)
const tableTreeData = ref<any[]>([])
const tableList = ref<string[]>([])

const treeProps = {
  label: 'label',
  children: 'children'
}

// 加载表列表
const loadTableList = async () => {
  try {
    const response = await getTableList()
    console.log('表列表响应:', response)
    if (response.code === 200 && response.data && Array.isArray(response.data)) {
      // 保存原始表名列表
      tableList.value = [...response.data]
      
      // 创建树形数据结构 - 每个表作为一个节点
      const newTableData = response.data.map((table: string) => ({
        label: table
      }))
      
      // 直接赋值
      tableTreeData.value = newTableData
      
      console.log('表列表数据已更新:', tableTreeData.value.length, '个表')
      console.log('tableList.value:', tableList.value)
      console.log('tableList.value.length:', tableList.value.length)
      console.log('前3个表:', tableList.value.slice(0, 3))
      
      // 强制触发响应式更新
      await nextTick()
      console.log('DOM更新后，tableList.value.length:', tableList.value.length)
      
      ElMessage.success(`已加载 ${tableList.value.length} 个表`)
    } else {
      console.warn('响应数据格式异常:', response)
      ElMessage.warning('未获取到表列表数据')
    }
  } catch (error: any) {
    console.error('加载表列表失败:', error)
    ElMessage.error('加载表列表失败: ' + (error.message || '未知错误'))
  }
}

// 鼠标悬停效果
const handleMouseEnter = (event: Event) => {
  const target = event.currentTarget as HTMLElement
  if (target) {
    target.style.background = '#ecf5ff'
    target.style.borderColor = '#b3d8ff'
  }
}

const handleMouseLeave = (event: Event) => {
  const target = event.currentTarget as HTMLElement
  if (target) {
    target.style.background = '#f5f7fa'
    target.style.borderColor = '#e4e7ed'
  }
}

// 点击表项（简单列表方式）
const handleTableItemClick = async (tableName: string) => {
  console.log('点击表:', tableName)
  // 直接生成SELECT语句
  sqlText.value = `SELECT * FROM ${tableName} LIMIT 100`
  ElMessage.info(`已选择表: ${tableName}`)
}

// 点击表节点（树形结构方式）
const handleTableClick = async (data: any) => {
  console.log('点击表节点:', data)
  const tableName = data.label || data
  // 如果children不存在或为空数组，则加载表结构
  if (!data.children || (Array.isArray(data.children) && data.children.length === 0)) {
    // 加载表结构
    try {
      const response = await getTableStructure(tableName)
      console.log('表结构响应:', response)
      if (response.code === 200 && response.data) {
        // 创建列数据
        const columns = response.data.map((col: any) => ({
          label: `${col.columnName} (${col.dataType})`
        }))
        
        // 找到对应的节点并更新 - 使用深拷贝确保响应式
        const nodeIndex = tableTreeData.value.findIndex((item: any) => item.label === tableName)
        if (nodeIndex !== -1) {
          // 创建全新的数组确保Vue检测到变化
          const newData = tableTreeData.value.map((item: any, index: number) => {
            if (index === nodeIndex) {
              return {
                ...item,
                children: columns
              }
            }
            return { ...item }
          })
          tableTreeData.value = newData
          console.log('已更新表结构，节点索引:', nodeIndex)
        }
        
        // 生成SELECT语句
        sqlText.value = `SELECT * FROM ${tableName} LIMIT 100`
        ElMessage.success(`已加载表 ${tableName} 的结构，共 ${columns.length} 个字段`)
      }
    } catch (error: any) {
      console.error('加载表结构失败:', error)
      ElMessage.error('加载表结构失败: ' + (error.message || '未知错误'))
    }
  } else {
    // 如果已有children，直接生成SELECT语句
    sqlText.value = `SELECT * FROM ${tableName} LIMIT 100`
  }
}

// 执行查询
const executeQuery = async () => {
  if (!sqlText.value.trim()) {
    ElMessage.warning('请输入SQL语句')
    return
  }

  queryLoading.value = true
  errorMessage.value = ''
  queryResults.value = []
  updateResult.value = null
  resultCount.value = null

  try {
    const response = await executeQuery(sqlText.value)
    console.log('查询响应:', response)
    if (response.code === 200 && response.data) {
      queryResults.value = response.data.data || []
      resultCount.value = response.data.count || 0
      ElMessage.success(`查询成功，共 ${resultCount.value} 条记录`)
    } else {
      errorMessage.value = response.message || '查询失败'
      ElMessage.error(errorMessage.value)
    }
  } catch (error: any) {
    console.error('查询失败:', error)
    errorMessage.value = error.message || '执行失败'
    ElMessage.error(errorMessage.value)
  } finally {
    queryLoading.value = false
  }
}

// 执行更新
const executeUpdate = async () => {
  if (!sqlText.value.trim()) {
    ElMessage.warning('请输入SQL语句')
    return
  }

  updateLoading.value = true
  errorMessage.value = ''
  queryResults.value = []
  updateResult.value = null
  resultCount.value = null

  try {
    const response = await executeUpdate(sqlText.value)
    console.log('更新响应:', response)
    if (response.code === 200 && response.data) {
      updateResult.value = response.data.affectedRows || 0
      ElMessage.success(`执行成功，影响 ${updateResult.value} 行`)
    } else {
      errorMessage.value = response.message || '执行失败'
      ElMessage.error(errorMessage.value)
    }
  } catch (error: any) {
    console.error('更新失败:', error)
    errorMessage.value = error.message || '执行失败'
    ElMessage.error(errorMessage.value)
  } finally {
    updateLoading.value = false
  }
}

// 清空SQL
const clearSql = () => {
  sqlText.value = ''
  queryResults.value = []
  updateResult.value = null
  errorMessage.value = ''
  resultCount.value = null
}

onMounted(() => {
  console.log('SqlExecute 组件已挂载')
  console.log('初始 tableList:', tableList.value)
  console.log('初始 tableTreeData:', tableTreeData.value)
  loadTableList()
})
</script>

<style scoped>
.sql-execute-container {
  padding: 20px;
  min-height: calc(100vh - 100px);
  width: 100%;
  box-sizing: border-box;
}

.sql-card {
  min-height: 600px;
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-list-card {
  min-height: 300px;
}

.sql-editor-card {
  min-height: 400px;
}

.result-card {
  min-height: 600px;
}

.sql-textarea {
  font-family: 'Courier New', monospace;
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 5px;
  width: 100%;
}

.tree-node:hover {
  background-color: #f5f7fa;
  border-radius: 4px;
}

.table-item {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  margin-bottom: 6px;
  cursor: pointer;
  border-radius: 4px;
  transition: background-color 0.2s;
  border: 1px solid transparent;
  min-height: 36px;
}

.table-item:hover {
  background-color: #ecf5ff;
  border-color: #b3d8ff;
}

.table-item:active {
  background-color: #d9ecff;
}

:deep(.el-tree-node__content) {
  height: 32px;
  line-height: 32px;
}

:deep(.el-tree-node__label) {
  font-size: 14px;
}

.error-message {
  margin-bottom: 10px;
}

.update-result {
  margin-top: 20px;
}
</style>

