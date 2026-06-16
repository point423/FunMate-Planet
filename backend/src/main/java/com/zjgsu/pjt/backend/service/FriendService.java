package com.zjgsu.pjt.backend.service;

import com.zjgsu.pjt.backend.entity.User;
import com.zjgsu.pjt.backend.entity.FriendRequest;
import com.zjgsu.pjt.backend.entity.Friendship;
import com.zjgsu.pjt.backend.repository.UserRepository;
import com.zjgsu.pjt.backend.repository.FriendRequestRepository;
import com.zjgsu.pjt.backend.repository.FriendshipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class FriendService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @Autowired
    private FriendshipRepository friendshipRepository;

    public List<User> getFriends(Long userId) {
        List<Friendship> friendships = friendshipRepository.findByUserId(userId,
            PageRequest.of(0, Integer.MAX_VALUE)).getContent();

        List<Long> friendIds = friendships.stream()
            .map(Friendship::getFriendId)
            .filter(friendId -> !friendshipRepository.findAllByUserIdAndFriendId(friendId, userId).isEmpty())
            .distinct()
            .collect(Collectors.toList());

        return userRepository.findAllById(friendIds);
    }

    public User getFriendById(Long friendId) {
        return userRepository.findById(friendId).orElse(null);
    }

    @Transactional
    public boolean deleteFriend(Long userId, Long friendId) {
        List<Friendship> friendships = friendshipRepository.findAllByUserIdAndFriendId(userId, friendId);
        if (!friendships.isEmpty()) {
            friendshipRepository.deleteAll(friendships);
            List<Friendship> reverseFriendships = friendshipRepository.findAllByUserIdAndFriendId(friendId, userId);
            if (!reverseFriendships.isEmpty()) {
                friendshipRepository.deleteAll(reverseFriendships);
            }
            return true;
        }
        return false;
    }

    public String sendFriendRequest(Long senderId, Long targetUserId) {
        FriendRequest existing = friendRequestRepository.findBySenderIdAndReceiverId(senderId, targetUserId);
        if (existing != null) return "已发送过申请";

        FriendRequest req = new FriendRequest();
        req.setSenderId(senderId);
        req.setReceiverId(targetUserId);
        req.setStatus("pending");
        friendRequestRepository.save(req);
        return "好友申请已发送";
    }

    public Map<String, Object> getRequests(Long userId) {
        Map<String, Object> data = new HashMap<>();
        data.put("incoming", friendRequestRepository.findByReceiverId(userId));
        data.put("outgoing", friendRequestRepository.findBySenderId(userId));
        return data;
    }

    public FriendRequest getRequestById(Long requestId, Long currentUserId) {
        FriendRequest request = friendRequestRepository.findById(requestId).orElse(null);
        // 安全校验：只有发送者或接收者可以查看详情
        if (request != null && (request.getSenderId().equals(currentUserId) || request.getReceiverId().equals(currentUserId))) {
            return request;
        }
        return null;
    }

    @Transactional
    public boolean handleRequest(Long requestId, boolean accept, Long currentUserId) {
        FriendRequest req = friendRequestRepository.findById(requestId).orElse(null);
        if (req != null) {
            // 安全校验：只有接收者可以处理申请
            if (!Objects.equals(req.getReceiverId(), currentUserId)) {
                return false;
            }
            req.setStatus(accept ? "accepted" : "declined");
            friendRequestRepository.save(req);

            if (accept) {
                ensureFriendship(req.getSenderId(), req.getReceiverId());
                ensureFriendship(req.getReceiverId(), req.getSenderId());
            }
            return true;
        }
        return false;
    }

    private void ensureFriendship(Long userId, Long friendId) {
        if (!friendshipRepository.findAllByUserIdAndFriendId(userId, friendId).isEmpty()) {
            return;
        }

        Friendship friendship = new Friendship();
        friendship.setUserId(userId);
        friendship.setFriendId(friendId);
        friendshipRepository.save(friendship);
    }

    @Transactional
    public boolean deleteRequest(Long requestId, Long currentUserId) {
        FriendRequest req = friendRequestRepository.findById(requestId).orElse(null);
        if (req != null) {
            // 安全校验：只有相关人员可以删除记录
            if (!req.getSenderId().equals(currentUserId) && !req.getReceiverId().equals(currentUserId)) {
                return false;
            }
            friendRequestRepository.deleteById(requestId);
            return true;
        }
        return false;
    }
}
