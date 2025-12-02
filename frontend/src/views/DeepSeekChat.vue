<template>
  <div class="deepseek-chat-container">
    <div class="page-header">
      <h1>AI 智能助手</h1>
      <p class="subtitle">基于 DeepSeek API 的智能对话助手</p>
    </div>

    <el-card class="chat-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span class="card-title">对话窗口</span>
          <el-tag :type="statusConfigured ? 'success' : 'danger'" size="small">
            {{ statusConfigured ? '已配置' : '未配置' }}
          </el-tag>
        </div>
      </template>

      <!-- 消息列表 -->
      <div class="messages-container" ref="messagesContainer">
        <div v-if="messages.length === 0" class="empty-state">
          <el-icon :size="48" style="color: #909399; margin-bottom: 16px;">
            <ChatLineRound />
          </el-icon>
          <p>开始对话吧！我可以帮您分析县域风险数据、解答相关问题。</p>
        </div>
        
        <div
          v-for="(msg, index) in messages"
          :key="index"
          :class="['message-item', msg.role === 'user' ? 'user-message' : 'assistant-message']"
        >
          <div class="message-avatar">
            <el-icon v-if="msg.role === 'user'" :size="20">
              <User />
            </el-icon>
            <el-icon v-else :size="20">
              <Service />
            </el-icon>
          </div>
          <div class="message-content">
            <div class="message-role">{{ msg.role === 'user' ? '您' : 'AI助手' }}</div>
            <div class="message-text" v-html="formatMessage(msg.content)"></div>
            <div class="message-time">{{ formatTime(msg.timestamp) }}</div>
          </div>
        </div>
        
        <div v-if="thinking" class="message-item assistant-message">
          <div class="message-avatar">
            <el-icon :size="20">
              <Service />
            </el-icon>
          </div>
          <div class="message-content">
            <div class="message-role">AI助手</div>
            <div class="message-text thinking">
              <el-icon class="is-loading"><Loading /></el-icon>
              正在思考中...
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="input-area">
        <el-input
          v-model="inputMessage"
          type="textarea"
          :rows="3"
          placeholder="输入您的问题..."
          @keydown.ctrl.enter="sendMessage"
          @keydown.meta.enter="sendMessage"
          :disabled="!statusConfigured || sending"
        />
        <div class="input-actions">
          <div class="tips">
            <span>按 Ctrl+Enter 或 Cmd+Enter 发送</span>
          </div>
          <el-button
            type="primary"
            @click="sendMessage"
            :loading="sending"
            :disabled="!statusConfigured || !inputMessage.trim()"
          >
            发送
          </el-button>
          <el-button @click="clearMessages" :disabled="messages.length === 0">
            清空对话
          </el-button>
        </div>
      </div>

      <!-- 配置提示 -->
      <el-alert
        v-if="!statusConfigured"
        type="warning"
        :closable="false"
        style="margin-top: 20px;"
      >
        <template #title>
          <div>
            <strong>API 未配置</strong>
            <p style="margin: 8px 0 0 0; font-size: 12px;">
              请在后端配置文件 <code>application.yml</code> 中设置 <code>deepseek.api.key</code>，
              或设置环境变量 <code>DEEPSEEK_API_KEY</code>。
              <br>
              获取 API Key: <a href="https://platform.deepseek.com" target="_blank">https://platform.deepseek.com</a>
            </p>
          </div>
        </template>
      </el-alert>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Service, ChatLineRound, Loading } from '@element-plus/icons-vue'
import { simpleChat, checkDeepSeekStatus } from '../api/deepseek'

const loading = ref(false)
const sending = ref(false)
const thinking = ref(false)
const statusConfigured = ref(false)
const inputMessage = ref('')
const messages = ref<Array<{
  role: string
  content: string
  timestamp: number
}>>([])
const messagesContainer = ref<HTMLElement | null>(null)

// 检查配置状态
const checkStatus = async () => {
  try {
    const res = await checkDeepSeekStatus()
    if (res.code === 200) {
      statusConfigured.value = res.data?.configured || false
      if (!statusConfigured.value) {
        ElMessage.warning('DeepSeek API 未配置，请先配置 API Key')
      }
    }
  } catch (error) {
    console.error('检查配置状态失败:', error)
  }
}

// 发送消息
const sendMessage = async () => {
  if (!inputMessage.value.trim() || sending.value || !statusConfigured.value) {
    return
  }

  const userMessage = inputMessage.value.trim()
  inputMessage.value = ''

  // 添加用户消息
  messages.value.push({
    role: 'user',
    content: userMessage,
    timestamp: Date.now()
  })

  scrollToBottom()

  // 发送到 API
  sending.value = true
  thinking.value = true

  try {
    const res = await simpleChat(userMessage, {
      systemPrompt: '你是一个专业的县域风险分析助手，擅长分析县域经济、社会、环境等各方面的风险数据。请用专业但易懂的语言回答问题。'
    })

    thinking.value = false

    if (res.code === 200 && res.data) {
      // 提取 AI 回复
      const choices = res.data.choices
      if (choices && choices.length > 0) {
        const assistantMessage = choices[0].message?.content || '抱歉，我无法理解您的问题。'
        messages.value.push({
          role: 'assistant',
          content: assistantMessage,
          timestamp: Date.now()
        })
      } else {
        ElMessage.error('未收到有效回复')
      }
    } else {
      ElMessage.error(res.message || '发送消息失败')
    }
  } catch (error: any) {
    thinking.value = false
    console.error('发送消息失败:', error)
    ElMessage.error(error.message || '发送消息失败，请检查网络连接和 API 配置')
    
    // 添加错误消息
    messages.value.push({
      role: 'assistant',
      content: '抱歉，发生了错误：' + (error.message || '未知错误'),
      timestamp: Date.now()
    })
  } finally {
    sending.value = false
    scrollToBottom()
  }
}

// 清空对话
const clearMessages = () => {
  messages.value = []
  ElMessage.success('对话已清空')
}

// 格式化消息内容（支持 Markdown）
const formatMessage = (content: string) => {
  if (!content) return ''
  
  // 简单的 Markdown 转 HTML
  let html = content
    .replace(/\n/g, '<br>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/`(.*?)`/g, '<code>$1</code>')
  
  return html
}

// 格式化时间
const formatTime = (timestamp: number) => {
  const date = new Date(timestamp)
  return date.toLocaleTimeString('zh-CN', { 
    hour: '2-digit', 
    minute: '2-digit' 
  })
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

onMounted(() => {
  checkStatus()
})
</script>

<style scoped lang="scss">
.deepseek-chat-container {
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

.chat-card {
  height: calc(100vh - 200px);
  display: flex;
  flex-direction: column;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #fafafa;
  border-radius: 8px;
  margin-bottom: 20px;
  min-height: 400px;
  max-height: 600px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
  text-align: center;
}

.message-item {
  display: flex;
  margin-bottom: 20px;
  animation: fadeIn 0.3s;

  &.user-message {
    flex-direction: row-reverse;

    .message-content {
      background: #409EFF;
      color: white;
      margin-right: 12px;
    }
  }

  &.assistant-message {
    .message-content {
      background: white;
      color: #303133;
      margin-left: 12px;
    }
  }
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.message-content {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.message-role {
  font-size: 12px;
  opacity: 0.7;
  margin-bottom: 6px;
  font-weight: 500;
}

.message-text {
  line-height: 1.6;
  word-wrap: break-word;

  :deep(code) {
    background: rgba(0, 0, 0, 0.1);
    padding: 2px 6px;
    border-radius: 4px;
    font-family: 'Courier New', monospace;
    font-size: 0.9em;
  }

  &.thinking {
    display: flex;
    align-items: center;
    gap: 8px;
    color: #909399;
  }
}

.message-time {
  font-size: 11px;
  opacity: 0.6;
  margin-top: 6px;
}

.input-area {
  .input-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 12px;

    .tips {
      font-size: 12px;
      color: #909399;
    }
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>

