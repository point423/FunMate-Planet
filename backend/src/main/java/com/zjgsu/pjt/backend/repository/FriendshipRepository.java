package com.zjgsu.pjt.backend.repository;

import com.zjgsu.pjt.backend.entity.Friendship;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    List<Friendship> findAllByUserIdAndFriendId(Long userId, Long friendId);
    Page<Friendship> findByUserId(Long userId, Pageable pageable);
    Page<Friendship> findByFriendId(Long friendId, Pageable pageable);
    Long countByUserId(Long userId);
    Long countByFriendId(Long friendId);
}
