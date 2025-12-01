package com.county.risk.controller;

import com.county.risk.entity.User;
import com.county.risk.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

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

        if (!user.getPasswordHash().equals(DigestUtils.md5DigestAsHex(password.getBytes()))) {
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
}
