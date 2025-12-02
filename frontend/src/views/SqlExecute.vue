<template>
  <Layout>
    <div class="sql-execute-container">
      <el-card class="sql-card">
        <template #header>
          <div class="card-header">
            <span>SQL操作</span>
            <el-button type="primary" size="small" @click="loadTableList">刷新表列表</el-button>
          </div>
        </template>

        <el-row :gutter="20">
          <!-- 左侧：表列表和SQL编辑器 -->
          <el-col :span="12">
            <el-card shadow="never" class="table-list-card">
              <template #header>
                <span>数据库表</span>
              </template>
              <el-scrollbar height="300px">
                <el-tree
                  :data="tableTreeData"
                  :props="{ label: 'label', children: 'children' }"
                  @node-click="handleTableClick"
                  highlight-current
                >
                  <template #default="{ node }">
                    <span class="tree-node">
                      <el-icon v-if="!node.children"><Document /></el-icon>
                      <el-icon v-else><Folder /></el-icon>
                      {{ node.label }}
                    </span>
                  </template>
                </el-tree>
              </el-scrollbar>
            </el-card>

            <el-card shadow="never" class="sql-editor-card" style="margin-top: 20px;">
              <template #header>
                <div class="card-header">
                  <span>SQL编辑器</span>
                  <div>
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
              <el-input
                v-model="sqlText"
                type="textarea"
                :rows="10"
                placeholder="请输入SQL语句..."
                class="sql-textarea"
              />
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
  </Layout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, Folder } from '@element-plus/icons-vue'
import Layout from '../components/Layout.vue'
import { executeQuery, executeUpdate, getTableList, getTableStructure } from '../api/sql'

const sqlText = ref('')
const queryResults = ref<any[]>([])
const updateResult = ref<number | null>(null)
const errorMessage = ref('')
const resultCount = ref<number | null>(null)
const queryLoading = ref(false)
const updateLoading = ref(false)
const tableTreeData = ref<any[]>([])

// 加载表列表
const loadTableList = async () => {
  try {
    const response = await getTableList()
    if (response.code === 200 && response.data) {
      tableTreeData.value = response.data.map((table: string) => ({
        label: table,
        children: []
      }))
    }
  } catch (error: any) {
    ElMessage.error('加载表列表失败: ' + (error.message || '未知错误'))
  }
}

// 点击表节点
const handleTableClick = async (data: any) => {
  if (!data.children || data.children.length === 0) {
    // 加载表结构
    try {
      const response = await getTableStructure(data.label)
      if (response.code === 200 && response.data) {
        data.children = response.data.map((col: any) => ({
          label: `${col.columnName} (${col.dataType})`
        }))
        
        // 生成SELECT语句
        sqlText.value = `SELECT * FROM ${data.label} LIMIT 100`
      }
    } catch (error: any) {
      ElMessage.error('加载表结构失败: ' + (error.message || '未知错误'))
    }
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
    if (response.code === 200) {
      queryResults.value = response.data || []
      resultCount.value = response.count || 0
      ElMessage.success('查询成功')
    } else {
      errorMessage.value = response.message || '查询失败'
      ElMessage.error(errorMessage.value)
    }
  } catch (error: any) {
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
    if (response.code === 200) {
      updateResult.value = response.affectedRows || 0
      ElMessage.success(`执行成功，影响 ${updateResult.value} 行`)
    } else {
      errorMessage.value = response.message || '执行失败'
      ElMessage.error(errorMessage.value)
    }
  } catch (error: any) {
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
  loadTableList()
})
</script>

<style scoped>
.sql-execute-container {
  padding: 20px;
}

.sql-card {
  min-height: 600px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-list-card,
.sql-editor-card,
.result-card {
  height: 100%;
}

.sql-textarea {
  font-family: 'Courier New', monospace;
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 5px;
}

.error-message {
  margin-bottom: 10px;
}

.update-result {
  margin-top: 20px;
}
</style>

