package com.zjgsu.pjt.backend.service;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final Map<Long, List<Map<String, Object>>> messageStore = new ConcurrentHashMap<>();

    public List<Map<String, Object>> getConversations(Long userId) {
        List<Map<String, Object>> conversations = new ArrayList<>();

        messageStore.forEach((storeKey, messages) -> {
            // 只有当 storeKey 是当前用户时，才去提取对应的聊天对象
            if (Objects.equals(storeKey, userId) && !messages.isEmpty()) {
                // 这里的逻辑需要根据消息记录提取所有不同的联系人
                Map<Long, Map<String, Object>> latestMessages = new HashMap<>();

                for (Map<String, Object> msg : messages) {
                    Long senderId = (Long) msg.get("senderId");
                    Long receiverId = (Long) msg.get("receiverId");
                    Long targetId = Objects.equals(senderId, userId) ? receiverId : senderId;

                    latestMessages.put(targetId, msg);
                }

                latestMessages.forEach((targetId, lastMsg) -> {
                    Map<String, Object> conv = new HashMap<>();
                    conv.put("userId", targetId);
                    conv.put("lastMessage", lastMsg.get("content"));
                    conv.put("lastMessageTime", lastMsg.get("createTime"));
                    conv.put("unreadCount", 0);
                    conversations.add(conv);
                });
            }
        });

        return conversations;
    }

    public Map<String, Object> getMessages(Long currentUserId, Long targetUserId, int pageNum, int pageSize) {
        List<Map<String, Object>> allMessages = messageStore.getOrDefault(currentUserId, new ArrayList<>());

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
                Math.min(fromIndex, total),
                Math.min(toIndex, total)
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
