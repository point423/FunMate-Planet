package com.zjgsu.pjt.backend.service;

import com.zjgsu.pjt.backend.entity.Friendship;
import com.zjgsu.pjt.backend.repository.FriendshipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FriendshipService {

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Transactional
    public String follow(Long userId, Long friendId, boolean follow) {
        Friendship existing = friendshipRepository.findByUserIdAndFriendId(userId, friendId);
        if (follow) {
            if (existing == null) {
                Friendship f = new Friendship();
                f.setUserId(userId);
                f.setFriendId(friendId);
                friendshipRepository.save(f);
            }
            return "关注成功";
        } else {
            if (existing != null) {
                friendshipRepository.delete(existing);
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

    @Transactional
    public boolean deleteFriendship(Long id) {
        if (friendshipRepository.existsById(id)) {
            friendshipRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
