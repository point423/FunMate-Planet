package com.zjgsu.pjt.backend.controller;

import com.zjgsu.pjt.backend.entity.User;
import com.zjgsu.pjt.backend.service.UserService;
import com.zjgsu.pjt.backend.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil; // ✅ Mock JwtUtil 绕过拦截器

    @Autowired
    private ObjectMapper objectMapper;

    private final String DUMMY_TOKEN = "Bearer dummy-token";

    @Test
    @DisplayName("1. 获取当前用户-未登录报错401")
    void getMe_NotLoggedIn_Returns401() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("2. 获取当前用户-已登录返回资料")
    void getMe_LoggedIn_ReturnsUser() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setNickname("测试员");

        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(1L);
        when(userService.findById(1L)).thenReturn(user);

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", DUMMY_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0)) // ✅ 改为 0，匹配作业要求
                .andExpect(jsonPath("$.data.nickname").value("测试员"));
    }

    @Test
    @DisplayName("3. 编辑个人资料-成功场景")
    void updateMe_Success() throws Exception {
        User profile = new User();
        profile.setNickname("新昵称");

        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(1L);
        when(userService.updateProfile(eq(1L), any(User.class))).thenReturn(profile);

        mockMvc.perform(put("/api/users/me")
                        .header("Authorization", DUMMY_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profile)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("success")); // ✅ 确保字段名是 msg
    }

    @Test
    @DisplayName("4. 获取指定ID用户-不存在返回404")
    void getUserById_NotFound_Returns404() throws Exception {
        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(1L);
        when(userService.findById(99L)).thenReturn(null);

        mockMvc.perform(get("/api/users/99")
                        .header("Authorization", DUMMY_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("5. 上报位置-解析参数异常")
    void updateLocation_InvalidParams_Returns400() throws Exception {
        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(1L);

        mockMvc.perform(post("/api/users/location")
                        .header("Authorization", DUMMY_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"wrong_key\": 120}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("6. 获取当前用户-用户不存在报错404")
    void getMe_UserNotFound_Returns404() throws Exception {
        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(1L);
        when(userService.findById(1L)).thenReturn(null);

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", DUMMY_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }
}
