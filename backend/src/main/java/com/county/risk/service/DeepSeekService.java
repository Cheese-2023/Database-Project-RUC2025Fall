package com.county.risk.service;

import java.util.Map;

/**
 * DeepSeek API 服务接口
 */
public interface DeepSeekService {
    
    /**
     * 发送聊天消息到 DeepSeek API
     * 
     * @param messages 消息列表（包含角色和内容）
     * @param model 模型名称（可选，默认使用配置的模型）
     * @return API 响应结果
     */
    Map<String, Object> chat(Map<String, Object> request);
    
    /**
     * 检查 API 配置是否有效
     * 
     * @return 是否配置有效
     */
    boolean isConfigured();
}

