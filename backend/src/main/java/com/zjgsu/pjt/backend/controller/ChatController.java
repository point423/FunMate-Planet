package com.zjgsu.pjt.backend.controller;

import com.zjgsu.pjt.backend.common.Result;
import com.zjgsu.pjt.backend.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @GetMapping("/conversations")
    public Result<List<Map<String, Object>>> getConversations(@RequestAttribute(value = "currentUserId", required = false) Long currentUserId) {
        if (currentUserId == null) {
            return Result.error(401, "未授权");
        }
        return Result.success(chatService.getConversations(currentUserId));
    }

    @GetMapping("/messages")
    public Result<Map<String, Object>> getMessages(
            @RequestParam Long targetUserId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "50") int pageSize,
            @RequestAttribute(value = "currentUserId", required = false) Long currentUserId) {

        if (currentUserId == null) {
            return Result.error(401, "未授权");
        }

        return Result.success(chatService.getMessages(currentUserId, targetUserId, pageNum, pageSize));
    }

    @PostMapping("/messages")
    public Result<String> sendMessage(@RequestBody Map<String, Object> message,
                                      @RequestAttribute(value = "currentUserId", required = false) Long currentUserId) {
        if (currentUserId == null) {
            return Result.error(401, "未授权");
        }

        Long receiverId = Long.valueOf(message.get("receiverId").toString());
        String content = message.get("content").toString();

        String result = chatService.sendMessage(currentUserId, receiverId, content);
        return Result.created(result);
    }

    @DeleteMapping("/messages/{messageId}")
    public Result<String> deleteMessage(@PathVariable String messageId) {
        boolean success = chatService.deleteMessage(messageId);
        return success ? Result.success("消息已删除") : Result.error(404, "消息不存在");
    }
}
