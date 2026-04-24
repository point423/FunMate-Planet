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
    @DisplayName("测试发送消息逻辑")
    void sendMessage_Success() {
        String result = chatService.sendMessage(1L, 2L, "Hello");
        assertEquals("消息发送成功", result);
    }

    @Test
    @DisplayName("测试获取消息列表-多页/边界情况")
    void getMessages_Boundary() {
        for(int i=0; i<15; i++) {
            chatService.sendMessage(1L, 2L, "Msg " + i);
        }
        // 测试第一页
        Map<String, Object> page1 = chatService.getMessages(1L, 2L, 1, 10);
        assertEquals(10, ((List)page1.get("list")).size());
        assertEquals(15, page1.get("total"));

        // 测试第二页
        Map<String, Object> page2 = chatService.getMessages(1L, 2L, 2, 10);
        assertEquals(5, ((List)page2.get("list")).size());
    }

    @Test
    @DisplayName("测试获取会话列表")
    void getConversations_Logic() {
        chatService.sendMessage(1L, 2L, "Last Msg");
        List<Map<String, Object>> result = chatService.getConversations(1L);
        assertFalse(result.isEmpty());
        assertEquals(2L, result.get(0).get("userId"));
    }

    @Test
    @DisplayName("测试删除消息")
    void deleteMessage_Success() {
        assertTrue(chatService.deleteMessage("any-id"));
    }
}
