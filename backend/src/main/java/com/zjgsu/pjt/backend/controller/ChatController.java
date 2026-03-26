package com.zjgsu.pjt.backend.controller;

import com.zjgsu.pjt.backend.common.Result;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @GetMapping("/conversations")
    public Result<List<Map<String, Object>>> getConversations() {
        // 返回模拟会话列表
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> conv = new HashMap<>();
        conv.put("lastMessage", "你好，很高兴认识你");
        conv.put("unreadCount", 0);
        list.add(conv);
        return Result.success(list);
    }

    @PostMapping("/messages")
    public Result<String> sendMessage(@RequestBody Map<String, Object> message) {
        return Result.success("消息发送成功");
    }
}
