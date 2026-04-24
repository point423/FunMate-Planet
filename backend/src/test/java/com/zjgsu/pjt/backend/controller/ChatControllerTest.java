package com.zjgsu.pjt.backend.controller;

import com.zjgsu.pjt.backend.service.ChatService;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("测试获取会话列表接口")
    void getConversations_Success() throws Exception {
        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(1L);
        when(chatService.getConversations(anyLong())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/chat/conversations")
                        .header("Authorization", "Bearer dummy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @DisplayName("测试发送消息接口")
    void sendMessage_Success() throws Exception {
        Map<String, Object> msg = new HashMap<>();
        msg.put("receiverId", 2L);
        msg.put("content", "Hello");

        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(1L);
        when(chatService.sendMessage(anyLong(), anyLong(), anyString())).thenReturn("消息发送成功");

        mockMvc.perform(post("/api/chat/messages")
                        .header("Authorization", "Bearer dummy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(msg)))
                .andExpect(status().isOk()) // ✅ 修正为 isOk()，因为 Result.created 默认返回 200 HTTP 状态
                .andExpect(jsonPath("$.code").value(0));
    }
}
