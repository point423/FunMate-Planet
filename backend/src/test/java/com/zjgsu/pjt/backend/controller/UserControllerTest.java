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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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

    // ✅ 修复：将过时的 @MockBean 替换为 Spring Boot 3.4 推荐的 @MockitoBean
    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private final String DUMMY_TOKEN = "Bearer dummy-token";

    @Test
    @DisplayName("安全校验-越权修改他人资料应返回403")
    void updateUser_Forbidden_Returns403() throws Exception {
        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(1L);

        mockMvc.perform(put("/api/users/99")
                        .header("Authorization", DUMMY_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"hacker\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("修改自己资料-成功场景")
    void updateUser_Own_Success() throws Exception {
        User user = new User();
        user.setId(1L);
        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(1L);
        when(userService.updateUser(eq(1L), any())).thenReturn(user);

        mockMvc.perform(put("/api/users/1")
                        .header("Authorization", DUMMY_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }
}
