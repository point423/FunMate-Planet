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
    public Result<List<Map<String, Object>>> getConversations(@RequestAttribute("currentUserId") Long currentUserId) {
        return Result.success(chatService.getConversations(currentUserId));
    }

    @GetMapping("/messages")
    public Result<Map<String, Object>> getMessages(
            @RequestParam Long targetUserId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "50") int pageSize,
            @RequestAttribute("currentUserId") Long currentUserId) {
        return Result.success(chatService.getMessages(currentUserId, targetUserId, pageNum, pageSize));
    }

    @PostMapping("/messages")
    public Result<String> sendMessage(@RequestBody Map<String, Object> message,
                                      @RequestAttribute("currentUserId") Long currentUserId) {
        Long receiverId = Long.valueOf(message.get("receiverId").toString());
        String content = message.get("content").toString();
        String result = chatService.sendMessage(currentUserId, receiverId, content);
        return Result.created(result);
    }

    /**
     * 安全修复：防止水平越权 (IDOR)
     * 只有发送者可以删除自己的消息
     */
    @DeleteMapping("/messages/{messageId}")
    public Result<String> deleteMessage(@PathVariable String messageId,
                                        @RequestAttribute("currentUserId") Long currentUserId) {
        boolean success = chatService.deleteMessage(messageId, currentUserId);
        return success ? Result.success("消息已删除") : Result.error(403, "无权删除此消息或消息不存在");
    }
}
