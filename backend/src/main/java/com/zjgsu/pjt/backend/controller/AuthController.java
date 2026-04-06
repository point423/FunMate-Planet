package com.zjgsu.pjt.backend.controller;

import com.zjgsu.pjt.backend.common.Result;
import com.zjgsu.pjt.backend.entity.User;
import com.zjgsu.pjt.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody User user) {
        boolean success = authService.register(user);
        return success ? Result.success("注册成功") : Result.error(400, "用户名已存在");
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        if (credentials.get("username") == null || credentials.get("password") == null) {
            return Result.error(400, "用户名和密码不能为空");
        }
        Map<String, Object> data = authService.login(credentials.get("username"), credentials.get("password"));
        return data != null ? Result.success(data) : Result.error(401, "用户名或密码错误");
    }

    @PostMapping("/logout")
    public Result<String> logout() {
        authService.logout();
        return Result.success("登出成功");
    }

    @GetMapping("/me")
    public Result<Map<String, Object>> getCurrentUser(@RequestAttribute(value = "currentUserId", required = false) Long userId) {
        if (userId == null) {
            return Result.error(401, "未授权");
        }
        Map<String, Object> userInfo = authService.getCurrentUser(userId);
        return userInfo != null ? Result.success(userInfo) : Result.error(404, "用户不存在");
    }
}
