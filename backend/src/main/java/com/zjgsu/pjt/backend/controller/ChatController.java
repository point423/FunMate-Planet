// package com.zjgsu.pjt.backend.controller;

// import com.zjgsu.pjt.backend.common.Result;
// import org.springframework.web.bind.annotation.*;

// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;

// @RestController
// @RequestMapping("/api/chat")
// @CrossOrigin(origins = "*")
// public class ChatController {

//     @GetMapping("/conversations")
//     public Result<List<Map<String, Object>>> getConversations() {
//         // 返回模拟会话列表
//         List<Map<String, Object>> list = new ArrayList<>();
//         Map<String, Object> conv = new HashMap<>();
//         conv.put("lastMessage", "你好，很高兴认识你");
//         conv.put("unreadCount", 0);
//         list.add(conv);
//         return Result.success(list);
//     }

//     @PostMapping("/messages")
//     public Result<String> sendMessage(@RequestBody Map<String, Object> message) {
//         return Result.success("消息发送成功");
//     }
// }

package com.zjgsu.pjt.backend.controller;

import com.zjgsu.pjt.backend.common.Result;
import jakarta.servlet.http.HttpServletRequest;
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
    public Result<List<Map<String, Object>>> getConversations(HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("currentUserId");
        if (currentUserId == null) return Result.error(401, "未授权");

        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> conv = new HashMap<>();
        conv.put("lastMessage", "你好，很高兴认识你");
        conv.put("unreadCount", 0);
        list.add(conv);
        return Result.success(list);
    }

    @GetMapping("/messages")
    public Result<Map<String, Object>> getMessages(
            @RequestParam Long targetUserId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "50") int pageSize,
            HttpServletRequest request) {

        Long currentUserId = (Long) request.getAttribute("currentUserId");
        if (currentUserId == null) return Result.error(401, "未授权");

        Map<String, Object> data = new HashMap<>();
        data.put("total", 0);
        data.put("list", new ArrayList<>());
        data.put("targetUserId", targetUserId);
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);
        return Result.success(data);
    }

    @PostMapping("/messages")
    public Result<String> sendMessage(@RequestBody Map<String, Object> message,
                                      HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("currentUserId");
        if (currentUserId == null) return Result.error(401, "未授权");

        Long receiverId = Long.valueOf(message.get("receiverId").toString());
        String content = message.get("content").toString();

        // 可选：这里可以实际保存消息，现在先 mock
        // ChatMessage msg = new ChatMessage();
        // msg.setSenderId(currentUserId);
        // msg.setReceiverId(receiverId);
        // msg.setContent(content);
        // chatMessageRepository.save(msg);

        return Result.created("消息发送成功");
    }
}