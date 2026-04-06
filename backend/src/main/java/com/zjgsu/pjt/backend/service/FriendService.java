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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            .collect(Collectors.toList());

        return userRepository.findAllById(friendIds);
    }

    public User getFriendById(Long friendId) {
        return userRepository.findById(friendId).orElse(null);
    }

    public boolean deleteFriend(Long userId, Long friendId) {
        Friendship friendship = friendshipRepository.findByUserIdAndFriendId(userId, friendId);
        if (friendship != null) {
            friendshipRepository.delete(friendship);
            Friendship reverseFriendship = friendshipRepository.findByUserIdAndFriendId(friendId, userId);
            if (reverseFriendship != null) {
                friendshipRepository.delete(reverseFriendship);
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

    public FriendRequest getRequestById(Long requestId) {
        return friendRequestRepository.findById(requestId).orElse(null);
    }

    public boolean handleRequest(Long requestId, boolean accept) {
        FriendRequest req = friendRequestRepository.findById(requestId).orElse(null);
        if (req != null) {
            req.setStatus(accept ? "accepted" : "declined");
            friendRequestRepository.save(req);

            if (accept) {
                Friendship friendship1 = new Friendship();
                friendship1.setUserId(req.getSenderId());
                friendship1.setFriendId(req.getReceiverId());
                friendshipRepository.save(friendship1);

                Friendship friendship2 = new Friendship();
                friendship2.setUserId(req.getReceiverId());
                friendship2.setFriendId(req.getSenderId());
                friendshipRepository.save(friendship2);
            }
            return true;
        }
        return false;
    }

    public boolean deleteRequest(Long requestId) {
        if (friendRequestRepository.existsById(requestId)) {
            friendRequestRepository.deleteById(requestId);
            return true;
        }
        return false;
    }
}
