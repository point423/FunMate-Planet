package com.zjgsu.pjt.backend.service;

import com.zjgsu.pjt.backend.entity.Friendship;
import com.zjgsu.pjt.backend.repository.FriendshipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class FriendshipService {

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Transactional
    public String follow(Long userId, Long friendId, boolean follow) {
        var existing = friendshipRepository.findAllByUserIdAndFriendId(userId, friendId);
        if (follow) {
            if (existing.isEmpty()) {
                Friendship f = new Friendship();
                f.setUserId(userId);
                f.setFriendId(friendId);
                friendshipRepository.save(f);
            }
            return "关注成功";
        } else {
            if (!existing.isEmpty()) {
                friendshipRepository.deleteAll(existing);
            }
            return "已取消关注";
        }
    }

    public Page<Friendship> getFollowers(Long friendId, Pageable pageable) {
        return friendshipRepository.findByFriendId(friendId, pageable);
    }

    public Page<Friendship> getFollowing(Long userId, Pageable pageable) {
        return friendshipRepository.findByUserId(userId, pageable);
    }

    /**
     * 安全加固：删除社交关系（增加 IDOR 校验）
     */
    @Transactional
    public boolean deleteFriendship(Long id, Long currentUserId) {
        Friendship friendship = friendshipRepository.findById(id).orElse(null);
        if (friendship != null) {
            // 只有关系的发起者可以主动删除这条记录
            if (!Objects.equals(friendship.getUserId(), currentUserId)) {
                return false;
            }
            friendshipRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
