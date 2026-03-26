package com.zjgsu.pjt.backend.controller;

import com.zjgsu.pjt.backend.common.Result;
import com.zjgsu.pjt.backend.entity.User;
import com.zjgsu.pjt.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public Result<String> register(@RequestBody User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return Result.error(400, "用户名已存在");
        }
        userRepository.save(user);
        return Result.success("注册成功");
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            Map<String, Object> data = new HashMap<>();
            data.put("token", "mock-jwt-token-" + userOpt.get().getId());
            data.put("isNewUser", false);
            return Result.success(data);
        }
        return Result.error(401, "用户名或密码错误");
    }

    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success("登出成功");
    }
}
