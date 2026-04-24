package com.zjgsu.pjt.backend.service;

import com.zjgsu.pjt.backend.entity.User;
import com.zjgsu.pjt.backend.repository.UserRepository;
import com.zjgsu.pjt.backend.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("测试用户注册-成功")
    void register_Success() {
        User user = new User();
        user.setUsername("newuser");
        user.setPassword("123456"); // ✅ 必须设置密码，否则 BCrypt 会报错

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenReturn(user);

        boolean success = authService.register(user);
        assertTrue(success);
    }

    @Test
    @DisplayName("测试用户注册-用户名已存在")
    void register_Fail_UserExists() {
        User user = new User();
        user.setUsername("existuser");
        user.setPassword("123456"); // ✅ 必须设置密码

        when(userRepository.findByUsername("existuser")).thenReturn(Optional.of(new User()));

        boolean success = authService.register(user);
        assertFalse(success);
    }

    @Test
    @DisplayName("测试登录-成功并获取Token")
    void login_Success() {
        User user = new User();
        user.setId(1L);
        user.setUsername("user");
        // 这里的密码必须是加密后的，否则 authService.login 里的 matches 会失败
        // 简单起见，我们在 AuthService 中 Mock 掉 matches 逻辑是不可能的，因为它是 private final。
        // 但 AuthService 目前直接 new 了 BCrypt，所以我们需要给它一个真实的加密串。
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        user.setPassword(encoder.encode("123"));

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(1L)).thenReturn("mock-token");

        Map<String, Object> result = authService.login("user", "123");
        assertNotNull(result);
        assertEquals("mock-token", result.get("token"));
    }
}
