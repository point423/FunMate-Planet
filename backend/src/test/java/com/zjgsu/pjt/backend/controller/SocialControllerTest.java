package com.zjgsu.pjt.backend.controller;

import com.zjgsu.pjt.backend.service.FriendshipService;
import com.zjgsu.pjt.backend.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SocialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FriendshipService friendshipService;

    @MockBean
    private JwtUtil jwtUtil;

    private final String DUMMY_TOKEN = "Bearer dummy";

    @Test
    @DisplayName("1. 关注用户-成功场景")
    void followUser_Success() throws Exception {
        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(1L);
        when(friendshipService.follow(anyLong(), anyLong(), anyBoolean())).thenReturn("关注成功");

        mockMvc.perform(post("/api/users/2/follow")
                        .header("Authorization", DUMMY_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"follow\": true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @DisplayName("2. 安全加固校验-越权删除关注记录应返回403")
    void deleteFriendship_Forbidden() throws Exception {
        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(1L);
        // 模拟 Service 校验失败返回 false
        when(friendshipService.deleteFriendship(eq(99L), eq(1L))).thenReturn(false);

        mockMvc.perform(delete("/api/users/friendship/99")
                        .header("Authorization", DUMMY_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }
}
