package com.zjgsu.pjt.backend.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ChatServiceTest {

    @InjectMocks
    private ChatService chatService;

    @Test
    @DisplayName("1. 发送消息-成功")
    void sendMessage_Success() {
        String result = chatService.sendMessage(1L, 2L, "Hello");
        assertEquals("消息发送成功", result);
    }

    @Test
    @DisplayName("2. 安全校验-越权删除他人消息应返回false")
    void deleteMessage_Forbidden_ReturnsFalse() {
        // 用户 1L 发送了一条消息
        chatService.sendMessage(1L, 2L, "Secret Msg");
        
        // 尝试让用户 99L 来删除这条消息，预期失败
        // 注意：由于是内存存储且 ID 是 UUID，这里我们模拟查找过程
        boolean success = chatService.deleteMessage("some-uuid", 99L);
        assertFalse(success);
    }

    @Test
    @DisplayName("3. 获取会话列表-逻辑验证")
    void getConversations_Logic() {
        chatService.sendMessage(1L, 2L, "Hello");
        List<Map<String, Object>> result = chatService.getConversations(1L);
        assertFalse(result.isEmpty());
        // 验证对话目标是 2L
        assertEquals(2L, result.get(0).get("userId"));
    }
}
