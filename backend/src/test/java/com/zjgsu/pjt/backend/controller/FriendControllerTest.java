package com.zjgsu.pjt.backend.controller;

import com.zjgsu.pjt.backend.service.FriendService;
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

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class FriendControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FriendService friendService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private final String DUMMY_TOKEN = "Bearer dummy";

    @Test
    @DisplayName("1. 发送好友申请-成功场景")
    void sendRequest_Success() throws Exception {
        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(1L);
        when(friendService.sendFriendRequest(anyLong(), anyLong())).thenReturn("好友申请已发送");

        Map<String, Object> body = new HashMap<>();
        body.put("targetUserId", 2L);

        mockMvc.perform(post("/api/friends/requests")
                        .header("Authorization", DUMMY_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk()) // ✅ 修正：由 isCreated 改为 isOk
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @DisplayName("2. 安全校验-越权处理他人申请应返回403")
    void handleRequest_Forbidden() throws Exception {
        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(1L);
        when(friendService.handleRequest(eq(99L), anyBoolean(), eq(1L))).thenReturn(false);

        Map<String, Boolean> body = new HashMap<>();
        body.put("accept", true);

        mockMvc.perform(post("/api/friends/requests/99/handle")
                        .header("Authorization", DUMMY_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("3. 安全校验-越权删除申请记录应返回403")
    void deleteRequest_Forbidden() throws Exception {
        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(1L);
        when(friendService.deleteRequest(eq(99L), eq(1L))).thenReturn(false);

        mockMvc.perform(delete("/api/friends/requests/99")
                        .header("Authorization", DUMMY_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }
}
