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

    private final String DUMMY_TOKEN = "Bearer dummy";

    @Test
    @DisplayName("1. 发送消息-成功场景")
    void sendMessage_Success() throws Exception {
        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(1L);
        when(chatService.sendMessage(anyLong(), anyLong(), anyString())).thenReturn("消息发送成功");

        Map<String, Object> msg = new HashMap<>();
        msg.put("receiverId", 2L);
        msg.put("content", "Hello");

        mockMvc.perform(post("/api/chat/messages")
                        .header("Authorization", DUMMY_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(msg)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @DisplayName("2. 安全加固校验-越权删除他人消息应返回403")
    void deleteMessage_Forbidden() throws Exception {
        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(1L);
        // 模拟 Service 校验失败返回 false
        when(chatService.deleteMessage(eq("msg-99"), eq(1L))).thenReturn(false);

        mockMvc.perform(delete("/api/chat/messages/msg-99")
                        .header("Authorization", DUMMY_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("3. 获取会话列表-成功场景")
    void getConversations_Success() throws Exception {
        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(1L);
        when(chatService.getConversations(anyLong())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/chat/conversations")
                        .header("Authorization", DUMMY_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }
}
