import request from '../utils/request';

/**
 * DeepSeek API 相关接口
 */

// 发送聊天消息（完整版）
export const chatWithDeepSeek = (messages: Array<{role: string, content: string}>, options?: {
  model?: string;
  temperature?: number;
  max_tokens?: number;
}) => {
  return request.post('/deepseek/chat', {
    messages,
    ...options
  });
};

// 发送聊天消息（简化版）
export const simpleChat = (message: string, options?: {
  systemPrompt?: string;
  model?: string;
  temperature?: number;
  max_tokens?: number;
}) => {
  return request.post('/deepseek/chat/simple', {
    message,
    ...options
  });
};

// 检查配置状态
export const checkDeepSeekStatus = () => {
  return request.get('/deepseek/status');
};

