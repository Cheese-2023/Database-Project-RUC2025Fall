# DeepSeek API 快速开始指南

## 🚀 5分钟快速集成

### 步骤1：获取 API Key
访问 https://platform.deepseek.com 注册并获取 API Key

### 步骤2：配置 API Key（选择一种方式）

**方式A：环境变量（推荐）**
```bash
export DEEPSEEK_API_KEY=sk-your-api-key-here
```

**方式B：本地配置文件**
```bash
cd backend/src/main/resources
cp application-local.yml.example application-local.yml
# 然后编辑 application-local.yml，填入您的 API Key
```

### 步骤3：重启后端
```bash
cd backend
mvn spring-boot:run
```

### 步骤4：使用
1. 登录系统
2. 点击顶部菜单 "AI助手"
3. 开始对话！

## ✅ 验证配置

访问：`http://localhost:8080/api/deepseek/status`

如果返回 `{"configured": true}` 说明配置成功！

## 📖 详细文档

查看 `DeepSeek_API_集成指南.md` 获取完整文档。

