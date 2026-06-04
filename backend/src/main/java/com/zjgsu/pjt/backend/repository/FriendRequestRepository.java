package com.zjgsu.pjt.backend.repository;

import com.zjgsu.pjt.backend.entity.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    FriendRequest findBySenderIdAndReceiverId(Long senderId, Long receiverId);
    List<FriendRequest> findByReceiverId(Long receiverId);
    List<FriendRequest> findBySenderId(Long senderId);
}
