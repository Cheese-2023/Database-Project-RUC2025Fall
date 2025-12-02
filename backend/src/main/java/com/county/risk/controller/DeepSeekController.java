package com.county.risk.controller;

import com.county.risk.common.Result;
import com.county.risk.service.DeepSeekService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek API 控制器
 */
@Tag(name = "DeepSeek AI助手", description = "集成 DeepSeek API 提供AI对话功能")
@RestController
@RequestMapping("/deepseek")
@RequiredArgsConstructor
public class DeepSeekController {
    
    private final DeepSeekService deepSeekService;
    
    /**
     * 发送聊天消息
     */
    @Operation(summary = "发送聊天消息", description = "向 DeepSeek API 发送消息并获取回复")
    @PostMapping("/chat")
    public Result<Map<String, Object>> chat(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = deepSeekService.chat(request);
        
        // 检查是否有错误
        if (response.containsKey("error")) {
            return Result.error(500, (String) response.get("error"));
        }
        
        return Result.success(response);
    }
    
    /**
     * 简化版聊天接口（只需要用户消息）
     */
    @Operation(summary = "简化聊天接口", description = "发送用户消息，自动构建对话上下文")
    @PostMapping("/chat/simple")
    public Result<Map<String, Object>> simpleChat(@RequestBody Map<String, Object> request) {
        String userMessage = (String) request.get("message");
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return Result.error(400, "消息内容不能为空");
        }
        
        // 构建消息列表
        List<Map<String, String>> messages = new java.util.ArrayList<>();
        
        // 系统提示词（可选）
        String systemPrompt = (String) request.getOrDefault("systemPrompt", 
            "你是一个专业的县域风险分析助手，擅长分析县域经济、社会、环境等各方面的风险数据。请用专业但易懂的语言回答问题。");
        
        if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt);
            messages.add(systemMsg);
        }
        
        // 用户消息
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        messages.add(userMsg);
        
        // 构建完整请求
        Map<String, Object> fullRequest = new HashMap<>();
        fullRequest.put("messages", messages);
        if (request.containsKey("model")) {
            fullRequest.put("model", request.get("model"));
        }
        if (request.containsKey("temperature")) {
            fullRequest.put("temperature", request.get("temperature"));
        }
        if (request.containsKey("max_tokens")) {
            fullRequest.put("max_tokens", request.get("max_tokens"));
        }
        
        Map<String, Object> response = deepSeekService.chat(fullRequest);
        
        if (response.containsKey("error")) {
            return Result.error(500, (String) response.get("error"));
        }
        
        return Result.success(response);
    }
    
    /**
     * 检查配置状态
     */
    @Operation(summary = "检查配置状态", description = "检查 DeepSeek API 是否已正确配置")
    @GetMapping("/status")
    public Result<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("configured", deepSeekService.isConfigured());
        if (!deepSeekService.isConfigured()) {
            status.put("message", "请在 application.yml 中配置 deepseek.api.key");
        }
        return Result.success(status);
    }
}

