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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    private final String DUMMY_TOKEN = "Bearer dummy-token";

    @Test
    @DisplayName("测试关注用户接口")
    void followUser_Success() throws Exception {
        // ✅ 核心修复：Mock Token 解析
        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(1L);
        when(friendshipService.follow(anyLong(), anyLong(), anyBoolean())).thenReturn("关注成功");

        mockMvc.perform(post("/api/users/2/follow")
                        .header("Authorization", DUMMY_TOKEN) // ✅ 携带 Token
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"follow\": true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @DisplayName("测试获取粉丝列表接口")
    void getFollowers_Success() throws Exception {
        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(1L);
        when(friendshipService.getFollowers(anyLong(), any())).thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/users/1/followers")
                        .header("Authorization", DUMMY_TOKEN)) // ✅ 补全 Token
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }
}
