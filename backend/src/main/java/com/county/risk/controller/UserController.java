package com.county.risk.controller;

import com.county.risk.entity.User;
import com.county.risk.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    // 登录接口
    @PostMapping("/login")
    public Map<String, Object> login(@RequestParam String username, @RequestParam String password) {
        Map<String, Object> response = new HashMap<>();
        User user = userMapper.findByUsername(username);
        if (user == null) {
            response.put("success", false);
            response.put("message", "用户不存在");
            return response;
        }

        // 使用SHA-256加密密码（与数据库中的SHA2('password', 256)匹配）
        String passwordHash = sha256(password);
        if (!user.getPasswordHash().equalsIgnoreCase(passwordHash)) {
            response.put("success", false);
            response.put("message", "密码错误");
            return response;
        }

        response.put("success", true);
        response.put("username", user.getUsername());
        response.put("role", user.getRole());
        return response;
    }

    // 管理员操作示例接口
    @PostMapping("/admin-action")
    public ResponseEntity<?> adminAction(@RequestHeader("role") String role) {
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("无权限访问");
        }
        // 管理员操作逻辑
        return ResponseEntity.ok("操作成功");
    }
    
    /**
     * SHA-256加密方法（与MySQL的SHA2函数匹配）
     */
    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("密码加密失败", e);
        }
    }
}
