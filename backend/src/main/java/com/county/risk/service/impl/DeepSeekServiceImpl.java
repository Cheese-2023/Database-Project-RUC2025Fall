package com.county.risk.service.impl;

import com.county.risk.service.DeepSeekService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * DeepSeek API 服务实现类
 */
@Slf4j
@Service
public class DeepSeekServiceImpl implements DeepSeekService {
    
    @Value("${deepseek.api.key:}")
    private String apiKey;
    
    @Value("${deepseek.api.url:https://api.deepseek.com/v1/chat/completions}")
    private String apiUrl;
    
    @Value("${deepseek.api.model:deepseek-chat}")
    private String defaultModel;
    
    @Value("${deepseek.api.max-tokens:2000}")
    private Integer maxTokens;
    
    private final RestTemplate restTemplate;
    
    public DeepSeekServiceImpl() {
        this.restTemplate = new RestTemplate();
    }
    
    @Override
    public boolean isConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }
    
    @Override
    public Map<String, Object> chat(Map<String, Object> request) {
        if (!isConfigured()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "DeepSeek API 未配置，请在 application.yml 中设置 deepseek.api.key");
            error.put("code", "NOT_CONFIGURED");
            return error;
        }
        
        try {
            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            
            // 模型名称
            String model = (String) request.getOrDefault("model", defaultModel);
            requestBody.put("model", model);
            
            // 消息列表
            @SuppressWarnings("unchecked")
            List<Map<String, String>> messages = (List<Map<String, String>>) request.get("messages");
            if (messages == null || messages.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "消息列表不能为空");
                error.put("code", "INVALID_REQUEST");
                return error;
            }
            requestBody.put("messages", messages);
            
            // 可选参数
            if (request.containsKey("temperature")) {
                requestBody.put("temperature", request.get("temperature"));
            } else {
                requestBody.put("temperature", 0.7);
            }
            
            if (request.containsKey("max_tokens")) {
                requestBody.put("max_tokens", request.get("max_tokens"));
            } else {
                requestBody.put("max_tokens", maxTokens);
            }
            
            if (request.containsKey("stream")) {
                requestBody.put("stream", request.get("stream"));
            }
            
            // 发送请求
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "API 调用失败");
                error.put("code", "API_ERROR");
                error.put("status", response.getStatusCode().value());
                return error;
            }
            
        } catch (Exception e) {
            log.error("调用 DeepSeek API 失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "调用 DeepSeek API 时发生错误: " + e.getMessage());
            error.put("code", "EXCEPTION");
            return error;
        }
    }
}

