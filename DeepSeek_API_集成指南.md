# DeepSeek API 集成指南

## 📋 概述

本系统已集成 DeepSeek API，提供 AI 智能对话功能，可以帮助用户分析县域风险数据、解答相关问题。

## 🔑 第一步：获取 API Key

1. 访问 [DeepSeek 开放平台](https://platform.deepseek.com)
2. 注册/登录账号
3. 在控制台创建应用，获取 API Key
4. 记录您的 API Key（格式类似：`sk-xxxxxxxxxxxxx`）

## ⚙️ 第二步：配置 API Key

### 方法一：使用环境变量（最推荐，最安全）

设置环境变量 `DEEPSEEK_API_KEY`：

**Linux/Mac:**
```bash
export DEEPSEEK_API_KEY=sk-your-actual-api-key-here
```

**Windows (PowerShell):**
```powershell
$env:DEEPSEEK_API_KEY="sk-your-actual-api-key-here"
```

**Windows (CMD):**
```cmd
set DEEPSEEK_API_KEY=sk-your-actual-api-key-here
```

或者在启动后端时直接设置：
```bash
DEEPSEEK_API_KEY=sk-your-actual-api-key-here mvn spring-boot:run
```

### 方法二：使用本地配置文件（推荐用于开发环境）

1. 复制示例配置文件：
   ```bash
   cd backend/src/main/resources
   cp application-local.yml.example application-local.yml
   ```

2. 编辑 `application-local.yml`，填入您的 API Key：
   ```yaml
   deepseek:
     api:
       key: sk-your-actual-api-key-here
   ```

3. 启动时激活 local 配置：
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=local
   ```

   ⚠️ **注意**：`application-local.yml` 已在 `.gitignore` 中，不会被提交到 Git。

### 方法三：在 IDE 中设置环境变量

**IntelliJ IDEA:**
1. Run → Edit Configurations
2. 在 Environment variables 中添加：`DEEPSEEK_API_KEY=sk-your-actual-api-key-here`

**Eclipse:**
1. Run → Run Configurations
2. 在 Environment 标签页添加环境变量

## 🚀 第三步：启动服务

1. **重启后端服务**（如果已在运行）：
   ```bash
   cd backend
   mvn spring-boot:run
   ```

2. **启动前端服务**（如果未启动）：
   ```bash
   cd frontend
   npm run dev
   ```

## 💻 第四步：使用 AI 助手

1. 登录系统
2. 在顶部菜单点击 **"AI助手"**
3. 在对话框中输入您的问题
4. 按 **Ctrl+Enter**（Windows/Linux）或 **Cmd+Enter**（Mac）发送消息

## 📝 API 接口说明

### 1. 简化聊天接口（推荐）

**接口地址：** `POST /api/deepseek/chat/simple`

**请求体：**
```json
{
  "message": "请分析一下云南省的贫困县风险情况",
  "systemPrompt": "你是一个专业的县域风险分析助手..." // 可选
}
```

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "choices": [
      {
        "message": {
          "role": "assistant",
          "content": "根据数据分析，云南省共有88个贫困县..."
        }
      }
    ]
  }
}
```

### 2. 完整聊天接口

**接口地址：** `POST /api/deepseek/chat`

**请求体：**
```json
{
  "model": "deepseek-chat",
  "messages": [
    {
      "role": "system",
      "content": "你是一个专业的县域风险分析助手"
    },
    {
      "role": "user",
      "content": "请分析一下云南省的贫困县风险情况"
    }
  ],
  "temperature": 0.7,
  "max_tokens": 2000
}
```

### 3. 检查配置状态

**接口地址：** `GET /api/deepseek/status`

**响应：**
```json
{
  "code": 200,
  "data": {
    "configured": true,
    "message": ""
  }
}
```

## 🎯 使用场景示例

### 场景1：分析特定省份的贫困县情况
```
用户：请分析一下云南省的贫困县摘帽后的经济变化
AI：根据数据分析，云南省共有88个贫困县...
```

### 场景2：解释风险指标
```
用户：什么是综合风险得分？它是如何计算的？
AI：综合风险得分是一个综合评估指标...
```

### 场景3：数据查询建议
```
用户：我想查看高风险县域，应该在哪里查找？
AI：您可以在"风险分析"页面中...
```

## ⚠️ 注意事项

1. **API Key 安全**：
   - ⚠️ **不要**将 API Key 提交到 Git 仓库
   - ⚠️ **不要**在前端代码中硬编码 API Key
   - ✅ 使用环境变量或配置文件（并添加到 `.gitignore`）

2. **费用控制**：
   - DeepSeek API 按使用量计费
   - 建议设置合理的 `max_tokens` 限制
   - 监控 API 调用次数和费用

3. **错误处理**：
   - 如果 API Key 未配置，系统会显示警告提示
   - 如果 API 调用失败，会显示错误信息
   - 检查后端日志获取详细错误信息

4. **性能优化**：
   - 对话历史会保存在前端（刷新页面会清空）
   - 如需持久化，可以保存到数据库
   - 建议添加请求频率限制

## 🔧 高级配置

### 修改默认模型

在 `application.yml` 中修改：
```yaml
deepseek:
  api:
    model: deepseek-chat  # 或其他可用模型
```

### 调整生成参数

在调用 API 时可以传递参数：
```javascript
simpleChat("你的问题", {
  temperature: 0.8,  // 创造性（0-1，越高越有创造性）
  max_tokens: 3000   // 最大生成token数
})
```

### 自定义系统提示词

在调用时指定：
```javascript
simpleChat("你的问题", {
  systemPrompt: "你是一个专门分析县域经济数据的专家..."
})
```

## 📚 相关文档

- [DeepSeek 官方文档](https://platform.deepseek.com/docs)
- [DeepSeek API 参考](https://api-docs.deepseek.com)

## 🐛 常见问题

### Q1: 提示 "API 未配置"
**A:** 检查 `application.yml` 中的 `deepseek.api.key` 是否正确设置，或环境变量 `DEEPSEEK_API_KEY` 是否已设置。

### Q2: 提示 "API 调用失败"
**A:** 
- 检查 API Key 是否有效
- 检查网络连接
- 查看后端日志获取详细错误信息

### Q3: 响应速度慢
**A:** 
- 这是正常的，AI 生成需要时间
- 可以调整 `max_tokens` 减少生成内容长度
- 检查网络延迟

### Q4: 如何保存对话历史？
**A:** 当前版本对话历史仅保存在前端内存中，刷新页面会清空。如需持久化，可以：
- 保存到 localStorage
- 保存到数据库
- 导出为文件

## 📞 技术支持

如有问题，请查看：
1. 后端日志：`backend/logs/application.log`
2. 浏览器控制台：F12 打开开发者工具
3. 网络请求：在 Network 标签查看 API 调用详情

