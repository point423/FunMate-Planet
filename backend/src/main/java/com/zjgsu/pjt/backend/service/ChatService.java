package com.zjgsu.pjt.backend.service;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private static final Map<Long, List<Map<String, Object>>> messageStore = new ConcurrentHashMap<>();

    public List<Map<String, Object>> getConversations(Long userId) {
        List<Map<String, Object>> conversations = new ArrayList<>();

        messageStore.forEach((targetUserId, messages) -> {
            if (!messages.isEmpty()) {
                Map<String, Object> lastMsg = messages.get(messages.size() - 1);
                if (Objects.equals(lastMsg.get("senderId"), userId) ||
                    Objects.equals(lastMsg.get("receiverId"), userId)) {

                    Map<String, Object> conv = new HashMap<>();
                    conv.put("userId", targetUserId);
                    conv.put("lastMessage", lastMsg.get("content"));
                    conv.put("lastMessageTime", lastMsg.get("createTime"));
                    conv.put("unreadCount", 0);
                    conversations.add(conv);
                }
            }
        });

        return conversations;
    }

    public Map<String, Object> getMessages(Long currentUserId, Long targetUserId, int pageNum, int pageSize) {
        List<Map<String, Object>> allMessages = messageStore.getOrDefault(targetUserId, new ArrayList<>());

        List<Map<String, Object>> filteredMessages = allMessages.stream()
                .filter(msg ->
                    (Objects.equals(msg.get("senderId"), currentUserId) &&
                     Objects.equals(msg.get("receiverId"), targetUserId)) ||
                    (Objects.equals(msg.get("senderId"), targetUserId) &&
                     Objects.equals(msg.get("receiverId"), currentUserId)))
                .sorted((m1, m2) -> ((LocalDateTime) m2.get("createTime")).compareTo((LocalDateTime) m1.get("createTime")))
                .collect(Collectors.toList());

        int total = filteredMessages.size();
        int fromIndex = Math.max(0, total - pageNum * pageSize);
        int toIndex = Math.max(0, total - (pageNum - 1) * pageSize);

        List<Map<String, Object>> pageMessages = filteredMessages.subList(
                Math.min(fromIndex, toIndex),
                Math.max(fromIndex, toIndex)
        );

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("list", pageMessages);
        result.put("targetUserId", targetUserId);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);

        return result;
    }

    public String sendMessage(Long senderId, Long receiverId, String content) {
        Map<String, Object> message = new HashMap<>();
        message.put("id", UUID.randomUUID().toString());
        message.put("senderId", senderId);
        message.put("receiverId", receiverId);
        message.put("content", content);
        message.put("createTime", LocalDateTime.now());
        message.put("isRead", false);

        messageStore.computeIfAbsent(senderId, k -> new ArrayList<>()).add(message);
        messageStore.computeIfAbsent(receiverId, k -> new ArrayList<>()).add(message);

        return "消息发送成功";
    }

    public boolean deleteMessage(String messageId) {
        messageStore.forEach((userId, messages) -> {
            messages.removeIf(msg -> Objects.equals(msg.get("id"), messageId));
        });
        return true;
    }
}
